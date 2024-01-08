package com.dx.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Constant;
import com.dx.common.Result;
import com.dx.dto.AssetHotDTO;
import com.dx.entity.*;
import com.dx.mapper.*;
import com.dx.vo.FreezeBalanceVO;
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
    private ChainGatherService gatherService;

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



    public Result<List<AssetHotDTO>> getHotwalletBalance(Integer type,Integer id) {
        Result<List<AssetHotDTO>> result = new Result<>();

        AssetHotDTO assetHotDTO = new AssetHotDTO();
        assetHotDTO.setBalance(new BigDecimal("100"));
        assetHotDTO.setId(1);
        assetHotDTO.setCoinCode("TRX");
        assetHotDTO.setCoinName("TRX");
        List<AssetHotDTO> list = new ArrayList<>();
        list.add(assetHotDTO);

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
        for (String code : vo.getCoinCodeList()) {
            LambdaQueryWrapper<ChainAssets> awrapper = Wrappers.lambdaQuery();
            awrapper.eq(ChainAssets::getAddress,hotWallet.getAddress());
            awrapper.eq(ChainAssets::getCoinCode,code);
            ChainAssets chainAssets = assetsMapper.selectOne(awrapper);
            String txId = gatherService.addressToGather(hotWallet.getAddress(), wallet.getAddress(), hotWallet.getPrivateKey(), code, chainAssets.getBalance());
            if(StringUtils.isEmpty(txId)){
                continue;
            }
            JSONObject json = basicService.gettransactioninfo(wallet.getNetName(), txId);
            if(json.containsKey("result")&&"FAILED".equals(json.containsKey("result"))){
                continue;
            }
            chainAssets.setBalance(BigDecimal.ZERO);
            assetsMapper.updateById(chainAssets);
            ChainFlow coldFlow = new ChainFlow();
            coldFlow.setNetName(hotWallet.getNetName());
            coldFlow.setWalletType(3);
            coldFlow.setAddress(hotWallet.getAddress());
            coldFlow.setTxId(txId);
            coldFlow.setTransferType(0);
            coldFlow.setFlowWay(5);
            coldFlow.setAmount(chainAssets.getBalance());
            coldFlow.setTargetAddress(wallet.getAddress());
            coldFlow.setCreateTime(System.currentTimeMillis());
            coldFlow.setCoinName(chainAssets.getCoinName());
            flowMapper.insert(coldFlow);
            result.setMessage("操作成功");
        }

        result.setMessage("操作成功");
        return result;

    }

    public Result freezeFeeBalance(FreezeBalanceVO vo) {
        Result<Object> result = new Result<>();
        ChainFeeWallet feeWallet = feeWalletMapper.selectById(vo.getId());
        LambdaQueryWrapper<ChainColdWallet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainColdWallet::getNetName,feeWallet.getNetName());
        ChainColdWallet wallet = coldWalletMapper.selectOne(wrapper);
        BigDecimal balance = feeWallet.getBalance();
        String txId = gatherService.feeWalletCold(feeWallet, wallet.getAddress(), balance);

        if(StringUtils.isEmpty(txId)){
            result.error("冷却失败");
            return result;
        }
        JSONObject json = basicService.gettransactioninfo(wallet.getNetName(), txId);
        
        if(json.containsKey("fee")){
            feeWallet.setBalance(BigDecimal.ZERO);
            ChainFlow feeFlow = new ChainFlow();
            feeFlow.setNetName(feeWallet.getNetName());
            feeFlow.setWalletType(2);
            feeFlow.setAddress(feeWallet.getAddress());
            feeFlow.setTxId(txId);
            feeFlow.setTransferType(0);
            feeFlow.setFlowWay(3);
            feeFlow.setAmount(Constant.BaseUrl.trxfee);
            feeFlow.setTargetAddress(feeWallet.getNetName());
            feeFlow.setCreateTime(System.currentTimeMillis());
            feeFlow.setCoinName(feeWallet.getCoinName());
            flowMapper.insert(feeFlow);
        }else {
            feeWallet.setBalance(Constant.BaseUrl.trxfee);
        }
        feeWalletMapper.updateById(feeWallet);

        //添加流水明细
        ChainFlow coldFlow = new ChainFlow();
        coldFlow.setNetName(feeWallet.getNetName());
        coldFlow.setWalletType(2);
        coldFlow.setAddress(feeWallet.getAddress());
        coldFlow.setTxId(txId);
        coldFlow.setTransferType(0);
        coldFlow.setFlowWay(5);
        BigDecimal subtract = balance.subtract(Constant.BaseUrl.trxfee);
        coldFlow.setAmount(subtract);
        coldFlow.setTargetAddress(wallet.getAddress());
        coldFlow.setCreateTime(System.currentTimeMillis());
        coldFlow.setCoinName(feeWallet.getCoinName());
        flowMapper.insert(coldFlow);
        result.setMessage("操作成功");
        return result;
    }

}
