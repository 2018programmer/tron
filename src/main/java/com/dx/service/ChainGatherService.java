package com.dx.service;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
            result.error("没有可用热钱包！请先新增");
            return result;
        }
        ChainHotWallet chainHotWallet = chainHotWallets.get(0);
        //创建归集明细
        LambdaQueryWrapper<ChainCoin> cwrapper = Wrappers.lambdaQuery();
        cwrapper.eq(ChainCoin::getCoinType,"base");
        ChainCoin chainCoin = coinMapper.selectOne(cwrapper);
        //获取资产表
        List<ChainAssets> assets = assetsMapper.getHaveAssets(chainHotWallet.getNetName(), null,null);
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
            chainGatherDetail.setGatherStage(0);
            chainGatherDetail.setAmount(BigDecimal.ZERO);
            chainGatherDetail.setCoinName(asset.getCoinName());
            chainGatherDetail.setTaskId(task.getId());
            chainGatherDetail.setTryTime(0);
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
        if(ObjectUtils.isNotNull(vo.getBeginTime())){
            wrapper.ge(ChainGatherTask::getCreateTime, vo.getBeginTime());
        }
        if (ObjectUtils.isNotNull(vo.getEndTime())){
            wrapper.le(ChainGatherTask::getCreateTime, vo.getEndTime());
        }
        wrapper.orderByDesc(ChainGatherTask::getId);
        IPage<ChainGatherTask> page = new Page<>(vo.getPageNum(), vo.getPageSize());
        page = gatherTaskMapper.selectPage(page, wrapper);

        LambdaQueryWrapper<ChainGatherDetail> dwrapper = Wrappers.lambdaQuery();
        IPage<GetGatherTasksDTO> convert = page.convert(u -> {
            GetGatherTasksDTO getGatherTasksDTO = new GetGatherTasksDTO();
            BeanUtils.copyProperties(u, getGatherTasksDTO);
            dwrapper.clear();
            dwrapper.eq(ChainGatherDetail::getTaskId,u.getId()).eq(ChainGatherDetail::getGatherStatus,3);
            Long aLong = gatherDetailMapper.selectCount(dwrapper);
            if(ObjectUtils.isNotNull(u.getEndTime())){
                getGatherTasksDTO.setTotalTime(DateUtil.formatBetween(new Date(u.getCreateTime()), new Date(u.getEndTime()), BetweenFormatter.Level.SECOND));

            }
            getGatherTasksDTO.setFinishNum(aLong.intValue());
            return getGatherTasksDTO;
        });

        result.setResult(convert);
        return result;
    }

    public Result<GetGatherDetailsDTO> getGatherDetails(GetGatherDetailsVO vo) {
        Result<GetGatherDetailsDTO> result = new Result<>();
        GetGatherDetailsDTO getGatherDetailsDTO = new GetGatherDetailsDTO();
        ChainGatherTask chainGatherTask = gatherTaskMapper.selectById(vo.getId());
        LambdaQueryWrapper<ChainGatherDetail> dwrapper = Wrappers.lambdaQuery();
        dwrapper.eq(ChainGatherDetail::getTaskId,vo.getId());
        List<ChainGatherDetail> chainGatherDetails = gatherDetailMapper.selectList(dwrapper);
        getGatherDetailsDTO.setFeeName(chainGatherDetails.get(0).getFeeCoinName());
        BigDecimal totalBalance = chainGatherDetails.stream()
                .map(ChainGatherDetail::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        getGatherDetailsDTO.setFeeAmount(totalBalance);
        getGatherDetailsDTO.setNetName(chainGatherTask.getNetName());
        List<GatherTotalDTO> totalList = new ArrayList<>();
        Set<String> collect = chainGatherDetails.stream().map(ChainGatherDetail::getCoinName).collect(Collectors.toSet());
        for (String s : collect) {
            GatherTotalDTO gatherTotalDTO = new GatherTotalDTO();
            gatherTotalDTO.setCoinName(s);
            BigDecimal reduce = chainGatherDetails.stream().filter(u -> u.getCoinName().equals(s)).map(ChainGatherDetail::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            gatherTotalDTO.setAmount(reduce);
            totalList.add(gatherTotalDTO);
        }

        getGatherDetailsDTO.setGatherList(totalList);


        IPage<ChainGatherDetail> page = new Page<>(vo.getPageNum(), vo.getPageSize());
        page=gatherDetailMapper.selectPage(page,dwrapper);
        IPage<GetGatherDetailDTO> convert = page.convert(u -> {
            GetGatherDetailDTO getGatherDetailDTO = new GetGatherDetailDTO();
            BeanUtils.copyProperties(u, getGatherDetailDTO);
            if(ObjectUtils.isNotNull(u.getFinishTime())){
                getGatherDetailDTO.setTotalTime(DateUtil.formatBetween(new Date(u.getCreateTime()), new Date(u.getFinishTime()), BetweenFormatter.Level.SECOND));

            }
            return getGatherDetailDTO;
        });
        getGatherDetailsDTO.setDetails(convert);

        result.setResult(getGatherDetailsDTO);

        return result;
    }

    @Transactional
    public Result cancelGatherTask(IdVO vo) {
        Result<Object> result = new Result<>();
        ChainGatherTask chainGatherTask = gatherTaskMapper.selectById(vo.getId());
        if(chainGatherTask.getTaskStatus() == 3 || chainGatherTask.getTaskStatus() == 5){
            result.error("任务已完成或已取消，无法取消！");
            return result;
        }
        chainGatherTask.setTaskStatus(3);
        gatherTaskMapper.updateById(chainGatherTask);
        //归集子任务取消
        LambdaUpdateWrapper<ChainGatherDetail> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ChainGatherDetail::getTaskId,vo.getId());
        wrapper.eq(ChainGatherDetail::getGatherStatus,0).or().eq(ChainGatherDetail::getGatherStatus,2);
        wrapper.set(ChainGatherDetail::getGatherStatus,4);
        gatherDetailMapper.update(wrapper);

        result.setMessage("操作成功！");
        return  result;
    }
}
