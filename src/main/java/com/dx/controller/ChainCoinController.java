package com.dx.controller;

import com.dx.common.Result;
import com.dx.dto.CoinDTO;
import com.dx.dto.UpdateMinNumDTO;
import com.dx.service.ChainCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 币种接口
 */
@RestController
@RequestMapping("/coin")
public class ChainCoinController {

    @Autowired
    private ChainCoinService chainCoinService;

    /**
     * 获取币种列表  (缺少分页)
     */
    @GetMapping("/list/get")
    public Result<List<CoinDTO>> getCoins(){
        return chainCoinService.getCoins();
    }

    /**
     * 修改最小收款数
     */
    @PostMapping("/min-num/update")
    public Result updateMinNum(UpdateMinNumDTO dto){
        return chainCoinService.updateMinNum(dto);
    }
}
