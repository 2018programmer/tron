package com.dx.task;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.NetEnum;
import com.dx.entity.*;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainFlowMapper;
import com.dx.mapper.ChainGatherDetailMapper;
import com.dx.mapper.ChainGatherTaskMapper;
import com.dx.service.ChainBasicService;
import com.dx.service.ChainPoolAddressService;
import com.dx.service.other.ChainOperateService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@Slf4j
public class GatherJob {


    @Autowired
    private ChainGatherDetailMapper gatherDetailMapper;

    @Autowired
    private ChainOperateService operateService;

    @Autowired
    private ChainGatherTaskMapper gatherTaskMapper;

    @Autowired
    private ChainPoolAddressService poolAddressService;

    @Autowired
    private ChainBasicService basicService;
    @Autowired
    private ChainCoinMapper coinMapper;
    @Autowired
    private ChainFlowMapper flowMapper;
    @XxlJob("executeGather")
    public void executeGather(){
        LambdaQueryWrapper<ChainGatherTask> twrapper = Wrappers.lambdaQuery();
        twrapper.eq(ChainGatherTask::getTaskStatus,1);
        twrapper.eq(ChainGatherTask::getNetName,NetEnum.TRON.getNetName());
        ChainGatherTask chainGatherTask = gatherTaskMapper.selectOne(twrapper);
        if(ObjectUtils.isNull(chainGatherTask)){
            return;
        }
        //扫描任务明细
        LambdaQueryWrapper<ChainGatherDetail> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherDetail::getGatherStatus,0);
        wrapper.eq(ChainGatherDetail::getTaskId,chainGatherTask.getId());
        wrapper.orderByDesc(ChainGatherDetail::getId);
        List<ChainGatherDetail> chainGatherDetails = gatherDetailMapper.selectList(wrapper);
        ChainGatherDetail nowTask =null;
        if(CollectionUtils.isEmpty(chainGatherDetails)){
            wrapper.clear();
            wrapper.eq(ChainGatherDetail::getGatherStatus,2);
            wrapper.eq(ChainGatherDetail::getTaskId,chainGatherTask.getId());
            wrapper.le(ChainGatherDetail::getTryTime,5);
            wrapper.orderByAsc(ChainGatherDetail::getTryTime);
            chainGatherDetails = gatherDetailMapper.selectList(wrapper);
            if(CollectionUtils.isEmpty(chainGatherDetails)){
                chainGatherTask.setTaskStatus(5);
                chainGatherTask.setEndTime(System.currentTimeMillis());
                gatherTaskMapper.updateById(chainGatherTask);
                return;
            }else {
                nowTask=chainGatherDetails.get(0);
            }

        }else {
            nowTask = chainGatherDetails.get(0);
        }
        if(ObjectUtils.isNull(nowTask)){
            return;
        }
        long start = System.currentTimeMillis();
        log.info("开始归集任务id{},子任务id{},当前常识次数{},归集地址{}",nowTask.getTaskId(),nowTask.getId(),nowTask.getTryTime()+1,nowTask.getGatherAddress());
        nowTask.setTryTime(nowTask.getTryTime()+1);
        try{
            ChainPoolAddress address = poolAddressService.getAddress(nowTask.getGatherAddress());
            LambdaQueryWrapper<ChainCoin> cwrapper = Wrappers.lambdaQuery();
            cwrapper.eq(ChainCoin::getCoinName,nowTask.getCoinName());
            cwrapper.eq(ChainCoin::getNetName, NetEnum.TRON.getNetName());
            ChainCoin transCoin = coinMapper.selectOne(cwrapper);
            JSONObject jsonObject = operateService.addressToGather(nowTask.getGatherAddress(), chainGatherTask.getAddress(), address.getPrivateKey(), transCoin.getCoinCode(),nowTask.getTaskId());
            String txId = jsonObject.getString("txId");
            if(ObjectUtils.isNotEmpty(txId)){

                Thread.sleep(3000);

                //解析记录 更新流水 和子任务
                JSONObject json = basicService.gettransactioninfo(NetEnum.TRON.getNetName(), txId);
                BigDecimal num6 = new BigDecimal("1000000");
                BigDecimal gatherFee =BigDecimal.ZERO;
                if(json.containsKey("fee")) {
                    String fee = json.getString("fee");
                    gatherFee = new BigDecimal(fee).divide(num6, 6, RoundingMode.FLOOR);
                }
                ChainFlow gatherFlow = new ChainFlow();
                gatherFlow.setNetName("TRON");
                gatherFlow.setWalletType(3);
                gatherFlow.setAddress(chainGatherTask.getAddress());
                gatherFlow.setTxId(txId);
                gatherFlow.setTransferType(1);
                gatherFlow.setFlowWay(4);
                gatherFlow.setAmount(jsonObject.getBigDecimal("balance"));
                gatherFlow.setTargetAddress(nowTask.getGatherAddress());
                gatherFlow.setCreateTime(System.currentTimeMillis());
                gatherFlow.setGroupId(chainGatherTask.getId().toString());
                gatherFlow.setCoinName(transCoin.getCoinName());
                nowTask.setGatherStatus(2);
                nowTask.setTxId(txId);
                nowTask.setFeeAmount(nowTask.getFeeAmount().add(gatherFee));
                nowTask.setFeeAddress(jsonObject.getString("feeAddress"));
                if("base".equals(transCoin.getCoinType())){
                    flowMapper.insert(gatherFlow);
                    nowTask.setGatherStatus(3);
                    nowTask.setFinishTime(System.currentTimeMillis());
                    nowTask.setAmount(jsonObject.getBigDecimal("balance"));
                }else {
                    String result = json.getJSONObject("receipt").getString("result");
                    if("SUCCESS".equals(result)){
                        flowMapper.insert(gatherFlow);
                        nowTask.setGatherStatus(3);
                        nowTask.setFinishTime(System.currentTimeMillis());
                        nowTask.setAmount(jsonObject.getBigDecimal("balance"));
                    }
                }

            }else {
                nowTask.setGatherStatus(2);
            }
        }catch (Exception e){
            e.printStackTrace();
            nowTask.setGatherStatus(2);
        }
        gatherDetailMapper.updateById(nowTask);
        if (3==nowTask.getGatherStatus()){
            log.info("归集成功任务id{},子任务id{}",nowTask.getTaskId(),nowTask.getId());
        }else {
            log.info("归集失败任务id{},子任务id{},当前尝试次数{},归集地址{}",nowTask.getTaskId(),nowTask.getId(),nowTask.getTryTime(),nowTask.getGatherAddress());
        }
        long end = System.currentTimeMillis();
        log.info("归集耗时{}秒",(end-start)/1000);
    }
}
