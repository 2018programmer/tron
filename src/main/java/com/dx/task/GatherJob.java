package com.dx.task;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.NetEnum;
import com.dx.entity.*;
import com.dx.mapper.*;
import com.dx.service.ChainBasicService;
import com.dx.service.ChainPoolAddressService;
import com.dx.service.other.ChainOperateService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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

    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ChainNetMapper netMapper;
    @Autowired
    private ChainHotWalletMapper hotWalletMapper;
    @Autowired
    private ChainAssetsMapper assetsMapper;

    @XxlJob("autoGather")
    public void autoGather(){
        LambdaQueryWrapper<ChainNet> nwrapper = Wrappers.lambdaQuery();
        nwrapper.eq(ChainNet::getRunningStatus,1);
        List<ChainNet> chainNets = netMapper.selectList(nwrapper);
        for (ChainNet chainNet : chainNets) {
            LambdaQueryWrapper<ChainGatherTask> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(ChainGatherTask::getNetName,chainNet.getNetName());
            wrapper.eq(ChainGatherTask::getTaskStatus,1);
            List<ChainGatherTask> chainGatherTasks = gatherTaskMapper.selectList(wrapper);

            if(CollectionUtils.isNotEmpty(chainGatherTasks)){
                continue;
            }
            LambdaQueryWrapper<ChainHotWallet> hotwrapper = Wrappers.lambdaQuery();
            hotwrapper.eq(ChainHotWallet::getNetName,chainNet.getNetName());
            hotwrapper.eq(ChainHotWallet::getRunningStatus,1);
            List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(hotwrapper);

            if(CollectionUtils.isEmpty(chainHotWallets)){
                continue;
            }
            ChainHotWallet chainHotWallet = chainHotWallets.get(0);
            //创建归集明细
            LambdaQueryWrapper<ChainCoin> cwrapper = Wrappers.lambdaQuery();
            cwrapper.eq(ChainCoin::getCoinType,"base");
            ChainCoin chainCoin = coinMapper.selectOne(cwrapper);
            //获取资产表
            List<ChainAssets> assets = assetsMapper.getHaveAssets(chainHotWallet.getNetName(), null);
            if(CollectionUtils.isEmpty(assets)){
                continue;
            }
            ChainGatherTask task = new ChainGatherTask();
            task.setGatherType(0);
            task.setAddress(chainHotWallet.getAddress());
            task.setTaskStatus(1);
            task.setCreateTime(System.currentTimeMillis());
            task.setNetName(chainHotWallet.getNetName());
            task.setTotalNum(assets.size());
            gatherTaskMapper.insert(task);
            //创建 对应明细
            for (ChainAssets asset : assets) {
                ChainGatherDetail chainGatherDetail = new ChainGatherDetail();
                chainGatherDetail.setGatherAddress(asset.getAddress());
                chainGatherDetail.setGatherStatus(0);
                chainGatherDetail.setGatherStage(0);
                chainGatherDetail.setAmount(BigDecimal.ZERO);
                chainGatherDetail.setCoinName(asset.getCoinName());
                chainGatherDetail.setTaskId(task.getId());
                chainGatherDetail.setTryTime(0);
                chainGatherDetail.setFeeAmount(BigDecimal.ZERO);
                chainGatherDetail.setFeeCoinName(chainCoin.getCoinName());

                gatherDetailMapper.insert(chainGatherDetail);
            }
        }

    }

    @XxlJob("executeGather")
    public void executeGather(){
        log.info("开始扫描归集任务");

        LambdaQueryWrapper<ChainGatherTask> twrapper = Wrappers.lambdaQuery();
        twrapper.eq(ChainGatherTask::getTaskStatus,1);
        twrapper.eq(ChainGatherTask::getNetName,NetEnum.TRON.getNetName());
        ChainGatherTask chainGatherTask = gatherTaskMapper.selectOne(twrapper);
        if(ObjectUtils.isNull(chainGatherTask)){
            log.info("没有归集任务");
            return;
        }
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        defaultTransactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(defaultTransactionDefinition);
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
            if(!CollectionUtils.isEmpty(chainGatherDetails)){
                nowTask=chainGatherDetails.get(0);
            }
        }else {
            nowTask = chainGatherDetails.get(0);
        }
        if(ObjectUtils.isNull(nowTask)){
            chainGatherTask.setTaskStatus(5);
            chainGatherTask.setEndTime(System.currentTimeMillis());
            gatherTaskMapper.updateById(chainGatherTask);
            transactionManager.commit(status);
            return;
        }
        long start = System.currentTimeMillis();
        log.info("开始归集任务id{},子任务id{},当前常识次数{},归集地址{}",nowTask.getTaskId(),nowTask.getId(),nowTask.getTryTime()+1,nowTask.getGatherAddress());
        //开始执行该子任务,并且更改阶段
        nowTask.setGatherStage(1);
        nowTask.setGatherStatus(1);
        nowTask.setCreateTime(System.currentTimeMillis());
        nowTask.setTryTime(nowTask.getTryTime()+1);
        gatherDetailMapper.updateById(nowTask);
        transactionManager.commit(status);
        execute(nowTask,chainGatherTask,start);
    }

    public void execute(ChainGatherDetail nowTask, ChainGatherTask chainGatherTask,Long start) {

            ChainPoolAddress address = poolAddressService.getAddress(nowTask.getGatherAddress());
            LambdaQueryWrapper<ChainCoin> cwrapper = Wrappers.lambdaQuery();
            cwrapper.eq(ChainCoin::getCoinName,nowTask.getCoinName());
            cwrapper.eq(ChainCoin::getNetName, NetEnum.TRON.getNetName());
            ChainCoin transCoin = coinMapper.selectOne(cwrapper);
        try{
            JSONObject jsonObject = operateService.addressToGather(nowTask, chainGatherTask.getAddress(), address.getPrivateKey(), transCoin.getCoinCode());
            if(ObjectUtils.isNull(jsonObject)){
                nowTask.setGatherStatus(3);
                nowTask.setGatherStage(3);
                nowTask.setFinishTime(System.currentTimeMillis());
            }else {
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
                        nowTask.setGatherStage(3);
                        nowTask.setFinishTime(System.currentTimeMillis());
                        nowTask.setAmount(jsonObject.getBigDecimal("balance"));
                    }else {
                        String result = json.getJSONObject("receipt").getString("result");
                        if("SUCCESS".equals(result)){
                            flowMapper.insert(gatherFlow);
                            nowTask.setGatherStatus(3);
                            nowTask.setGatherStage(3);
                            nowTask.setFinishTime(System.currentTimeMillis());
                            nowTask.setAmount(jsonObject.getBigDecimal("balance"));
                        }
                    }

                }else {
                    nowTask.setGatherStatus(2);
                }
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
