package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainAddressIncome;
import com.dx.mapper.ChainAddressIncomeMapper;
import com.dx.service.iservice.IChainAddressIncomeService;

public class ChainAddressIncomeServiceImpl extends ServiceImpl<ChainAddressIncomeMapper, ChainAddressIncome> implements IChainAddressIncomeService {
    @Override
    public Long getIncomeWrongCount() {
        LambdaQueryWrapper<ChainAddressIncome> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainAddressIncome::getEffective,1);
        wrapper.isNull(ChainAddressIncome::getSerial);
        return count(wrapper);
    }
}
