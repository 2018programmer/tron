package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainCoin;
import com.dx.mapper.ChainCoinMapper;
import com.dx.service.iservice.IChainCoinService;
import org.springframework.stereotype.Service;


@Service
public class ChainCoinServiceImpl extends ServiceImpl<ChainCoinMapper, ChainCoin> implements IChainCoinService {
    @Override
    public Long getCoinCount() {
        return this.count();
    }

    @Override
    public ChainCoin getBaseCoin(String netName) {
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getNetName,netName);
        wrapper.eq(ChainCoin::getCoinType,"base");
        return getOne(wrapper);
    }

    @Override
    public ChainCoin getCoinByCode(String code) {
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getCoinCode,code);
        return getOne(wrapper);
    }

    @Override
    public ChainCoin getCoinByName(String coinName, String netName) {
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getCoinName,coinName);
        wrapper.eq(ChainCoin::getNetName,netName);
        return getOne(wrapper);
    }
}
