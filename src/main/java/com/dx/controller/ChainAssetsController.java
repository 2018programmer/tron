package com.dx.controller;

import com.dx.common.Result;
import com.dx.dto.AssetHotDTO;
import com.dx.vo.FreezeBalanceVO;
import com.dx.service.ChainAssetsService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 余额接口
 */
@RestController
@RequestMapping("/assets")
public class ChainAssetsController {

    @Autowired
    private ChainAssetsService chainAssetsService;

    /**
     *  获取热钱包余额列表
     * @param type 1: 主链币 2:合约币 不传 就是所有
     * @param id  钱包id
     */
    @GetMapping("/hot-wallet/list/get")
    public Result<List<AssetHotDTO>>  getHotwalletBalance(Integer type,@NotNull  Integer id){
        return chainAssetsService.getHotwalletBalance(type, id);
    }

    /**
     * 冷却
     */
    @PostMapping("/hot-wallet/balance/freeze")
    public Result freezeBalance(@RequestBody FreezeBalanceVO vo){
        return chainAssetsService.freezeBalance(vo);

    }
}
