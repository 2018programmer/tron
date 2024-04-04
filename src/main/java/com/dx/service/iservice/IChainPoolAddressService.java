package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainPoolAddress;

import java.util.List;

public interface IChainPoolAddressService extends IService<ChainPoolAddress> {
    Long getActiveAddressCount();

    Long getCount(String netName);

    Long getNoAssignedNum(String netName);

    List<ChainPoolAddress> getByAssigned(String assignedId, Integer assignType, String netName);

    List<ChainPoolAddress> getMatchAddress();

    ChainPoolAddress getValidAddress(String address, String netName);

    void unbindAddress(String address);

    ChainPoolAddress getByAddress(String address);

    List<ChainPoolAddress> getAssignedByNet(String netName);
}
