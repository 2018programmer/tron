package com.dx.service.iservice.impl;

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
}
