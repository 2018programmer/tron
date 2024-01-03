package com.dx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.Result;
import com.dx.dto.*;
import com.dx.entity.ChainCoin;
import com.dx.entity.ChainNet;
import com.dx.entity.ChainPoolAddress;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainMainNetMapper;
import com.dx.mapper.ChainPoolAddressMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ChainPoolAddressService {


    @Autowired
    private ChainCoinMapper coinMapper;

    @Autowired
    private ChainMainNetMapper netMapper;

    @Autowired
    private ChainPoolAddressMapper poolAddressMapper;

    public Result<IPage<CoinManageDTO>> getPoolManage(String netName,Integer pageNum,Integer pageSize) {
        Result<IPage<CoinManageDTO>> result = new Result<>();
        IPage<ChainCoin> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getNetName,netName);
        page=coinMapper.selectPage(page,wrapper);
        IPage<CoinManageDTO> convert = page.convert(u -> {
            CoinManageDTO coinManageDTO = new CoinManageDTO();
            BeanUtils.copyProperties(u, coinManageDTO);
            coinManageDTO.setTotalBalance(BigDecimal.ZERO);
            return coinManageDTO;
        });
        result.setResult(convert);
        return result;
    }

    public Result updatePoolManage(UpdatePoolManageDTO dto) {
        Result<Object> result = new Result<>();
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getCoinCode,dto.getCoinCode());
        ChainCoin chainCoin = coinMapper.selectOne(wrapper);
        if(Objects.isNull(chainCoin)){
            result.error("币种编码有误");
            return result;
        }
        chainCoin.setThreshold(dto.getThreshold());
        coinMapper.updateById(chainCoin);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<PoolManageDTO>> getNets() {
        Result<List<PoolManageDTO>> result = new Result<>();
        List<ChainNet> chainNets = netMapper.selectList(null);
        List<PoolManageDTO> poollist = new ArrayList<>();
        for (ChainNet chainNet : chainNets) {
            PoolManageDTO poolManageDTO = new PoolManageDTO();
            poolManageDTO.setNetName(chainNet.getNetName());
            poolManageDTO.setTotalNum(0);
            poolManageDTO.setGatherStatus(0);
            poolManageDTO.setNoAssignedNum(0);
            poollist.add(poolManageDTO);
        }
        result.setResult(poollist);
        return  result;
    }

    public Result<IPage<PoolAddressDTO>> getPoolAddress(QueryPoolAddressDTO dto) {
        Result<IPage<PoolAddressDTO>> result = new Result<>();

        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        if(ObjectUtils.isNotNull(dto.getAssignId())){
            wrapper.eq(ChainPoolAddress::getAssignedId,dto.getIsAssigned());
        }
        if(ObjectUtils.isNotNull(dto.getIsActivated())){
            wrapper.eq(ChainPoolAddress::getIsActivated,dto.getIsActivated());
        }
        if(ObjectUtils.isNotNull(dto.getIsAssigned())){
            wrapper.eq(ChainPoolAddress::getIsAssigned,dto.getIsAssigned());
        }
        if(ObjectUtils.isNotNull(dto.getAssignType())){
            wrapper.eq(ChainPoolAddress::getAssignType,dto.getAssignType());
        }
        IPage<ChainPoolAddress> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        page=poolAddressMapper.selectPage(page,wrapper);

        IPage<PoolAddressDTO> convert = page.convert(u -> {
            PoolAddressDTO poolAddressDTO = new PoolAddressDTO();
            BeanUtils.copyProperties(u, poolAddressDTO);
            poolAddressDTO.setEstimateBalance(BigDecimal.ZERO);
            return poolAddressDTO;
        });

        result.setResult(convert);

        return result;
    }
}
