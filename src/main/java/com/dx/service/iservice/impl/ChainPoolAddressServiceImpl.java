package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainHotWallet;
import com.dx.entity.ChainPoolAddress;
import com.dx.mapper.ChainPoolAddressMapper;
import com.dx.service.iservice.IChainPoolAddressService;
import org.springframework.stereotype.Service;

@Service
public class ChainPoolAddressServiceImpl extends ServiceImpl<ChainPoolAddressMapper, ChainPoolAddress> implements IChainPoolAddressService {
    @Override
    public Long getActiveAddressCount() {
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getIsActivated,1);
        return this.count(wrapper);
    }


}
