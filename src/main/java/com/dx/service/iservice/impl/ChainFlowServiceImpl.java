package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainFlow;
import com.dx.mapper.ChainFlowMapper;
import com.dx.service.iservice.IChainFlowService;
import org.springframework.stereotype.Service;

@Service
public class ChainFlowServiceImpl extends ServiceImpl<ChainFlowMapper, ChainFlow> implements IChainFlowService {
    @Override
    public Long getWalletOutCount(String address) {
        LambdaQueryWrapper<ChainFlow> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainFlow::getAddress,address);
        wrapper.and((w)->{
            w.eq(ChainFlow::getFlowWay,2).or().eq(ChainFlow::getFlowWay,5);
        });
        return count(wrapper);
    }

    @Override
    public Long getWalletInCount(String address) {
        LambdaQueryWrapper<ChainFlow> wrapper = Wrappers.lambdaQuery();
        wrapper.clear();
        wrapper.eq(ChainFlow::getAddress,address);
        wrapper.eq(ChainFlow::getFlowWay,4);
        return count(wrapper);
    }
}
