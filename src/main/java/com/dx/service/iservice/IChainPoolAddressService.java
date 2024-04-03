package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainPoolAddress;

public interface IChainPoolAddressService extends IService<ChainPoolAddress> {
    Long getActiveAddressCount();

}
