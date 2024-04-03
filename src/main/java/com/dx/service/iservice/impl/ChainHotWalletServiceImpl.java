package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainHotWallet;
import com.dx.mapper.ChainHotWalletMapper;
import com.dx.service.iservice.IChainHotWalletService;
import org.springframework.stereotype.Service;

@Service
public class ChainHotWalletServiceImpl extends ServiceImpl<ChainHotWalletMapper, ChainHotWallet> implements IChainHotWalletService {
    @Override
    public Long getOnHotWalletCount() {
        LambdaQueryWrapper<ChainHotWallet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainHotWallet::getRunningStatus,1);

        return this.count(wrapper);
    }
}
