package com.dx.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Constant;
import com.dx.common.Result;
import com.dx.pojo.dto.AssetHotDTO;
import com.dx.entity.*;
import com.dx.mapper.*;
import com.dx.service.other.ChainOperateService;
import com.dx.pojo.vo.FreezeBalanceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChainAssetsService {

    @Autowired
    private ChainAssetsMapper assetsMapper;

    @Autowired
    private ChainOperateService operateService;

    @Autowired
    private ChainFeeWalletMapper feeWalletMapper;
    @Autowired
    private ChainColdWalletMapper coldWalletMapper;
    
    @Autowired
    private ChainHotWalletMapper hotWalletMapper;

    @Autowired
    private ChainBasicService basicService;
    
    @Autowired
    private ChainFlowMapper flowMapper;
    @Autowired
    private ChainCoinMapper coinMapper;



    public Result<List<AssetHotDTO>> getHotwalletBalance(Integer type,Integer id) {
        Result<List<AssetHotDTO>> result = new Result<>();
        List<ChainCoin> chainCoins = coinMapper.selectList(null);



        ChainHotWallet chainHotWallet = hotWalletMapper.selectById(id);
        LambdaQueryWrapper<ChainAssets> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainAssets::getAddress,chainHotWallet.getAddress());
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
        ChainHotWallet hotWallet = hotWalletMapper.selectById(vo.getId());
        LambdaQueryWrapper<ChainColdWallet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainColdWallet::getNetName,hotWallet.getNetName());
        ChainColdWallet wallet = coldWalletMapper.selectOne(wrapper);
        LambdaQueryWrapper<ChainCoin> bwrapper = Wrappers.lambdaQuery();
        bwrapper.eq(ChainCoin::getNetName,hotWallet.getNetName());
        bwrapper.eq(ChainCoin::getCoinType,"base");
        ChainCoin baseCoin = coinMapper.selectOne(bwrapper);
        String msg="";
        for (String code : vo.getCoinCodeList()) {
            bwrapper.clear();
            bwrapper.eq(ChainCoin::getNetName,hotWallet.getNetName());
            bwrapper.eq(ChainCoin::getCoinCode,code);
            ChainCoin transCoin = coinMapper.selectOne(bwrapper);
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
        ChainFeeWallet feeWallet = feeWalletMapper.selectById(vo.getId());
        LambdaQueryWrapper<ChainColdWallet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainColdWallet::getNetName,feeWallet.getNetName());
        ChainColdWallet wallet = coldWalletMapper.selectOne(wrapper);

        BigDecimal balance = basicService.queryBalance(feeWallet.getNetName(), feeWallet.getAddress());
        if(balance.compareTo(Constant.BaseUrl.trxfee)<=0){
            result.error("必须余额大于"+Constant.BaseUrl.trxfee+"才可冷却");
            return result;
        }
        balance=balance.subtract(Constant.BaseUrl.trxfee);
        String txId = operateService.feeWalletCold(feeWallet, wallet.getAddress(), balance);

        if(StringUtils.isEmpty(txId)){
            result.error("冷却失败");
            return result;
        }
        try {
            Thread.sleep(3500);
        }catch (Exception e){
        }
        long current = System.currentTimeMillis();
        JSONObject json = basicService.gettransactioninfo(wallet.getNetName(), txId);
        ChainFlow feeFlow = new ChainFlow();
        feeFlow.setNetName(feeWallet.getNetName());
        feeFlow.setWalletType(2);
        feeFlow.setAddress(feeWallet.getAddress());
        feeFlow.setTxId(txId);
        feeFlow.setTransferType(0);
        feeFlow.setFlowWay(3);
        if(json.containsKey("fee")){
            feeFlow.setAmount(Constant.BaseUrl.trxfee);
        }else {
            feeFlow.setAmount(BigDecimal.ZERO);
        }
        feeFlow.setCreateTime(current);
        feeFlow.setGroupId(String.valueOf(current));
        feeFlow.setCoinName(feeWallet.getCoinName());
        flowMapper.insert(feeFlow);

        //添加流水明细
        ChainFlow coldFlow = new ChainFlow();
        coldFlow.setNetName(feeWallet.getNetName());
        coldFlow.setWalletType(2);
        coldFlow.setAddress(feeWallet.getAddress());
        coldFlow.setTxId(txId);
        coldFlow.setTransferType(0);
        coldFlow.setFlowWay(5);
        coldFlow.setAmount(balance);
        coldFlow.setTargetAddress(wallet.getAddress());
        coldFlow.setCreateTime(current);
        coldFlow.setGroupId(String.valueOf(current));
        coldFlow.setCoinName(feeWallet.getCoinName());
        flowMapper.insert(coldFlow);
        result.setMessage("操作成功");
        return result;
    }

}
