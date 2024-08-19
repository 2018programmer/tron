package com.dx.service.iservice;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainCoin;

import java.util.List;

public interface IChainCoinService extends IService<ChainCoin> {
    Long getCoinCount();

    ChainCoin getBaseCoin(String netName);

    ChainCoin getCoinByCode(String code);

    ChainCoin getCoinByName(String coinName, String netName);

    List<ChainCoin> getContractCoin(String netName);

    List<ChainCoin> getByNet(String netName);
}
