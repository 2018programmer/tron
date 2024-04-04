package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainAddressIncome;

public interface IChainAddressIncomeService extends IService<ChainAddressIncome> {
    Long getIncomeWrongCount();
}
