package com.dx.service.iservice.impl;

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
}
