package com.dx.controller;

import com.dx.common.Result;
import com.dx.pojo.dto.GetNetByNameDTO;
import com.dx.pojo.dto.NetDTO;
import com.dx.pojo.vo.UpdateNetStatusVO;
import com.dx.service.ChainNetService;
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
    private ChainNetService chainNetService;

    /**
     * 获取主网列表
     */
    @GetMapping("/list/get")
    public Result<List<NetDTO>> getNets(){
        return chainNetService.getChainNet();
    }

    /**
     * 通过名字获取主网
     * @return
     */
    @GetMapping("/get-by-name")
    public Result<GetNetByNameDTO> getNetByName(String netName, String coinName){
        return chainNetService.getNetByName(netName,coinName);
    }

    /**
     * 通过币种获取主网
     * @return
     */
    @GetMapping("/get-by-coin")
    public Result<List<GetNetByNameDTO>> getNetByCoin(String coinName){
        return chainNetService.getNetByCoin(coinName);
    }

    /**
     * 修改主网运行状态
     */
    @PostMapping("/status/update")
    @ResponseBody
    public Result updateNetStatus(@RequestBody UpdateNetStatusVO vo){
        return chainNetService.updateNetStatus(vo);
    }


}
