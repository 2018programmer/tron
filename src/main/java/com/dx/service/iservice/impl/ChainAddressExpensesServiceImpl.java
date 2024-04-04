package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainAddressExpenses;
import com.dx.mapper.ChainAddressExpensesMapper;
import com.dx.service.iservice.IChainAddressExpensesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChainAddressExpensesServiceImpl extends ServiceImpl<ChainAddressExpensesMapper, ChainAddressExpenses> implements IChainAddressExpensesService {
    @Override
    public Long getExpensesWrongCount() {
        LambdaQueryWrapper<ChainAddressExpenses> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainAddressExpenses::getExpensesStatus,3);
        return count(wrapper);
    }

    @Override
    public List<ChainAddressExpenses> getFinsishExpenses(String orderId) {
        LambdaQueryWrapper<ChainAddressExpenses> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainAddressExpenses::getExpensesStatus,4);
        wrapper.eq(ChainAddressExpenses::getSerial,orderId);
        return list(wrapper);
    }
}
