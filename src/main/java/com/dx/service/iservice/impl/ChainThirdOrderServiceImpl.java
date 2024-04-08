package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainThirdOrder;
import com.dx.mapper.ChainThirdOrderMapper;
import com.dx.service.iservice.IChainThirdOrderService;

import java.util.List;

public class ChainThirdOrderServiceImpl extends ServiceImpl<ChainThirdOrderMapper, ChainThirdOrder> implements IChainThirdOrderService {
    @Override
    public List<ChainThirdOrder> getAvailableAddress(String netName) {
        LambdaQueryWrapper<ChainThirdOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.lt(ChainThirdOrder::getUnbindTime, System.currentTimeMillis());
        wrapper.eq(ChainThirdOrder::getNetName, netName);
        return list(wrapper);
    }

    @Override
    public ChainThirdOrder getByAddress(String address) {
        LambdaQueryWrapper<ChainThirdOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainThirdOrder::getAddress, address);
        return getOne(wrapper);
    }
}
