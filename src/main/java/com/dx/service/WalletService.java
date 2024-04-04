package com.dx.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Constant;
import com.dx.common.NetEnum;
import com.dx.common.Result;
import com.dx.entity.*;
import com.dx.pojo.dto.*;
import com.dx.pojo.vo.HotWalletExpensesVO;
import com.dx.service.iservice.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class WalletService {

    @Autowired
    private IChainHotWalletService chainHotWalletService;
    @Autowired
    private IChainColdWalletService chainColdWalletService;
    @Autowired
    private IChainFeeWalletService chainFeeWalletService;
    @Autowired
    private BasicService basicService;
    @Autowired
    private IChainCoinService chainCoinService;
    @Autowired
    private IChainAssetsService chainAssetsService;
    @Autowired
    private IChainAddressExpensesService chainAddressExpensesService;
    @Autowired
    private IChainFlowService chainFlowService;
    @Autowired
    private IChainGatherTaskService chainGatherTaskService;

    @Autowired
    private ApiService apiService;

    public Result updateColdWallet(UpdateColdWalletDTO dto){
        Result<Object> result = new Result<>();
        ChainColdWallet chainColdWallet = chainColdWalletService.getById(dto.getId());
        if(Objects.isNull(chainColdWallet)) {
            result.error("参数有误,该对象不存在");
            return result;
        }
        chainColdWallet.setUpdateTime(System.currentTimeMillis());
        chainColdWallet.setAddress(dto.getAddress());
        chainColdWalletService.updateById(chainColdWallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<HotWalletDTO>>  getHotWallet(String netName) {
        Result<List<HotWalletDTO>> result = new Result<>();
        LambdaQueryWrapper<ChainHotWallet> wrapper = new LambdaQueryWrapper<>();
        if(Strings.isNotEmpty(netName)){
            wrapper.eq(ChainHotWallet::getNetName,netName);
        }
        List<ChainHotWallet> chainHotWallets = chainHotWalletService.list(wrapper);
        List<HotWalletDTO> list = new ArrayList<>();
        if(CollectionUtils.isEmpty(chainHotWallets)){
            result.setResult(list);
            return result;
        }

        Map<String, String> priceList = apiService.getPriceList();

        for (ChainHotWallet chainHotWallet : chainHotWallets) {
            HotWalletDTO hotWalletDTO = new HotWalletDTO();
            BeanUtils.copyProperties(chainHotWallet,hotWalletDTO);

            Long outNum = chainFlowService.getWalletOutCount(chainHotWallet.getAddress());
            hotWalletDTO.setOutCount(outNum.intValue());
            Long inNum = chainFlowService.getWalletInCount(chainHotWallet.getAddress());
            hotWalletDTO.setInCount(inNum.intValue());

            ChainAssets chainAssets = chainAssetsService.getAssetOne(chainHotWallet.getAddress(),NetEnum.TRON.getBaseCoin());
            if(Objects.isNull(chainAssets)){
                hotWalletDTO.setBalance(BigDecimal.ZERO);
            }else {
                hotWalletDTO.setBalance(chainAssets.getBalance());
            }
            hotWalletDTO.setConvertBalance(getContractBalance(chainHotWallet.getAddress(),priceList));
            list.add(hotWalletDTO);
        }
        result.setResult(list);
        return result;
    }

    private BigDecimal getContractBalance(String address,Map<String,String> map) {
        if(CollectionUtils.isEmpty(map)){
            return BigDecimal.ZERO;
        }
        List<ChainAssets> chainAssets = chainAssetsService.getAddressAssets(address);
        BigDecimal amount =BigDecimal.ZERO;
        if(CollectionUtils.isEmpty(chainAssets)){
            return amount;
        }
        for (ChainAssets chainAsset : chainAssets) {
            BigDecimal num = BigDecimal.ZERO;
            if (!StringUtils.isEmpty(map.get(chainAsset.getCoinName()))){
                num =new BigDecimal(map.get(chainAsset.getCoinName()));
            }
            amount=amount.add(chainAsset.getBalance().multiply(num));
        }
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public Result updateHotWalletStatus(UpdateHotWalletStatusDTO dto) {
        Result<Object> result = new Result<>();
        ChainHotWallet chainHotWallet = chainHotWalletService.getById(dto.getId());
        if(Objects.isNull(chainHotWallet)){
            result.error("没有数据");
            return result;
        }
        LambdaQueryWrapper<ChainGatherTask> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherTask::getAddress,chainHotWallet.getAddress());
        wrapper.eq(ChainGatherTask::getTaskStatus,1);
        List<ChainGatherTask> chainGatherTasks = chainGatherTaskService.list(wrapper);
        if(!CollectionUtils.isEmpty(chainGatherTasks)){
            result.error("该地址有归集任务在运行,请等待结束");
            return result;
        }
        chainHotWallet.setRunningStatus(dto.getRunningStatus());
        chainHotWalletService.updateById(chainHotWallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<ChainColdWallet>> getColdWallets() {
        Result<List<ChainColdWallet>> result = new Result<>();
        List<ChainColdWallet> chainColdWallets = chainColdWalletService.list();
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
        chainHotWalletService.save(chainHotWallet);
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

        ChainCoin chainCoin = chainCoinService.getBaseCoin(dto.getNetName());

        if(!Objects.isNull(chainCoin)){
            wallet.setCoinName(chainCoin.getCoinName());
        }

        chainFeeWalletService.save(wallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<FeeWalletDTO>> getFeeWallets(String netName) {
        Result<List<FeeWalletDTO>> result = new Result<>();
        LambdaQueryWrapper<ChainFeeWallet> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotEmpty(netName)){
            wrapper.eq(ChainFeeWallet::getNetName,netName);
        }

        List<ChainFeeWallet> chainFeeWallets = chainFeeWalletService.list(wrapper);
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
        ChainFeeWallet feeWallet = chainFeeWalletService.getById(dto.getId());
        if(Objects.isNull(feeWallet)){
            result.error("没有数据");
            return result;
        }
        feeWallet.setRunningStatus(dto.getRunningStatus());
        chainFeeWalletService.updateById(feeWallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result<HotWalletExpensesDTO> hotWalletExpenses(HotWalletExpensesVO vo){
        log.info("出款参数为:{}"+vo.toString());
        Result<HotWalletExpensesDTO> result = new Result<>();

        List<ChainAddressExpenses> addressExpenses = chainAddressExpensesService.getFinsishExpenses(vo.getOrderId());
        if(!CollectionUtils.isEmpty(addressExpenses)){
            result.error("请勿重复出款");
            return result;
        }
        LambdaQueryWrapper<ChainHotWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainHotWallet::getNetName,vo.getNetName());
        wrapper.eq(ChainHotWallet::getRunningStatus,1);
        List<ChainHotWallet> chainHotWallets = chainHotWalletService.getOnHotWalletList(vo.getNetName());
        if(CollectionUtils.isEmpty(chainHotWallets)){
            result.error("热钱包为空");
            return result;
        }
        LambdaQueryWrapper<ChainPoolAddress> awrapper = Wrappers.lambdaQuery();
        awrapper.eq(ChainPoolAddress::getAddress,vo.getAddress());

        ChainCoin chainCoin = chainCoinService.getCoinByName(vo.getCoinName(),vo.getNetName());
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
            if(chainCoin.getCoinType().equals("base")){
                BigDecimal trx = basicService.queryBalance(chainCoin.getNetName(), chainHotWallet.getAddress());
                if((vo.getAmount().add(Constant.BaseUrl.trxfee)).compareTo(trx)>0){
                    continue;
                }
                txId = basicService.transferBaseCoins(chainCoin.getNetName(), chainHotWallet.getAddress(), vo.getAddress(), chainHotWallet.getPrivateKey(), vo.getAmount());
            }else {
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
            }




            if(StringUtils.isNotEmpty(txId)){
                address= chainHotWallet.getAddress();
                break;
            }
        }
        if(StringUtils.isEmpty(txId)){
            chainAddressExpenses.setExpensesStatus(3);
            chainAddressExpensesService.save(chainAddressExpenses);
            result.error("出款失败,检查热钱包余额");
            return result;
        }

        try{
            Thread.sleep(4000);
        }catch (Exception e){
        }

        JSONObject json = basicService.gettransactioninfo(NetEnum.TRON.getNetName(), txId);
        BigDecimal num6 = new BigDecimal("1000000");
        BigDecimal gatherFee =BigDecimal.ZERO;
        if(json.containsKey("fee")) {
            String fee = json.getString("fee");
            gatherFee = new BigDecimal(fee).divide(num6, 6, RoundingMode.FLOOR);
        }
        ChainFlow feeFlow = new ChainFlow();
        feeFlow.setNetName("TRON");
        feeFlow.setWalletType(3);
        feeFlow.setAddress(address);
        feeFlow.setTxId(txId);
        feeFlow.setTransferType(0);
        feeFlow.setFlowWay(3);
        feeFlow.setAmount(gatherFee);
        feeFlow.setCreateTime(System.currentTimeMillis());
        feeFlow.setGroupId(vo.getOrderId());
        feeFlow.setCoinName(NetEnum.TRON.getBaseCoin());
        chainFlowService.save(feeFlow);

        chainAddressExpenses.setAddress(address);
        chainAddressExpenses.setFinishTime(System.currentTimeMillis());
        chainAddressExpenses.setTxId(txId);
        chainAddressExpenses.setExpensesStatus(4);
        chainAddressExpenses.setFee(gatherFee);
        chainAddressExpenses.setFeeCoinName(NetEnum.TRON.getBaseCoin());
        chainAddressExpensesService.save(chainAddressExpenses);

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
        chainFlowService.save(chainFlow);

        HotWalletExpensesDTO hotWalletExpensesDTO = new HotWalletExpensesDTO();
        hotWalletExpensesDTO.setTxId(txId);
        hotWalletExpensesDTO.setAddress(address);
        result.setResult(hotWalletExpensesDTO);
        return result;
    }

    public Result vaildHotWalletBalance(BigDecimal amount,String netName,String coinName) {
        Result<Object> result = new Result<>();


        ChainCoin chainCoin = chainCoinService.getCoinByName(coinName,netName);
        List<ChainHotWallet> chainHotWallets = chainHotWalletService.getOnHotWalletList(netName);

        for (ChainHotWallet chainHotWallet : chainHotWallets) {
            BigDecimal balance ;
            BigDecimal trx=basicService.queryBalance(netName, chainHotWallet.getAddress());
            if(chainCoin.getCoinType().equals("base")){
                balance=trx;
            }else {
                balance= basicService.queryContractBalance(netName, chainCoin.getCoinCode(), chainHotWallet.getAddress());
            }
            if (trx.compareTo(new BigDecimal(15))<0){
                return result.error("trx少于15,矿工费不足请补充");
            }
            if(amount.compareTo(balance)<=0){
                return result.ok("热钱包余额充足");
            }
        }
        return result.error("热钱包余额不足");
    }
}
