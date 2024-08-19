package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainHotWallet;
import com.dx.mapper.ChainHotWalletMapper;
import com.dx.service.iservice.IChainHotWalletService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChainHotWalletServiceImpl extends ServiceImpl<ChainHotWalletMapper, ChainHotWallet> implements IChainHotWalletService {
    @Override
    public Long getOnHotWalletCount() {
        LambdaQueryWrapper<ChainHotWallet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainHotWallet::getRunningStatus,1);

        return this.count(wrapper);
    }

    @Override
    public List<ChainHotWallet> getOnHotWalletList(String netName) {
        LambdaQueryWrapper<ChainHotWallet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainHotWallet::getNetName,netName);
        wrapper.eq(ChainHotWallet::getRunningStatus,1);
        return list(wrapper);
    }

    @Override
    public List<ChainHotWallet> getHotWalletsByNet(String netName) {
        LambdaQueryWrapper<ChainHotWallet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainHotWallet::getNetName,netName);
        return list(wrapper);
    }
}
