package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.PageVO;
import com.dx.common.Result;
import com.dx.pojo.dto.CoinDTO;
import com.dx.task.BalanceJob;
import com.dx.task.GatherJob;
import com.dx.task.MonitorJob;
import com.dx.pojo.vo.UpdateMinNumVO;
import com.dx.service.ChainCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 币种接口
 */
@RestController
@RequestMapping("/coin")
public class ChainCoinController {

    @Autowired
    private ChainCoinService chainCoinService;

    @Autowired
    private MonitorJob balanceJob;

    /**
     * 获取币种列表
     */
    @GetMapping("/list/get")
    public Result<IPage<CoinDTO>> getCoins(PageVO vo){
        return chainCoinService.getCoins(vo.getPageNum(),vo.getPageSize());
    }

    /**
     * 修改最小收款数
     */
    @PostMapping("/min-num/update")
    public Result updateMinNum(@RequestBody UpdateMinNumVO vo){
        return chainCoinService.updateMinNum(vo);
    }

    @PostMapping("/getm")
    public void  getm(){
        balanceJob.monitorTransferTRON();
    }
}
