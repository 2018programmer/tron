package com.dx.service;

import com.dx.common.Result;
import com.dx.dto.CoinDTO;
import com.dx.entity.ChainCoin;
import com.dx.mapper.ChainCoinMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChainCoinService {
    
    @Autowired
    private ChainCoinMapper coinMapper;
    public void changeMinNum(){

        //根据入参修改 修改最小收款数额
    }

    public Result<List<CoinDTO>> getCoins() {
        Result<List<CoinDTO>> result = new Result<>();
        List<ChainCoin> chainCoins = coinMapper.selectList(null);

        if (CollectionUtils.isEmpty(chainCoins)){
            result.error("没有数据");
        }
        List<CoinDTO> list = new ArrayList<>();
        for (ChainCoin chainCoin : chainCoins) {
            CoinDTO coinDTO = new CoinDTO();
            BeanUtils.copyProperties(chainCoin,coinDTO);
            coinDTO.setCoinType("数字币");
            list.add(coinDTO);
        }
        result.setResult(list);
        return result;
    }
}
