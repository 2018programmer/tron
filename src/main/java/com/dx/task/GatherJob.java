package com.dx.task;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.dx.common.NetEnum;
import com.dx.entity.*;
import com.dx.service.BasicService;
import com.dx.service.iservice.*;
import com.dx.service.other.OperateService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GatherJob {

    @Autowired
    private IChainGatherDetailService chainGatherDetailService;

    @Autowired
    private OperateService operateService;
    @Autowired
    private IChainGatherTaskService chainGatherTaskService;
    @Autowired
    private IChainPoolAddressService chainPoolAddressService;
    @Autowired
    private BasicService basicService;
    @Autowired
    private IChainCoinService chainCoinService;
    @Autowired
    private IChainFlowService chainFlowService;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private IChainNetService chainNetService;
    @Autowired
    private IChainHotWalletService chainHotWalletService;
    @Autowired
    private IChainAssetsService chainAssetsService;

    @XxlJob("autoGather")
    public void autoGather(){
        List<ChainNet> chainNets = chainNetService.getRunningNets();
        for (ChainNet chainNet : chainNets) {
            ChainGatherTask chainGatherTask = chainGatherTaskService.getRunningTask(NetEnum.TRON.getNetName());
            if(ObjectUtils.isNull(chainGatherTask)){
                continue;
            }
            List<ChainHotWallet> chainHotWallets = chainHotWalletService.getOnHotWalletList(chainNet.getNetName());
            if(CollectionUtils.isEmpty(chainHotWallets)){
                continue;
            }
            ChainHotWallet chainHotWallet = chainHotWallets.get(0);
            //创建归集明细

            List<ChainCoin> chainCoins = chainCoinService.getByNet(chainNet.getNetName());
            ChainCoin base = chainCoins.stream().filter(o -> o.getCoinType().equals("base")).collect(Collectors.toList()).get(0);
            //获取资产表
            List<ChainAssets> assets = chainAssetsService.getHaveAssets(chainHotWallet.getNetName(), null,1);

            if(CollectionUtils.isEmpty(assets)){
                continue;
            }
            ChainGatherTask task = new ChainGatherTask();
            task.setGatherType(1);
            task.setAddress(chainHotWallet.getAddress());
            task.setTaskStatus(1);
            task.setCreateTime(System.currentTimeMillis());
            task.setNetName(chainHotWallet.getNetName());
            task.setTotalNum(assets.size());
            chainGatherTaskService.save(task);
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
                chainGatherDetail.setFeeCoinName(base.getCoinName());

                chainGatherDetailService.save(chainGatherDetail);
            }
        }

    }

    @XxlJob("executeGather")
    public void executeGather(){
        log.info("开始扫描归集任务");


        ChainGatherTask chainGatherTask = chainGatherTaskService.getRunningTask(NetEnum.TRON.getNetName());
        if(ObjectUtils.isNull(chainGatherTask)){
            log.info("没有归集任务");
            return;
        }
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        defaultTransactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(defaultTransactionDefinition);
        //扫描任务明细

        List<ChainGatherDetail> chainGatherDetails = chainGatherDetailService.getNotStartDetail(chainGatherTask.getId());
        ChainGatherDetail nowTask =null;

        if(CollectionUtils.isEmpty(chainGatherDetails)){

            chainGatherDetails = chainGatherDetailService.getGoingDetail(chainGatherTask.getId());
            if(!CollectionUtils.isEmpty(chainGatherDetails)){
                nowTask=chainGatherDetails.get(0);
            }
        }else {
            nowTask = chainGatherDetails.get(0);
        }
        if(ObjectUtils.isNull(nowTask)){
            chainGatherTask.setTaskStatus(5);
            chainGatherTask.setEndTime(System.currentTimeMillis());
            chainGatherTaskService.updateById(chainGatherTask);
            transactionManager.commit(status);
            return;
        }
        long start = System.currentTimeMillis();
        log.info("开始归集任务id{},子任务id{},当前尝试次数{},归集地址{}",nowTask.getTaskId(),nowTask.getId(),nowTask.getTryTime()+1,nowTask.getGatherAddress());
        //开始执行该子任务,并且更改阶段
        nowTask.setGatherStage(1);
        nowTask.setGatherStatus(1);
        nowTask.setCreateTime(System.currentTimeMillis());
        nowTask.setTryTime(nowTask.getTryTime()+1);
        chainGatherDetailService.updateById(nowTask);
        transactionManager.commit(status);
        execute(nowTask,chainGatherTask,start);
    }

    public void execute(ChainGatherDetail nowTask, ChainGatherTask chainGatherTask,Long start) {

        ChainPoolAddress address = chainPoolAddressService.getByAddress(nowTask.getGatherAddress());

        ChainCoin transCoin = chainCoinService.getCoinByName(nowTask.getCoinName(), NetEnum.TRON.getNetName());

        try{
            JSONObject jsonObject = operateService.addressToGather(nowTask, chainGatherTask.getAddress(), address.getPrivateKey(), transCoin.getCoinCode());
            if(null==jsonObject){
                nowTask.setGatherStatus(2);
                nowTask.setGatherStage(1);
                nowTask.setFinishTime(System.currentTimeMillis());
            }else {
                String txId = jsonObject.getString("txId");
                if(ObjectUtils.isNotEmpty(txId)){

                    Thread.sleep(4000);
                    //解析记录 更新流水 和子任务
                    JSONObject json = basicService.gettransactioninfo(NetEnum.TRON.getNetName(), txId);

//                    BigDecimal num6 = new BigDecimal("1000000");
//                    BigDecimal gatherFee =BigDecimal.ZERO;
//                    if(json.containsKey("fee")) {
//                        String fee = json.getString("fee");
//                        gatherFee = new BigDecimal(fee).divide(num6, 6, RoundingMode.FLOOR);
//                    }
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
                    nowTask.setFeeAmount(nowTask.getFeeAmount().add(jsonObject.getBigDecimal("fee")));
                    nowTask.setFeeAddress(jsonObject.getString("feeAddress"));
                    if("base".equals(transCoin.getCoinType())){
                        chainFlowService.save(gatherFlow);
                        nowTask.setGatherStatus(3);
                        nowTask.setGatherStage(3);
                        nowTask.setFinishTime(System.currentTimeMillis());
                        nowTask.setAmount(jsonObject.getBigDecimal("balance"));
                    }else {
                        String result = json.getJSONObject("receipt").getString("result");
                        if("SUCCESS".equals(result)){
                            chainFlowService.save(gatherFlow);
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
            log.error("归集发生异常{},{}",e.getMessage(),e.getStackTrace());
            nowTask.setGatherStatus(2);
        }
        chainGatherDetailService.updateById(nowTask);
        if (3==nowTask.getGatherStatus()){
            log.info("归集成功任务id{},子任务id{}",nowTask.getTaskId(),nowTask.getId());
        }else {
            log.info("归集失败任务id{},子任务id{},当前尝试次数{},归集地址{}",nowTask.getTaskId(),nowTask.getId(),nowTask.getTryTime(),nowTask.getGatherAddress());
        }
        long end = System.currentTimeMillis();
        log.info("归集耗时{}秒",(end-start)/1000);
    }
}
