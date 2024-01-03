package com.dx.service;

import com.dx.common.Result;
import com.dx.dto.AssetHotDTO;
import com.dx.dto.FreezeBalanceDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChainAssetsService {



    public Result<List<AssetHotDTO>> getHotwalletBalance(Integer type,Integer id) {
        Result<List<AssetHotDTO>> result = new Result<>();

        AssetHotDTO assetHotDTO = new AssetHotDTO();
        assetHotDTO.setBalance(new BigDecimal("100"));
        assetHotDTO.setId(1);
        assetHotDTO.setCoinCode("TRX");
        assetHotDTO.setCoinName("TRX");
        List<AssetHotDTO> list = new ArrayList<>();
        list.add(assetHotDTO);

        result.setResult(list);
        return result;
    }

    public Result freezeBalance(FreezeBalanceDTO dto) {
        Result<Object> result = new Result<>();
        // TODO: 2024/1/2 实现异步冷却逻辑
        result.setMessage("操作成功");
        return result;

    }
}
