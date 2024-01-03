package com.dx.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Result;
import com.dx.dto.*;
import com.dx.entity.ChainCoin;
import com.dx.entity.ChainColdWallet;
import com.dx.entity.ChainFeeWallet;
import com.dx.entity.ChainHotWallet;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainColdWalletMapper;
import com.dx.mapper.ChainFeeWalletMapper;
import com.dx.mapper.ChainHotWalletMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ChainWalletService {

    @Autowired
    private ChainHotWalletMapper hotWalletMapper;
    
    @Autowired
    private ChainColdWalletMapper coldWalletMapper;

    @Autowired
    private ChainFeeWalletMapper feeWalletMapper;

    @Autowired
    private ChainBasicService basicService;

    @Autowired
    private ChainCoinMapper coinMapper;

    public Result updateColdWallet(UpdateColdWalletDTO dto){
        Result<Object> result = new Result<>();
        ChainColdWallet chainColdWallet = coldWalletMapper.selectById(dto.getId());
        if(Objects.isNull(chainColdWallet)) {
            result.error("参数有误,该对象不存在");
            return result;
        }
        chainColdWallet.setUpdateTime(new Date());
        chainColdWallet.setAddress(dto.getAddress());
        coldWalletMapper.updateById(chainColdWallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<HotWalletDTO>> getHotWallet(String netName) {
        Result<List<HotWalletDTO>> result = new Result<>();
        LambdaQueryWrapper<ChainHotWallet> wrapper = new LambdaQueryWrapper<>();
        if(Strings.isNotEmpty(netName)){
            wrapper.eq(ChainHotWallet::getNetName,netName);
        }
        List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(wrapper);
        if(CollectionUtils.isEmpty(chainHotWallets)){
            result.error("没有数据");
            return result;
        }
        List<HotWalletDTO> list = new ArrayList<>();
        for (ChainHotWallet chainHotWallet : chainHotWallets) {
            HotWalletDTO hotWalletDTO = new HotWalletDTO();
            BeanUtils.copyProperties(chainHotWallet,hotWalletDTO);
            hotWalletDTO.setInCount(0);
            hotWalletDTO.setOutCount(0);
            hotWalletDTO.setBalance(BigDecimal.ZERO);
            hotWalletDTO.setConvertBalance(BigDecimal.ZERO);
            list.add(hotWalletDTO);
        }
        result.setResult(list);
        return result;
    }

    public Result updateHotWalletStatus(UpdateHotWalletStatusDTO dto) {
        Result<Object> result = new Result<>();
        ChainHotWallet chainHotWallet = hotWalletMapper.selectById(dto.getId());
        if(Objects.isNull(chainHotWallet)){
            result.error("没有数据");
            return result;
        }
        chainHotWallet.setRunningStatus(dto.getRunningStatus());
        hotWalletMapper.updateById(chainHotWallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<ChainColdWallet>> getColdWallets() {
        Result<List<ChainColdWallet>> result = new Result<>();
        List<ChainColdWallet> chainColdWallets = coldWalletMapper.selectList(null);
        if(CollectionUtils.isEmpty(chainColdWallets)){
            result.error("没有数据");
            return result;
        }
        result.setResult(chainColdWallets);
        return result;
    }

    public Result addHotWallet(AddWalletDTO dto) {
        Result<Object> result = new Result<>();
        JSONObject json = basicService.createAddress(dto.getNetName());
        if(!json.containsKey("address")||!json.containsKey("privateKey")){
            result.error("新增失败");
        }
        ChainHotWallet chainHotWallet = new ChainHotWallet();
        chainHotWallet.setNetName(dto.getNetName());
        chainHotWallet.setAddress(json.getString("address"));
        chainHotWallet.setPrivateKey(json.getString("privateKey"));
        chainHotWallet.setRunningStatus(0);
        chainHotWallet.setCreateTime(new Date());
        chainHotWallet.setBalance(BigDecimal.ZERO);
        hotWalletMapper.insert(chainHotWallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result addFeeWallet(AddWalletDTO dto) {
        Result<Object> result = new Result<>();
        JSONObject json = basicService.createAddress(dto.getNetName());
        if(!json.containsKey("address")||!json.containsKey("privateKey")){
            result.error("新增失败");
        }
        ChainFeeWallet wallet = new ChainFeeWallet();
        wallet.setNetName(dto.getNetName());
        wallet.setAddress(json.getString("address"));
        wallet.setPrivateKey(json.getString("privateKey"));
        wallet.setRunningStatus(0);
        wallet.setBalance(BigDecimal.ZERO);

        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getNetName,dto.getNetName()).eq(ChainCoin::getCoinType,"base");
        ChainCoin chainCoin = coinMapper.selectOne(wrapper);

        if(!Objects.isNull(chainCoin)){
            wallet.setCoinName(chainCoin.getCoinName());
        }

        feeWalletMapper.insert(wallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<FeeWalletDTO>> getFeeWallets(String netName) {
        Result<List<FeeWalletDTO>> result = new Result<>();
        LambdaQueryWrapper<ChainFeeWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainFeeWallet::getNetName,netName);
        List<ChainFeeWallet> chainFeeWallets = feeWalletMapper.selectList(wrapper);
        if(CollectionUtils.isEmpty(chainFeeWallets)){
            result.error("数据不存在");
            return result;
        }
        List<FeeWalletDTO> list = new ArrayList<>();
        for (ChainFeeWallet chainFeeWallet : chainFeeWallets) {
            FeeWalletDTO feeWalletDTO = new FeeWalletDTO();
            BeanUtils.copyProperties(chainFeeWallet,feeWalletDTO);
            list.add(feeWalletDTO);
        }
        result.setResult(list);
        return result;
    }
}
