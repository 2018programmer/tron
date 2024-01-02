package com.dx.controller;

import com.dx.common.Result;
import com.dx.dto.CoinDTO;
import com.dx.service.ChainCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coin")
public class ChainCoinController {

    @Autowired
    private ChainCoinService chainCoinService;

    /**
     * 获取币种列表
     */
    @GetMapping("/getCoins")
    public Result<List<CoinDTO>> getCoins(){
        return chainCoinService.getCoins();
    }

    /**
     * 修改最小收款数
     */
    @PostMapping("/updateMinNum")
    public void updateMinNum(){

    }
}
