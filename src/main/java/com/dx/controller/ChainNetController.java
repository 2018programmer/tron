package com.dx.controller;

import com.dx.common.Result;
import com.dx.dto.NetDTO;
import com.dx.dto.UpdateNetStatusDTO;
import com.dx.service.ChainNetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/net")
public class ChainNetController {

    @Autowired
    private ChainNetService chainNetService;

    /**
     * 获取主网列表
     */
    @GetMapping("/getNets")
    public Result<List<NetDTO>> getNets(){
        return chainNetService.getChainNet();
    }

    /**
     * 修改主网运行状态
     */
    @PostMapping("/updateNetStatus")
    @ResponseBody
    public Result updateNetStatus(UpdateNetStatusDTO dto){
        return chainNetService.updateNetStatus(dto);
    }


}
