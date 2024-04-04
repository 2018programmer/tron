package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.common.NetEnum;
import com.dx.entity.ChainFeeWallet;
import com.dx.mapper.ChainFeeWalletMapper;
import com.dx.service.iservice.IChainFeeWalletService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChainFeeWalletServiceImpl extends ServiceImpl<ChainFeeWalletMapper, ChainFeeWallet> implements IChainFeeWalletService {
    @Override
    public List<ChainFeeWallet> getByNet(String netName) {
        LambdaQueryWrapper<ChainFeeWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainFeeWallet::getNetName, NetEnum.TRON.getNetName());
        return list(wrapper);
    }
}
