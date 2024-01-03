package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.Result;
import com.dx.dto.*;
import com.dx.service.ChainPoolAddressService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址池接口
 */
@RestController
@RequestMapping("/pool")
public class ChainPoolAddressController {

    @Autowired
    private ChainPoolAddressService poolService;

    /**
     * 获取地址池的地址明细
     */
    @PostMapping ("/info/list/get")
    public Result<List<PoolAddressDTO>> getPoolAddress(@RequestBody QueryPoolAddressDTO dto){
        return poolService.getPoolAddress(dto);
    }
    /**
     * 获取主网与地址数量
     */
    @GetMapping("/net/get")
    public Result<List<PoolManageDTO>> getNets(){
        return poolService.getNets();
    }
    /**
     * 获取地址池管理明细
     */
    @GetMapping("/manage/list/get")
    public Result<IPage<CoinManageDTO>> getPoolManage(@NotNull String netName, @NotNull Integer pageNum, @NotNull Integer pageSize){
        return poolService.getPoolManage(netName,pageNum,pageSize);
    }

    /**
     * 修改归集阀值
     */
    @PostMapping("/manage/update")
    public Result updatePoolManage(@RequestBody UpdatePoolManageDTO dto){
        return poolService.updatePoolManage(dto);
    }
}
