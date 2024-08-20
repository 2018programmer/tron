package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.Result;
import com.dx.pojo.dto.*;
import com.dx.pojo.param.*;
import com.dx.service.PoolAddressService;
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
    private PoolAddressService poolService;

    /**
     * 获取地址池的地址明细
     */
    @PostMapping ("/info/list/get")
    public Result<IPage<PoolAddressDTO>> getPoolAddress(@RequestBody QueryPoolAddressParam vo){
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
    public Result<IPage<CoinManageDTO>> getPoolManage(GetPoolManageParam vo){
        return poolService.getPoolManage(vo.getNetName(),vo.getPageNum(),vo.getPageSize());
    }

    /**
     * 修改归集阀值
     */
    @PostMapping("/manage/update")
    public Result updatePoolManage(@RequestBody UpdatePoolManageParam vo){
        return poolService.updatePoolManage(vo);
    }

    /**
     * 获取归集数量(用余额的数量)
     */
    @GetMapping("/gather-num/get")
    public Result<GetGatherNumDTO> getGatherNum(@NotNull String netName){
        return poolService.getGatherNum(netName);
    }

    /**
     * 获取用户对应地址
     * @return
     */
    @PostMapping("/user-address/get")
    public Result getUserAddress( @Validated  @RequestBody GetUserAddressParam vo){
        return poolService.matchUserAddress(vo);
    }

    /**
     * 获取扫码支付的地址
     * @return
     */
    @PostMapping("/scan-address/get")
    public Result<List<GetScanAddressDTO>> getScanAddress(){
        return poolService.getScanAddress();
    }
    /**
     * 检验地址是否在地址池中 type 1:只检验地址格式是否正确 2:都检验
     */
    @GetMapping("/address/verify")
    public Result<VerifyAddressDTO> verifyAddress(@NotNull String address,@NotNull String netName, Integer type){
        return poolService.verifyAddress(address,netName,type);
    }
    /**
     * 解除绑定地址(删除地址)
     */
    @PostMapping("/address/unbind")
    public Result deleteAddress(@RequestBody UnbindAddressParam vo){
        return poolService.deleteAddress(vo);
    }
    /**
     * 解除绑定地址(解除绑定关系)
     */
    @PostMapping("/address/unbind-delete")
    public Result unbindAddress(@RequestBody UnbindAddressParam vo){
        return poolService.unbindAddress(vo);
    }
}