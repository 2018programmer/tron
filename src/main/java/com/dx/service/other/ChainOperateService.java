package com.dx.service.other;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Constant;
import com.dx.entity.ChainCoin;
import com.dx.entity.ChainFeeWallet;
import com.dx.entity.ChainFlow;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainFeeWalletMapper;
import com.dx.mapper.ChainFlowMapper;
import com.dx.service.ChainBasicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * 查询有足够钱的矿工费
     * @param netName
     * @param amount
     * @return
     */
    public ChainFeeWallet getFeeEnoughWallet(String netName, BigDecimal amount){
        LambdaQueryWrapper<ChainFeeWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainFeeWallet::getNetName,netName);

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

    public String transferFee(BigDecimal amount, String toAddress,String netName,String coinName){

        //转账矿工费
        BigDecimal add = amount.add(Constant.BaseUrl.trxfee);
        ChainFeeWallet feeWallet = getFeeEnoughWallet(netName, add);

        if(Objects.isNull(feeWallet)){
            log.info("主网{},矿工费钱包余额不足",netName);
            return null;
        }
        String txId = basicService.transferBaseCoins(netName, feeWallet.getAddress(), toAddress, feeWallet.getPrivateKey(), amount);
        //查询交易结果
        JSONObject json = basicService.gettransactioninfo(netName, txId);
        if(json.containsKey("fee")){
            String fee = json.getString("fee");
            BigDecimal decimal = new BigDecimal("1000000");
            BigDecimal feeNum = new BigDecimal(fee).divide(decimal, 6, RoundingMode.FLOOR);
            amount =amount.add(feeNum);
        }
        //添加流水明细
        ChainFlow chainFlow = new ChainFlow();
        chainFlow.setNetName(netName);
        chainFlow.setWalletType(2);
        chainFlow.setAddress(feeWallet.getAddress());
        chainFlow.setTxId(txId);
        chainFlow.setTransferType(0);
        chainFlow.setFlowWay(3);
        chainFlow.setAmount(amount);
        chainFlow.setTargetAddress(toAddress);
        chainFlow.setCreateTime(System.currentTimeMillis());
        chainFlow.setCoinName(coinName);
        flowMapper.insert(chainFlow);
        return txId;
    }

    /**
     * TRON点对点归集  该过程只变动矿工费钱包 和对应流水
     * @return
     */
    public String addressToGather(String fromAddress ,String toAddress,String privateKey,String code,BigDecimal amount){
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getCoinCode,code);
        ChainCoin coin = coinMapper.selectOne(wrapper);
        String txId = "";
        if("base".equals(coin.getCoinType())){
            //转矿工费
            String tx = transferFee(Constant.BaseUrl.trxfee, fromAddress, coin.getNetName(), coin.getCoinName());
            if(Objects.isNull(tx)){
                return txId;
            }
            //开始归集 或者热钱包冷却
            txId = basicService.transferBaseCoins(coin.getNetName(), fromAddress, toAddress, privateKey, amount);
        }else {
            //查询需要消耗的trx
            String estimateenergy = basicService.estimateenergy(coin.getNetName(), fromAddress, toAddress, privateKey, coin.getCoinCode(), amount);
            //转矿工费
            String tx = transferFee(new BigDecimal(estimateenergy), fromAddress, coin.getNetName(), coin.getCoinName());
            if(Objects.isNull(tx)){
                return txId;
            }
            //开始归集 或者冷却
            txId = basicService.transferContractCoins(coin.getNetName(), fromAddress, toAddress, privateKey, coin.getCoinCode(), amount);

        }
        return txId;
    }

    /**
     * TRON点对点归集  该过程只变动矿工费钱包 和对应流水
     * @return
     */
    public String feeWalletCold(ChainFeeWallet feeWallet ,String toAddress,BigDecimal amount){
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getNetName,feeWallet.getNetName());
        wrapper.eq(ChainCoin::getCoinType,"base");
        ChainCoin coin = coinMapper.selectOne(wrapper);
        amount=amount.subtract(Constant.BaseUrl.trxfee);
        //开始冷却
        String txId  = basicService.transferBaseCoins(coin.getNetName(), feeWallet.getAddress(), toAddress, feeWallet.getPrivateKey(), amount);

        return txId;
    }
}
