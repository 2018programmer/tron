package com.dx.service.other;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Constant;
import com.dx.common.NetEnum;
import com.dx.entity.*;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainFeeWalletMapper;
import com.dx.mapper.ChainFlowMapper;
import com.dx.mapper.ChainGatherDetailMapper;
import com.dx.service.ChainBasicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ChainOperateService {

    @Autowired
    private ChainBasicService basicService;
    @Autowired
    private ChainFeeWalletMapper feeWalletMapper;
    @Autowired
    private ChainFlowMapper flowMapper;
    @Autowired
    private ChainCoinMapper coinMapper;

    @Autowired
    private ChainGatherDetailMapper gatherDetailMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;


    /**
     * 查询有足够钱的矿工费
     * @param netName
     * @param amount
     * @return
     */
    public ChainFeeWallet getFeeEnoughWallet(String netName, BigDecimal amount){
        LambdaQueryWrapper<ChainFeeWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainFeeWallet::getNetName,netName);
        wrapper.eq(ChainFeeWallet::getRunningStatus,1);

        List<ChainFeeWallet> chainFeeWallets = feeWalletMapper.selectList(wrapper);

        for (ChainFeeWallet chainFeeWallet : chainFeeWallets) {
            BigDecimal bigDecimal = basicService.queryBalance(netName, chainFeeWallet.getAddress());
            if (amount.compareTo(bigDecimal) <= 0) {
                return chainFeeWallet;
            }
        }
        return null;
    }

    /**
     * 转矿工费
     * @return
     */
    public void transferFee(BigDecimal amount, String toAddress,String netName,String coinName,Integer taskId,JSONObject jsonObject){

        //转账矿工费
        BigDecimal add = amount.add(Constant.BaseUrl.trxfee);
        ChainFeeWallet feeWallet = getFeeEnoughWallet(netName, add);

        if(Objects.isNull(feeWallet)){
            log.info("主网{},矿工费钱包余额不足",netName);
            return ;
        }
        String txId = basicService.transferBaseCoins(netName, feeWallet.getAddress(), toAddress, feeWallet.getPrivateKey(), amount);
        //查询交易结果
        try{
            Thread.sleep(3500);
        }catch (Exception e){

        }
        JSONObject json = basicService.gettransactioninfo(netName, txId);
        if(json.containsKey("fee")){
            String fee = json.getString("fee");
            BigDecimal decimal = new BigDecimal("1000000");
            BigDecimal feeNum = new BigDecimal(fee).divide(decimal, 6, RoundingMode.FLOOR);
            amount =amount.add(feeNum);
        }
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        defaultTransactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(defaultTransactionDefinition);
        //添加流水明细
        ChainFlow chainFlow = new ChainFlow();
        chainFlow.setNetName(netName);
        chainFlow.setWalletType(2);
        chainFlow.setAddress(feeWallet.getAddress());
        chainFlow.setTxId(txId);
        chainFlow.setTransferType(0);
        chainFlow.setFlowWay(3);
        chainFlow.setAmount(amount);
        chainFlow.setGroupId(taskId.toString());
        chainFlow.setTargetAddress(toAddress);
        chainFlow.setCreateTime(System.currentTimeMillis());
        chainFlow.setCoinName(coinName);
        flowMapper.insert(chainFlow);
        transactionManager.commit(status);
        jsonObject.put("fee",amount);
        if (StringUtils.isNotEmpty(feeWallet.getAddress())){
            jsonObject.put("feeAddress",feeWallet.getAddress());
        }
    }

    /**
     * TRON点对点归集  该过程只变动矿工费钱包 和对应流水
     * @return
     */
    @Transactional
    public JSONObject addressToGather(ChainGatherDetail nowtask,String toAddress,String privateKey,String code){

        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getCoinCode,code);
        ChainCoin coin = coinMapper.selectOne(wrapper);
        JSONObject jsonObject = new JSONObject();
        if("base".equals(coin.getCoinType())){
            BigDecimal balance = basicService.queryBalance(coin.getNetName(), nowtask.getGatherAddress());
            if(balance.compareTo(BigDecimal.ZERO)==0){
                return null;
            }
            //转矿工费
            transferFee(Constant.BaseUrl.trxfee, nowtask.getGatherAddress(), coin.getNetName(), NetEnum.TRON.getBaseCoin(), nowtask.getTaskId(),jsonObject);
            if(Objects.isNull(jsonObject.getString("feeAddress"))){
                return jsonObject;
            }
            nowtask.setGatherStage(2);
            gatherDetailMapper.updateById(nowtask);

            //开始归集 或者热钱包冷却
            String txId = basicService.transferBaseCoins(coin.getNetName(), nowtask.getGatherAddress(), toAddress, privateKey, balance);
            jsonObject.put("txId",txId);
            jsonObject.put("balance",balance);
        }else {
            //查合约币
            BigDecimal balance = basicService.queryContractBalance(coin.getNetName(), code, nowtask.getGatherAddress());
            if(balance.compareTo(BigDecimal.ZERO)==0){
                return null;
            }
            //查询需要消耗的trx
            String estimateenergy = basicService.estimateenergy(coin.getNetName(), nowtask.getGatherAddress(), toAddress, privateKey, coin.getCoinCode(), balance);
            //转矿工费
            transferFee(new BigDecimal(estimateenergy), nowtask.getGatherAddress(), coin.getNetName(), NetEnum.TRON.getBaseCoin(),nowtask.getTaskId(),jsonObject);
            if(Objects.isNull(jsonObject.getString("feeAddress"))){
                return jsonObject;
            }
            nowtask.setGatherStage(2);
            //开始归集 或者冷却
            String txId = basicService.transferContractCoins(coin.getNetName(), nowtask.getGatherAddress(), toAddress, privateKey, coin.getCoinCode(), balance);
            jsonObject.put("txId",txId);
            jsonObject.put("balance",balance);
        }
        return jsonObject;
    }

    /**
     * 矿工费冷却
     * @return
     */
    public String feeWalletCold(ChainFeeWallet feeWallet ,String toAddress,BigDecimal amount){

        //开始冷却
        String txId  = basicService.transferBaseCoins(feeWallet.getNetName(), feeWallet.getAddress(), toAddress, feeWallet.getPrivateKey(), amount);

        return txId;
    }

    public String hotWalletCold(ChainColdWallet wallet, ChainHotWallet hotWallet,ChainCoin transCoin,ChainCoin baseCoin) {

        //查主链币余额
        BigDecimal bigDecimal = basicService.queryBalance(hotWallet.getNetName(), hotWallet.getAddress());
        BigDecimal transAmount ;
        String txId ="";
        if(transCoin.getCoinCode().equals(baseCoin.getCoinCode())){
            transAmount =bigDecimal.subtract(Constant.BaseUrl.trxfee);
            if(bigDecimal.compareTo(Constant.BaseUrl.trxfee)<=0){
                return "任务终止请补充至少"+Constant.BaseUrl.trxfee+"矿工费";
            }
              txId= basicService.transferBaseCoins(transCoin.getNetName(), hotWallet.getAddress(), wallet.getAddress(), hotWallet.getPrivateKey(), transAmount);
        }else {
            //查合约币
            BigDecimal amount = basicService.queryContractBalance(hotWallet.getNetName(), transCoin.getCoinCode(), hotWallet.getAddress());
            transAmount =amount;
            //查询需要消耗的trx
            String estimateenergy = basicService.estimateenergy(transCoin.getNetName(), hotWallet.getAddress(), wallet.getAddress(), hotWallet.getPrivateKey(), transCoin.getCoinCode(), transAmount);
            BigDecimal fee = new BigDecimal(estimateenergy);
            if (bigDecimal.compareTo(fee)<0){
                return "任务终止请补充至少"+fee.subtract(bigDecimal)+"矿工费";
            }
            //开始归集 或者冷却
            txId = basicService.transferContractCoins(transCoin.getNetName(), hotWallet.getAddress(), wallet.getAddress(), hotWallet.getPrivateKey(), transCoin.getCoinCode(), transAmount);

        }

        if(StringUtils.isEmpty(txId)){
            return null;
        }
        try{
            Thread.sleep(3500);
        }catch (Exception e){

        }
        long gourpId = System.currentTimeMillis();
        JSONObject json = basicService.gettransactioninfo(wallet.getNetName(), txId);
        BigDecimal num6 = new BigDecimal("1000000");
        BigDecimal coldFee =BigDecimal.ZERO;
        if(json.containsKey("fee")){
            String fee = json.getString("fee");
            coldFee= new BigDecimal(fee).divide(num6, 6, RoundingMode.FLOOR);
        }
        ChainFlow feeFlow = new ChainFlow();
        feeFlow.setNetName(hotWallet.getNetName());
        feeFlow.setWalletType(3);
        feeFlow.setAddress(hotWallet.getAddress());
        feeFlow.setTxId(txId);
        feeFlow.setTransferType(0);
        feeFlow.setFlowWay(3);
        feeFlow.setAmount(coldFee);
        feeFlow.setGroupId(String.valueOf(gourpId));
        feeFlow.setCreateTime(System.currentTimeMillis());
        feeFlow.setCoinName(baseCoin.getCoinName());
        flowMapper.insert(feeFlow);
        //添加流水明细
        ChainFlow coldFlow = new ChainFlow();
        coldFlow.setNetName(hotWallet.getNetName());
        coldFlow.setWalletType(3);
        coldFlow.setAddress(hotWallet.getAddress());
        coldFlow.setTxId(txId);
        coldFlow.setGroupId(String.valueOf(gourpId));
        coldFlow.setTransferType(0);
        coldFlow.setFlowWay(5);
        coldFlow.setTargetAddress(wallet.getAddress());
        coldFlow.setCreateTime(System.currentTimeMillis());
        coldFlow.setCoinName(transCoin.getCoinName());
        if("base".equals(transCoin.getCoinType())){

            coldFlow.setAmount(transAmount);
            flowMapper.insert(coldFlow);
        }else {
            String result = json.getJSONObject("receipt").getString("result");
            if("SUCCESS".equals(result)){
                coldFlow.setAmount(transAmount);
                flowMapper.insert(coldFlow);
            }
        }
        return null;
    }
}
