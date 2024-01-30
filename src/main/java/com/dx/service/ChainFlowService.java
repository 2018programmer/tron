package com.dx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.Result;
import com.dx.dto.GetGatherTasksDTO;
import com.dx.entity.ChainFlow;
import com.dx.entity.ChainGatherTask;
import com.dx.mapper.ChainFlowMapper;
import com.dx.vo.GetChainFlowsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChainFlowService {

    @Autowired
    private ChainFlowMapper flowMapper;
    public Result<IPage<ChainFlow>> getChainFlows(GetChainFlowsVO vo) {
        Result<IPage<ChainFlow>> result = new Result<>();
        LambdaQueryWrapper<ChainFlow> wrapper = Wrappers.lambdaQuery();
        if(ObjectUtils.isNotNull(vo.getFlowWay())){
            wrapper.eq(ChainFlow::getFlowWay,vo.getFlowWay());
        }
        if(ObjectUtils.isNotNull(vo.getWalletType())){
            wrapper.eq(ChainFlow::getWalletType,vo.getWalletType());
        }
        if(ObjectUtils.isNotNull(vo.getTransferType())){
            wrapper.eq(ChainFlow::getTransferType,vo.getTransferType());
        }
        if(ObjectUtils.isNotNull(vo.getBeginTime())){

        }
        if(ObjectUtils.isNotNull(vo.getEndTime())){

        }
        wrapper.orderByDesc(ChainFlow::getId);
        IPage<ChainFlow> page = new Page<>(vo.getPageNum(), vo.getPageSize());
        page =flowMapper.selectPage(page,wrapper);

        result.setResult(page);
        return result;
    }
}
