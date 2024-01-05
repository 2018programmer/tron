package com.dx.service;

import cn.hutool.core.lang.Chain;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.Result;
import com.dx.dto.GetGatherDetailDTO;
import com.dx.dto.GetGatherDetailsDTO;
import com.dx.dto.GetGatherTasksDTO;
import com.dx.entity.ChainCoin;
import com.dx.entity.ChainGatherDetail;
import com.dx.entity.ChainGatherTask;
import com.dx.entity.ChainNet;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainGatherDetailMapper;
import com.dx.mapper.ChainGatherTaskMapper;
import com.dx.mapper.ChainNetMapper;
import com.dx.vo.GetGatherDetailsVO;
import com.dx.vo.GetGatherTasksVO;
import com.dx.vo.ManualGatherVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;

@Service
public class ChainGatherService {

    @Autowired
    private ChainGatherTaskMapper gatherTaskMapper;

    @Autowired
    private ChainCoinMapper coinMapper;

    @Autowired
    private ChainGatherDetailMapper gatherDetailMapper;

    public Result manualGather(ManualGatherVO vo) {
        Result<Object> result = new Result<>();

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
