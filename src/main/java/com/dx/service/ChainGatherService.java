package com.dx.service;

import cn.hutool.core.lang.Chain;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.Constant;
import com.dx.common.Result;
import com.dx.dto.GetGatherDetailDTO;
import com.dx.dto.GetGatherDetailsDTO;
import com.dx.dto.GetGatherTasksDTO;
import com.dx.entity.*;
import com.dx.mapper.*;
import com.dx.vo.GetGatherDetailsVO;
import com.dx.vo.GetGatherTasksVO;
import com.dx.vo.ManualGatherVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class ChainGatherService {

    @Autowired
    private ChainGatherTaskMapper gatherTaskMapper;

    @Autowired
    private ChainCoinMapper coinMapper;

    @Autowired
    private ChainGatherDetailMapper gatherDetailMapper;

    @Autowired
    private ChainHotWalletMapper hotWalletMapper;
    
    @Autowired
    private ChainBasicService basicService;


    @Autowired
    private ChainFeeWalletMapper chainFeeWalletMapper;

    @Autowired
    private ChainFlowMapper flowMapper;

    @Autowired
    private ChainAssetsMapper assetsMapper;


    public Result manualGather(ManualGatherVO vo) {
        Result<Object> result = new Result<>();
        LambdaQueryWrapper<ChainGatherTask> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherTask::getNetName,vo.getNetName());
        wrapper.eq(ChainGatherTask::getTaskStatus,1);
        List<ChainGatherTask> chainGatherTasks = gatherTaskMapper.selectList(wrapper);
        
        if(CollectionUtils.isNotEmpty(chainGatherTasks)){
            result.error("正在归集中,请勿使用手动归集");
            return result;
        }
        LambdaQueryWrapper<ChainHotWallet> hotwrapper = Wrappers.lambdaQuery();
        hotwrapper.eq(ChainHotWallet::getNetName,vo.getNetName());
        hotwrapper.eq(ChainHotWallet::getRunningStatus,1);
        List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(hotwrapper);

        if(CollectionUtils.isEmpty(chainHotWallets)){
            result.error("没有可用热钱包！");
            return result;
        }
        ChainHotWallet chainHotWallet = chainHotWallets.get(0);
        //创建归集明细
        LambdaQueryWrapper<ChainCoin> cwrapper = Wrappers.lambdaQuery();
        cwrapper.eq(ChainCoin::getCoinType,"base");
        ChainCoin chainCoin = coinMapper.selectOne(cwrapper);
        //获取资产表
        List<ChainAssets> assets = assetsMapper.getHaveAssets(chainHotWallet.getNetName(), null);
        if(CollectionUtils.isEmpty(assets)){
            result.error("没有达到归集要求的地址");
            return result;
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
            chainGatherDetail.setAmount(asset.getBalance());
            chainGatherDetail.setCoinName(asset.getCoinName());
            chainGatherDetail.setCreateTime(System.currentTimeMillis());
            chainGatherDetail.setTaskId(task.getId());
            chainGatherDetail.setFeeAmount(BigDecimal.ZERO);
            chainGatherDetail.setFeeCoinName(chainCoin.getCoinName());

            gatherDetailMapper.insert(chainGatherDetail);
        }

        result.setMessage("操作成功！");
        return result;
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
            transferFee(Constant.BaseUrl.trxfee,fromAddress,coin.getNetName(),coin.getCoinName());
            //开始归集 或者热钱包冷却
            txId = basicService.transferBaseCoins(coin.getNetName(), fromAddress, toAddress, privateKey, amount);
        }else {
            //查询需要消耗的trx
            String estimateenergy = basicService.estimateenergy(coin.getNetName(), fromAddress, toAddress, privateKey, coin.getCoinCode(), amount);
            //转矿工费
            transferFee(new BigDecimal(estimateenergy),fromAddress,coin.getNetName(),coin.getCoinName());
            //开始归集 或者冷却
            txId = basicService.transferContractCoins(coin.getNetName(), fromAddress, toAddress, privateKey, coin.getCoinCode(), amount);

        }
        return txId;
    }

    /**
     * 转矿工费
     * @return
     */

    public void transferFee(BigDecimal amount, String toAddress,String netName,String coinName){
        LambdaQueryWrapper<ChainFeeWallet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainFeeWallet::getRunningStatus,1);
        //转账矿工费
        BigDecimal add = amount.add(Constant.BaseUrl.trxfee);
        wrapper.gt(ChainFeeWallet::getBalance,add);
        List<ChainFeeWallet> chainFeeWallets = chainFeeWalletMapper.selectList(wrapper);
        ChainFeeWallet feeWallet = chainFeeWallets.get(0);

        String txId = basicService.transferBaseCoins(netName, feeWallet.getAddress(), toAddress, feeWallet.getPrivateKey(), amount);

        //查询交易结果
        JSONObject json = basicService.gettransactioninfo(netName, txId);
        if(json.containsKey("fee")){
            String fee = json.getString("fee");
            BigDecimal decimal = new BigDecimal("1000000");
            BigDecimal feeNum = new BigDecimal(fee).divide(decimal, 6, RoundingMode.FLOOR);
            amount =amount.add(feeNum);
        }
        BigDecimal subtract = feeWallet.getBalance().subtract(amount);

        feeWallet.setBalance(subtract);
        chainFeeWalletMapper.updateById(feeWallet);

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
    }


    public Result<IPage<GetGatherTasksDTO>> getGatherTasks(GetGatherTasksVO vo) {
        Result<IPage<GetGatherTasksDTO>> result = new Result<>();
        LambdaQueryWrapper<ChainGatherTask> wrapper = Wrappers.lambdaQuery();
        IPage<ChainGatherTask> page = new Page<>(vo.getPageNum(), vo.getPageSize());
        page = gatherTaskMapper.selectPage(page, wrapper);

        IPage<GetGatherTasksDTO> convert = page.convert(u -> {
            GetGatherTasksDTO getGatherTasksDTO = new GetGatherTasksDTO();
            BeanUtils.copyProperties(u, getGatherTasksDTO);
            getGatherTasksDTO.setFinishNum(0);
            return getGatherTasksDTO;
        });

        result.setResult(convert);
        return result;
    }

    public Result<GetGatherDetailsDTO> getGatherDetails(GetGatherDetailsVO vo) {
        Result<GetGatherDetailsDTO> result = new Result<>();
        GetGatherDetailsDTO getGatherDetailsDTO = new GetGatherDetailsDTO();
        ChainGatherTask chainGatherTask = gatherTaskMapper.selectById(vo.getId());
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getNetName,chainGatherTask.getNetName());
        wrapper.eq(ChainCoin::getCoinType,"base");
        ChainCoin chainCoin = coinMapper.selectOne(wrapper);
        getGatherDetailsDTO.setFeeName(chainCoin.getCoinName());
        getGatherDetailsDTO.setFeeAmount(BigDecimal.TWO);
        getGatherDetailsDTO.setNetName(chainGatherTask.getNetName());
        HashMap<String, BigDecimal> map = new HashMap<>();
        map.put("USDT",new BigDecimal("101.289"));
        map.put("TRX",new BigDecimal("38989.2998"));
        getGatherDetailsDTO.setGatherMap(map);

        LambdaQueryWrapper<ChainGatherDetail> dwrapper = Wrappers.lambdaQuery();
        dwrapper.eq(ChainGatherDetail::getTaskId,vo.getId());
        IPage<ChainGatherDetail> page = new Page<>(vo.getPageNum(), vo.getPageSize());
        page=gatherDetailMapper.selectPage(page,dwrapper);
        IPage<GetGatherDetailDTO> convert = page.convert(u -> {
            GetGatherDetailDTO getGatherDetailDTO = new GetGatherDetailDTO();
            BeanUtils.copyProperties(u, getGatherDetailDTO);
            return getGatherDetailDTO;
        });
        getGatherDetailsDTO.setDetails(convert);

        result.setResult(getGatherDetailsDTO);

        return result;
    }
}
