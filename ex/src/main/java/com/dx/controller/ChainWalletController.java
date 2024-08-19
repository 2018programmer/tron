package com.dx.controller;

import com.dx.common.Result;
import com.dx.pojo.dto.*;
import com.dx.entity.ChainColdWallet;
import com.dx.service.WalletService;
import com.dx.pojo.param.HotWalletExpensesParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包接口
 */
@RestController
@RequestMapping("/wallet")
public class ChainWalletController {
    @Autowired
    private WalletService walletService;
    /**
     * 获取热钱包列表
     */
    @GetMapping("/hot-wallet/list/get")
    public Result<List<HotWalletDTO>> getHotWallet(String netName){
        return walletService.getHotWallet(netName);
    }

    /**
     * 更新热钱包状态
     */
    @PostMapping("/hot-wallet/stauts/update")
    public Result updateHotWalletStatus(@RequestBody UpdateHotWalletStatusDTO dto){
        return walletService.updateHotWalletStatus(dto);
    }

    /**
     * 更新矿工费钱包钱包状态
     */
    @PostMapping("/fee-wallet/stauts/update")
    public Result updateFeeWalletStatus(@RequestBody UpdateHotWalletStatusDTO dto){
        return walletService.updateFeeWalletStatus(dto);
    }

    /**
     * 获取冷钱包列表
     *
     */
    @GetMapping("/cold-wallet/list/get")
    public Result<List<ChainColdWallet>> getColdWallets(){
        return walletService.getColdWallets();
    }

    /**
     * 修改冷钱包地址
     */
    @PostMapping("/cold-wallet/update")
    public Result updateColdWallet(@RequestBody UpdateColdWalletDTO dto){
        return walletService.updateColdWallet(dto);
    }

    /**
     * 新增热钱包
     * @return
     */
    @PostMapping("/hot-wallet/add")
    public Result addHotWallet(@RequestBody AddWalletDTO dto){
        return walletService.addHotWallet(dto);
    }

    /**
     * 新增矿工费钱包
     * @return
     */
    @PostMapping("/fee-wallet/add")
    public Result addFeeWallet(@RequestBody AddWalletDTO dto){
        return walletService.addFeeWallet(dto);
    }

    /**
     * 获取矿工费钱包列表
     * @return
     */
    @GetMapping("/fee-wallet/list/get")
    public Result<List<FeeWalletDTO>>  getFeeWallets(String netName){
        return walletService.getFeeWallets(netName);
    }

    /**
     * 出款
     * @return
     */
    @PostMapping("/hot-wallet/expenses")
    public Result<HotWalletExpensesDTO> hotWalletExpenses(@RequestBody HotWalletExpensesParam vo){
        return walletService.hotWalletExpenses(vo);
    }

    /**
     * 检查热钱包余额
     */
    @GetMapping("/hot-wallet/balance/vaild")
    public Result vaildHotWalletBalance(BigDecimal amount,String netName,String coinName){
        return walletService.vaildHotWalletBalance(amount,netName,coinName);
    }

}
