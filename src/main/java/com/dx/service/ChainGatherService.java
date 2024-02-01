package com.dx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.IdVO;
import com.dx.common.Result;
import com.dx.pojo.dto.GatherTotalDTO;
import com.dx.pojo.dto.GetGatherDetailDTO;
import com.dx.pojo.dto.GetGatherDetailsDTO;
import com.dx.pojo.dto.GetGatherTasksDTO;
import com.dx.entity.*;
import com.dx.mapper.*;
import com.dx.pojo.vo.GetGatherDetailsVO;
import com.dx.pojo.vo.GetGatherTasksVO;
import com.dx.pojo.vo.ManualGatherVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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

        List<GatherTotalDTO> totalList = new ArrayList<>();
        GatherTotalDTO gatherTotalDTO = new GatherTotalDTO();
        gatherTotalDTO.setCoinName("USDT");
        gatherTotalDTO.setAmount(new BigDecimal("101.289"));
        totalList.add(gatherTotalDTO);
        GatherTotalDTO gatherTotalDTO1 = new GatherTotalDTO();
        gatherTotalDTO1.setCoinName("TRX");
        gatherTotalDTO.setAmount(new BigDecimal("38989.2998"));
        totalList.add(gatherTotalDTO1);
        getGatherDetailsDTO.setGatherList(totalList);

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

    public Result cancelGatherTask(IdVO vo) {
        Result<Object> result = new Result<>();

        result.setMessage("操作成功！");
        return  result;
    }
}
