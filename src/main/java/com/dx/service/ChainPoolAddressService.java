package com.dx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.Result;
import com.dx.dto.*;
import com.dx.entity.ChainCoin;
import com.dx.entity.ChainNet;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainMainNetMapper;
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

    public Result<List<PoolAddressDTO>> getPoolAddress(QueryPoolAddressDTO dto) {
        Result<List<PoolAddressDTO>> result = new Result<>();
        PoolAddressDTO poolAddressDTO = new PoolAddressDTO();
        poolAddressDTO.setAddress("18mGmSB357AnGko9C1dfXJJCmRFCM9");
        poolAddressDTO.setCreateTime(new Date());
        poolAddressDTO.setIsAssigned(0);
        poolAddressDTO.setIsActivated(0);
        poolAddressDTO.setAssignType(0);
        poolAddressDTO.setAssignId(null);
        poolAddressDTO.setEstimateBalance(BigDecimal.ZERO);
        List<PoolAddressDTO> list = new ArrayList<>();
        list.add(poolAddressDTO);

        result.setResult(list);

        return result;
    }
}
