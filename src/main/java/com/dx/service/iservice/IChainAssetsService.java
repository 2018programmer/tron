package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainAssets;

import java.util.List;

public interface IChainAssetsService extends IService<ChainAssets> {
    List<ChainAssets> getHaveAssets(String netName, String coinName,Integer autoGather);
}
