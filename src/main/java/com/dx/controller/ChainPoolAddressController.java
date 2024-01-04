package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.Result;
import com.dx.dto.*;
import com.dx.service.ChainPoolAddressService;
import com.dx.vo.GetPoolManageVO;
import com.dx.vo.QueryPoolAddressVO;
import com.dx.vo.UpdatePoolManageVO;
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
    public Result<IPage<PoolAddressDTO>> getPoolAddress(@RequestBody QueryPoolAddressVO vo){
        return poolService.getPoolAddress(vo);
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
    public Result<IPage<CoinManageDTO>> getPoolManage(GetPoolManageVO vo){
        return poolService.getPoolManage(vo.getNetName(),vo.getPageNum(),vo.getPageSize());
    }

    /**
     * 修改归集阀值
     */
    @PostMapping("/manage/update")
    public Result updatePoolManage(@RequestBody UpdatePoolManageVO vo){
        return poolService.updatePoolManage(vo);
    }
}
