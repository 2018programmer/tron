package com.dx.controller;

import com.dx.common.Result;
import com.dx.pojo.dto.GetNetByNameDTO;
import com.dx.pojo.dto.NetDTO;
import com.dx.pojo.vo.UpdateNetStatusVO;
import com.dx.service.NetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 主网接口
 */
@RestController
@RequestMapping("/net")
public class ChainNetController {

    @Autowired
    private NetService netService;

    /**
     * 获取主网列表 runningStatus:1运行中 不传或者其他值为全量
     */
    @GetMapping("/list/get")
    public Result<List<NetDTO>> getNets(Integer runningStatus){
        return netService.getChainNet(runningStatus);
    }

    /**
     * 通过名字获取主网
     * @return
     */
    @GetMapping("/get-by-name")
    public Result<GetNetByNameDTO> getNetByName(String netName, String coinName){
        return netService.getNetByName(netName,coinName);
    }

    /**
     * 通过币种获取主网
     * @return
     */
    @GetMapping("/get-by-coin")
    public Result<List<GetNetByNameDTO>> getNetByCoin(String coinName){
        return netService.getNetByCoin(coinName);
    }

    /**
     * 修改主网运行状态
     */
    @PostMapping("/status/update")
    @ResponseBody
    public Result updateNetStatus(@RequestBody UpdateNetStatusVO vo){
        return netService.updateNetStatus(vo);
    }


}
