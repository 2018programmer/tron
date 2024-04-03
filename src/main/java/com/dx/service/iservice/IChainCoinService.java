package com.dx.service.iservice;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainCoin;

public interface IChainCoinService extends IService<ChainCoin> {
    Long getCoinCount();
}
