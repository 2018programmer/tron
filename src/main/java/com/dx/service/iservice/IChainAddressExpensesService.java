package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainAddressExpenses;

import java.util.List;

public interface IChainAddressExpensesService extends IService<ChainAddressExpenses> {
    Long getExpensesWrongCount();

    List<ChainAddressExpenses> getFinsishExpenses(String orderId);
}
