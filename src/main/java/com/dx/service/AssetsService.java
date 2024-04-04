package com.dx.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.dx.common.Constant;
import com.dx.common.Result;
import com.dx.entity.*;
import com.dx.pojo.dto.AssetHotDTO;
import com.dx.pojo.vo.FreezeBalanceVO;
import com.dx.service.iservice.*;
import com.dx.service.other.OperateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssetsService {

    @Autowired
    private OperateService operateService;
    @Autowired
    private IChainFeeWalletService chainFeeWalletService;
    @Autowired
    private BasicService basicService;

    @Autowired
    private IChainHotWalletService chainHotWalletService;

    @Autowired
    private IChainCoinService chainCoinService;

    @Autowired
    private IChainColdWalletService chainColdWalletService;

    @Autowired
    private IChainFlowService chainFlowService;




    public Result<List<AssetHotDTO>> getHotwalletBalance(Integer type,Integer id) {
        Result<List<AssetHotDTO>> result = new Result<>();
        List<ChainCoin> chainCoins = chainCoinService.list();
        ChainHotWallet chainHotWallet = chainHotWalletService.getById(id);
        List<AssetHotDTO> list = new ArrayList<>();
        for (ChainCoin chainCoin : chainCoins) {
            if(ObjectUtils.isNotNull(type)){
                if(1==type){
                    if(!"base".equals(chainCoin.getCoinType())){
                        continue;
                    }
                }
                if(2==type){
                    if("base".equals(chainCoin.getCoinType())){
                        continue;
                    }
                }
            }
            AssetHotDTO assetHotDTO = new AssetHotDTO();
            assetHotDTO.setId(id);
            BigDecimal amount =BigDecimal.ZERO;
            if("base".equals(chainCoin.getCoinType())){
                amount = basicService.queryBalance(chainCoin.getNetName(), chainHotWallet.getAddress());
            }else {
                amount = basicService.queryContractBalance(chainCoin.getNetName(), chainCoin.getCoinCode(), chainHotWallet.getAddress());
            }
            assetHotDTO.setBalance(amount);
            assetHotDTO.setCoinCode(chainCoin.getCoinCode());
            assetHotDTO.setCoinName(chainCoin.getCoinName());
            list.add(assetHotDTO);
        }
        result.setResult(list);
        return result;
    }

    public Result freezeHotBalance(FreezeBalanceVO vo) {
        Result<Object> result = new Result<>();
        if(CollectionUtils.isEmpty(vo.getCoinCodeList())){
            result.error("未选择币种");
            return result;
        }
        ChainHotWallet hotWallet = chainHotWalletService.getById(vo.getId());
        ChainColdWallet wallet = chainColdWalletService.getByNet(hotWallet.getNetName());
        ChainCoin baseCoin =chainCoinService.getBaseCoin(hotWallet.getNetName());
        String msg="";
        for (String code : vo.getCoinCodeList()) {
            ChainCoin transCoin =chainCoinService.getCoinByCode(code);
            msg =operateService.hotWalletCold(wallet, hotWallet,transCoin,baseCoin);
            if (!StringUtils.isEmpty(msg)){
                break;
            }
        }
        if(StringUtils.isEmpty(msg)){
            result.setMessage("操作成功");
        }else {
            result.error(msg);
        }
        return result;

    }

    public Result freezeFeeBalance(FreezeBalanceVO vo) {
        Result<Object> result = new Result<>();
        ChainFeeWallet feeWallet = chainFeeWalletService.getById(vo.getId());
        ChainColdWallet coldWallet = chainColdWalletService.getByNet(feeWallet.getNetName());

        BigDecimal balance = basicService.queryBalance(feeWallet.getNetName(), feeWallet.getAddress());
        if(balance.compareTo(Constant.BaseUrl.trxfee)<=0){
            result.error("必须余额大于"+Constant.BaseUrl.trxfee+"才可冷却");
            return result;
        }
        balance=balance.subtract(Constant.BaseUrl.trxfee);
        String txId = operateService.feeWalletCold(feeWallet, coldWallet.getAddress(), balance);

        if(StringUtils.isEmpty(txId)){
            result.error("冷却失败");
            return result;
        }
        try {
            Thread.sleep(4000);
        }catch (Exception e){
        }
        long current = System.currentTimeMillis();
        JSONObject json = basicService.gettransactioninfo(coldWallet.getNetName(), txId);
        ChainFlow feeFlow = new ChainFlow();
        feeFlow.setNetName(feeWallet.getNetName());
        feeFlow.setWalletType(2);
        feeFlow.setAddress(feeWallet.getAddress());
        feeFlow.setTxId(txId);
        feeFlow.setTransferType(0);
        feeFlow.setFlowWay(3);

        BigDecimal coldFee =BigDecimal.ZERO;
        BigDecimal num6 = new BigDecimal("1000000");
        if(json.containsKey("fee")){
            String fee = json.getString("fee");
            coldFee= new BigDecimal(fee).divide(num6, 6, RoundingMode.FLOOR);
        }
        feeFlow.setAmount(coldFee);
        feeFlow.setCreateTime(current);
        feeFlow.setGroupId(String.valueOf(current));
        feeFlow.setCoinName(feeWallet.getCoinName());
        chainFlowService.save(feeFlow);

        //添加流水明细
        ChainFlow coldFlow = new ChainFlow();
        coldFlow.setNetName(feeWallet.getNetName());
        coldFlow.setWalletType(2);
        coldFlow.setAddress(feeWallet.getAddress());
        coldFlow.setTxId(txId);
        coldFlow.setTransferType(0);
        coldFlow.setFlowWay(5);
        coldFlow.setAmount(balance);
        coldFlow.setTargetAddress(coldWallet.getAddress());
        coldFlow.setCreateTime(current);
        coldFlow.setGroupId(String.valueOf(current));
        coldFlow.setCoinName(feeWallet.getCoinName());
        chainFlowService.save(coldFlow);
        result.setMessage("操作成功");
        return result;
    }

}
