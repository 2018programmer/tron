package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainAddressExpenses;

public interface IChainAddressExpensesService extends IService<ChainAddressExpenses> {
    Long getExpensesWrongCount();
}
