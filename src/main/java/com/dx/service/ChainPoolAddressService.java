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
import com.dx.mapper.ChainNetMapper;
import com.dx.mapper.ChainPoolAddressMapper;
import com.dx.vo.QueryPoolAddressVO;
import com.dx.vo.UpdatePoolManageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ChainPoolAddressService {


    @Autowired
    private ChainCoinMapper coinMapper;

    @Autowired
    private ChainNetMapper netMapper;

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

    public Result updatePoolManage(UpdatePoolManageVO vo) {
        Result<Object> result = new Result<>();
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getCoinCode,vo.getCoinCode());
        ChainCoin chainCoin = coinMapper.selectOne(wrapper);
        if(Objects.isNull(chainCoin)){
            result.error("币种编码有误");
            return result;
        }
        if(ObjectUtils.isNotNull(vo.getThreshold())){
            chainCoin.setThreshold(vo.getThreshold());
        }
        if(ObjectUtils.isNotNull(vo.getAutoGather())){
            chainCoin.setAutoGather(vo.getAutoGather());
        }


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

            if("TRON".equals(chainNet.getNetName())){
                poolManageDTO.setTotalNum(12000);
                poolManageDTO.setGatherStatus(0);
                poolManageDTO.setNoAssignedNum(200);
            }
            if("ETH".equals(chainNet.getNetName())){
                poolManageDTO.setTotalNum(100);
                poolManageDTO.setGatherStatus(1);
                poolManageDTO.setNoAssignedNum(50);
            }
            poollist.add(poolManageDTO);
        }
        result.setResult(poollist);
        return  result;
    }

    public Result<IPage<PoolAddressDTO>> getPoolAddress(QueryPoolAddressVO vo) {
        Result<IPage<PoolAddressDTO>> result = new Result<>();

        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        if(ObjectUtils.isNotNull(vo.getAssignId())){
            wrapper.eq(ChainPoolAddress::getAssignedId,vo.getIsAssigned());
        }
        if(ObjectUtils.isNotNull(vo.getIsActivated())){
            wrapper.eq(ChainPoolAddress::getIsActivated,vo.getIsActivated());
        }
        if(ObjectUtils.isNotNull(vo.getIsAssigned())){
            wrapper.eq(ChainPoolAddress::getIsAssigned,vo.getIsAssigned());
        }
        if(ObjectUtils.isNotNull(vo.getAssignType())){
            wrapper.eq(ChainPoolAddress::getAssignType,vo.getAssignType());
        }
        IPage<ChainPoolAddress> page = new Page<>(vo.getPageNum(), vo.getPageSize());
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

    public Result<GetGatherNumDTO> getGatherNum(String netName) {
        Result<GetGatherNumDTO> result = new Result<>();
        GetGatherNumDTO getGatherNumDTO = new GetGatherNumDTO();
        getGatherNumDTO.setNum(255);
        getGatherNumDTO.setNetName(netName);

        result.setResult(getGatherNumDTO);
        result.setResult(getGatherNumDTO);
        return result;
    }
}
