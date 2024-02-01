package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.Result;
import com.dx.entity.ChainAddressExpenses;
import com.dx.entity.ChainAddressIncome;
import com.dx.pojo.vo.GetAddressExpensesVO;
import com.dx.pojo.vo.GetAddressIncomeVO;
import com.dx.service.ChainTranferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收入出款接口
 */
@RestController
@RequestMapping("/transfer")
public class ChainTransferController {

    @Autowired
    private ChainTranferService transferService;
    /**
     * 获取收款监听列表
     */
    @GetMapping("/income/list/get")
    public Result<IPage<ChainAddressIncome>> getAddressIncome(GetAddressIncomeVO vo){
        return transferService.getAddressIncome(vo);
    }

    /**
     * 获取出款列表
     */
    @GetMapping("/expenses/list/get")
    public Result<IPage<ChainAddressExpenses>> getAddressExpenses(GetAddressExpensesVO vo){
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

}
