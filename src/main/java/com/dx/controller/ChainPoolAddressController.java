package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.Result;
import com.dx.pojo.dto.*;
import com.dx.service.ChainPoolAddressService;
import com.dx.pojo.vo.GetPoolManageVO;
import com.dx.pojo.vo.GetUserAddressVO;
import com.dx.pojo.vo.QueryPoolAddressVO;
import com.dx.pojo.vo.UpdatePoolManageVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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

    /**
     * 获取归集数量(用余额的数量)
     */
    @GetMapping("/gather-num/get")
    public Result<GetGatherNumDTO> getGatherNum(String netName){
        return poolService.getGatherNum(netName);
    }

    /**
     * 获取用户对应地址
     * @param userId
     * @return
     */
    @PostMapping("/user-address/get")
    public Result getUserAddress( @Validated  @RequestBody GetUserAddressVO vo){
        return poolService.matchUserAddress(vo);
    }
    /**
     * 检验地址是否在地址池中 type 1:只检验地址格式是否正确 2:都检验
     */
    @GetMapping("/address/verify")
    public Result<VerifyAddressDTO> verifyAddress(@NotNull String address,@NotNull String netName, Integer type){
        return poolService.verifyAddress(address,netName,type);
    }
}
