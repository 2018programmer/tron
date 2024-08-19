package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainAssets;
import com.dx.mapper.ChainAssetsMapper;
import com.dx.service.iservice.IChainAssetsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChainAssetsServiceImpl extends ServiceImpl<ChainAssetsMapper, ChainAssets> implements IChainAssetsService {
    @Override
    public List<ChainAssets> getHaveAssets(String netName, String coinName,Integer autoGather) {
        return this.getBaseMapper().getHaveAssets(netName,coinName,autoGather);
    }

    @Override
    public List<ChainAssets> getAssetsBytype(String netName, String coinName, int i) {
        LambdaQueryWrapper<ChainAssets> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainAssets::getNetName,netName);
        wrapper.eq(ChainAssets::getCoinName,coinName);
        wrapper.eq(ChainAssets::getAssetType,2);
        return list(wrapper);
    }

    @Override
    public List<ChainAssets> getAddressAssets(String address) {
        LambdaQueryWrapper<ChainAssets> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainAssets::getAddress,address);
        return list(wrapper);
    }

    @Override
    public ChainAssets getAssetOne(String address, String baseCoin) {
        LambdaQueryWrapper<ChainAssets> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainAssets::getAddress,address);
        wrapper.eq(ChainAssets::getCoinName, baseCoin);
        return getOne(wrapper);
    }
}
