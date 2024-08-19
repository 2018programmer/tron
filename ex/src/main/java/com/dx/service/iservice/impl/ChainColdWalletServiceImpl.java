package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainColdWallet;
import com.dx.mapper.ChainColdWalletMapper;
import com.dx.service.iservice.IChainColdWalletService;
import org.springframework.stereotype.Service;

@Service
public class ChainColdWalletServiceImpl extends ServiceImpl<ChainColdWalletMapper, ChainColdWallet> implements IChainColdWalletService {
    @Override
    public ChainColdWallet getByNet(String netName) {
        LambdaQueryWrapper<ChainColdWallet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainColdWallet::getNetName,netName);
        return getOne(wrapper);
    }
}
