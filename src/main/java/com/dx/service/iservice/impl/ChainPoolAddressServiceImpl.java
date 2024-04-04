package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainPoolAddress;
import com.dx.mapper.ChainPoolAddressMapper;
import com.dx.service.iservice.IChainPoolAddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChainPoolAddressServiceImpl extends ServiceImpl<ChainPoolAddressMapper, ChainPoolAddress> implements IChainPoolAddressService {
    @Override
    public Long getActiveAddressCount() {
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getIsActivated,1);
        return this.count(wrapper);
    }

    @Override
    public Long getCount(String netName) {
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getNetName,netName);
        return count(wrapper);
    }

    @Override
    public Long getNoAssignedNum(String netName) {
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getNetName,netName);
        wrapper.eq(ChainPoolAddress::getIsAssigned,0);
        return count(wrapper);
    }

    @Override
    public List<ChainPoolAddress> getByAssigned(String assignedId, Integer assignType, String netName) {
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getAssignedId,assignedId);
        wrapper.eq(ChainPoolAddress::getAssignType,assignType);
        wrapper.eq(ChainPoolAddress::getNetName,netName);
        wrapper.eq(ChainPoolAddress::getIsDelete,0);
        return list(wrapper);
    }

    @Override
    public List<ChainPoolAddress> getMatchAddress() {
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getIsAssigned,0);
        wrapper.orderByAsc(ChainPoolAddress::getId);
        wrapper.last("limit 8");
        return list(wrapper);
    }

    @Override
    public ChainPoolAddress getValidAddress(String address, String netName) {
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getAddress,address);
        wrapper.eq(ChainPoolAddress::getNetName,netName);
        wrapper.eq(ChainPoolAddress::getIsAssigned,1);
        wrapper.eq(ChainPoolAddress::getIsDelete,0);
        return getOne(wrapper);
    }

    @Override
    public void unbindAddress(String address) {
        LambdaUpdateWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ChainPoolAddress::getAddress,address);
        wrapper.set(ChainPoolAddress::getIsDelete,1);
        update(wrapper);
    }

    @Override
    public ChainPoolAddress getByAddress(String address) {
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getAddress,address);
        return getOne(wrapper);
    }

    @Override
    public List<ChainPoolAddress> getAssignedByNet(String netName) {
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getNetName, netName);
        wrapper.eq(ChainPoolAddress::getIsAssigned,1);
        return list(wrapper);
    }


}
