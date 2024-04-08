package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainThirdOrder;

import java.util.List;

public interface IChainThirdOrderService extends IService<ChainThirdOrder> {
    List<ChainThirdOrder> getAvailableAddress(String netName);

    ChainThirdOrder getByAddress(String address);
}
