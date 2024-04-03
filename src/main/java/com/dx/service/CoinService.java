package com.dx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.Result;
import com.dx.pojo.dto.CoinDTO;
import com.dx.pojo.vo.UpdateMinNumVO;
import com.dx.entity.ChainCoin;
import com.dx.mapper.ChainCoinMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CoinService {
    
    @Autowired
    private ChainCoinMapper coinMapper;
    public Result updateMinNum(UpdateMinNumVO vo){
        Result<Object> result = new Result<>();
        ChainCoin chainCoin = coinMapper.selectById(vo.getId());
        if(Objects.isNull(chainCoin)){
            result.error("对象不存在,参数有误");
        }
        chainCoin.setMinNum(vo.getMinNum());
        coinMapper.updateById(chainCoin);

        result.setMessage("操作成功");
        return result;

    }

    public Result<IPage<CoinDTO>> getCoins(Integer pageNum,Integer pageSize) {
        Result<IPage<CoinDTO>> result = new Result<>();
        IPage<ChainCoin> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getRunningStatus,1);
        page=coinMapper.selectPage(page, wrapper);
        IPage<CoinDTO> convert = page.convert(u -> {
            CoinDTO coinDTO = new CoinDTO();
            BeanUtils.copyProperties(u, coinDTO);
            return coinDTO;
        });
        result.setResult(convert);
        return result;
    }
}