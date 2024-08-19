package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.Result;
import com.dx.entity.ChainAddressExpenses;
import com.dx.entity.ChainAddressIncome;
import com.dx.pojo.param.ConfirmOrderParam;
import com.dx.pojo.param.GetAddressExpensesParam;
import com.dx.pojo.param.GetAddressIncomeParam;
import com.dx.service.TranferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 收入出款接口
 */
@RestController
@RequestMapping("/transfer")
public class ChainTransferController {

    @Autowired
    private TranferService transferService;
    /**
     * 获取收款监听列表
     */
    @GetMapping("/income/list/get")
    public Result<IPage<ChainAddressIncome>> getAddressIncome(GetAddressIncomeParam vo){
        return transferService.getAddressIncome(vo);
    }

    /**
     * 获取出款列表
     */
    @GetMapping("/expenses/list/get")
    public Result<IPage<ChainAddressExpenses>> getAddressExpenses(GetAddressExpensesParam vo){
        return transferService.getAddressExpenses(vo);
    }

    /**
     * 通过交易id获取交易结果
     * @param txId
     * @return
     */
    @GetMapping("/result/get")
    public Result  getResultByTxId(String netName,String txId){
        return transferService.getResultByTxId(netName,txId);
    }

    /**
     * 确认充值
     * @return
     */
    @PostMapping("/income/confirm")
    public Result  confirmOrder(@RequestBody ConfirmOrderParam vo){
        return transferService.confirmOrder(vo);
    }
}
