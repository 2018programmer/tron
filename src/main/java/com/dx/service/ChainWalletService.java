package com.dx.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Result;
import com.dx.dto.*;
import com.dx.entity.*;
import com.dx.mapper.*;
import com.dx.vo.HotWalletExpensesVO;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ChainWalletService {

    @Autowired
    private ChainHotWalletMapper hotWalletMapper;

    @Autowired
    private ChainPoolAddressMapper chainPoolAddressMapper;
    
    @Autowired
    private ChainColdWalletMapper coldWalletMapper;

    @Autowired
    private ChainFeeWalletMapper feeWalletMapper;

    @Autowired
    private ChainBasicService basicService;

    @Autowired
    private ChainCoinMapper coinMapper;

    @Autowired
    private ChainAddressExpensesMapper addressExpensesMapper;
    @Autowired
    private ChainFlowMapper flowMapper;

    public Result updateColdWallet(UpdateColdWalletDTO dto){
        Result<Object> result = new Result<>();
        ChainColdWallet chainColdWallet = coldWalletMapper.selectById(dto.getId());
        if(Objects.isNull(chainColdWallet)) {
            result.error("参数有误,该对象不存在");
            return result;
        }
        chainColdWallet.setUpdateTime(System.currentTimeMillis());
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
        LambdaQueryWrapper<ChainAddressExpenses> ewrapper = Wrappers.lambdaQuery();
        List<HotWalletDTO> list = new ArrayList<>();
        for (ChainHotWallet chainHotWallet : chainHotWallets) {
            HotWalletDTO hotWalletDTO = new HotWalletDTO();
            BeanUtils.copyProperties(chainHotWallet,hotWalletDTO);
            ewrapper.clear();
            ewrapper.eq(ChainAddressExpenses::getAddress,chainHotWallet.getAddress());
            ewrapper.eq(ChainAddressExpenses::getExpensesStatus,4);
            Long num = addressExpensesMapper.selectCount(ewrapper);
            hotWalletDTO.setOutCount(num.intValue());
            hotWalletDTO.setInCount(0);
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
        chainHotWallet.setCreateTime(System.currentTimeMillis());
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
        if(StringUtils.isNotEmpty(netName)){
            wrapper.eq(ChainFeeWallet::getNetName,netName);
        }

        List<ChainFeeWallet> chainFeeWallets = feeWalletMapper.selectList(wrapper);
        List<FeeWalletDTO> list = new ArrayList<>();
        if(!CollectionUtils.isEmpty(chainFeeWallets)){
            for (ChainFeeWallet chainFeeWallet : chainFeeWallets) {
                FeeWalletDTO feeWalletDTO = new FeeWalletDTO();
                BeanUtils.copyProperties(chainFeeWallet,feeWalletDTO);
                list.add(feeWalletDTO);
            }
        }
        result.setResult(list);
        return result;
    }

    public Result updateFeeWalletStatus(UpdateHotWalletStatusDTO dto) {

        Result<Object> result = new Result<>();
        ChainFeeWallet feeWallet = feeWalletMapper.selectById(dto.getId());
        if(Objects.isNull(feeWallet)){
            result.error("没有数据");
            return result;
        }
        feeWallet.setRunningStatus(dto.getRunningStatus());
        feeWalletMapper.updateById(feeWallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result<HotWalletExpensesDTO> hotWalletExpenses(HotWalletExpensesVO vo){
        log.info("出款参数为:{}"+vo.toString());
        Result<HotWalletExpensesDTO> result = new Result<>();
        LambdaQueryWrapper<ChainHotWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainHotWallet::getNetName,vo.getNetName());
        wrapper.eq(ChainHotWallet::getRunningStatus,1);

        List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(wrapper);
        if(CollectionUtils.isEmpty(chainHotWallets)){
            result.error("热钱包为空");
            return result;
        }
        LambdaQueryWrapper<ChainPoolAddress> awrapper = Wrappers.lambdaQuery();
        awrapper.eq(ChainPoolAddress::getAddress,vo.getAddress());

        LambdaQueryWrapper<ChainCoin> cwrapper = Wrappers.lambdaQuery();
        cwrapper.eq(ChainCoin::getCoinName,vo.getCoinName());
        cwrapper.eq(ChainCoin::getNetName,vo.getNetName());
        ChainCoin chainCoin = coinMapper.selectOne(cwrapper);
        String txId ="";
        String address="";
        ChainAddressExpenses chainAddressExpenses = new ChainAddressExpenses();
        chainAddressExpenses.setCoinName(chainCoin.getCoinName());
        chainAddressExpenses.setNetName(chainCoin.getNetName());
        chainAddressExpenses.setAmount(vo.getAmount());
        chainAddressExpenses.setSerial(vo.getOrderId());
        chainAddressExpenses.setCreateTime(System.currentTimeMillis());
        chainAddressExpenses.setTryTime(1);

        for (ChainHotWallet chainHotWallet : chainHotWallets) {

            BigDecimal balance = basicService.queryContractBalance(chainCoin.getNetName(), chainCoin.getCoinCode(), chainHotWallet.getAddress());
            if(vo.getAmount().compareTo(balance)>0){
                continue;
            }
            String estimateenergy = basicService.estimateenergy(chainCoin.getNetName(), chainHotWallet.getAddress()
                    , vo.getAddress(), chainHotWallet.getPrivateKey(), chainCoin.getCoinCode(), vo.getAmount());
            BigDecimal trx = basicService.queryBalance(chainCoin.getNetName(), chainHotWallet.getAddress());
            if (trx.compareTo(new BigDecimal(estimateenergy))<0){
                continue;
            }
             txId= basicService.transferContractCoins(chainCoin.getNetName(), chainHotWallet.getAddress(), vo.getAddress()
                    , chainHotWallet.getPrivateKey(), chainCoin.getCoinCode(), vo.getAmount());

            if(StringUtils.isNotEmpty(txId)){
                address= chainHotWallet.getAddress();
                try{
                    Thread.sleep(1000);
                }catch (Exception e){
                }
                break;
            }
        }

        chainAddressExpenses.setAddress(address);
        chainAddressExpenses.setFinishTime(System.currentTimeMillis());
        if(StringUtils.isEmpty(txId)){
            chainAddressExpenses.setExpensesStatus(3);
            addressExpensesMapper.insert(chainAddressExpenses);
            result.error("出款失败,检查热钱包余额");
            return result;
        }
        chainAddressExpenses.setTxId(txId);
        chainAddressExpenses.setExpensesStatus(4);
        addressExpensesMapper.insert(chainAddressExpenses);
        ChainFlow chainFlow = new ChainFlow();
        chainFlow.setGroupId(vo.getOrderId());
        chainFlow.setGroupId(vo.getOrderId());
        chainFlow.setAddress(address);
        chainFlow.setFlowWay(2);
        chainFlow.setAmount(vo.getAmount());
        chainFlow.setTransferType(0);
        chainFlow.setCoinName(chainCoin.getCoinName());
        chainFlow.setTxId(txId);
        chainFlow.setWalletType(3);
        chainFlow.setTargetAddress(vo.getAddress());
        chainFlow.setNetName(chainCoin.getNetName());
        chainFlow.setCreateTime(System.currentTimeMillis());
        flowMapper.insert(chainFlow);
        HotWalletExpensesDTO hotWalletExpensesDTO = new HotWalletExpensesDTO();
        hotWalletExpensesDTO.setTxId(txId);
        hotWalletExpensesDTO.setAddress(address);
        result.setResult(hotWalletExpensesDTO);
        return result;
    }
}
