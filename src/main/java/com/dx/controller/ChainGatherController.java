package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.Result;
import com.dx.dto.GetGatherDetailsDTO;
import com.dx.dto.GetGatherTasksDTO;
import com.dx.service.ChainGatherService;
import com.dx.vo.GetGatherDetailsVO;
import com.dx.vo.GetGatherTasksVO;
import com.dx.vo.ManualGatherVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 归集接口
 */
@RestController
@RequestMapping("/gather")
public class ChainGatherController {

    @Autowired
    private ChainGatherService gatherService;

    /**
     * 主动归集
     * @param vo
     * @return
     */
    @PostMapping("/manual")
    public Result manualGather(@RequestBody ManualGatherVO vo){
        return gatherService.manualGather(vo);
    }

    /**
     * 获取归集任务列表
     * @return
     */
    @GetMapping("/task/list/get")
    public Result<IPage<GetGatherTasksDTO>> getGatherTasks(GetGatherTasksVO vo){
        return gatherService.getGatherTasks(vo);
    }

    /**
     * 获取归集任务明细
     */
    @GetMapping("/task/detail/list/get")
    public Result<GetGatherDetailsDTO> getGatherDetails(GetGatherDetailsVO vo){
        return gatherService.getGatherDetails(vo);
    }
}