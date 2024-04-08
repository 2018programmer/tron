package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.IdVO;
import com.dx.common.Result;
import com.dx.pojo.dto.GetGatherDetailsDTO;
import com.dx.pojo.dto.GetGatherTasksDTO;
import com.dx.service.GatherService;
import com.dx.pojo.param.GetGatherDetailsParam;
import com.dx.pojo.param.GetGatherTasksParam;
import com.dx.pojo.param.ManualGatherParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 归集接口
 */
@RestController
@RequestMapping("/gather")
public class ChainGatherController {

    @Autowired
    private GatherService gatherService;

    /**
     * 主动归集
     * @param vo
     * @return
     */
    @PostMapping("/manual")
    public Result manualGather(@RequestBody ManualGatherParam vo){
        return gatherService.manualGather(vo);
    }

    /**
     * 获取归集任务列表
     * @return
     */
    @GetMapping("/task/list/get")
    public Result<IPage<GetGatherTasksDTO>> getGatherTasks(GetGatherTasksParam vo){
        return gatherService.getGatherTasks(vo);
    }

    /**
     * 获取归集任务明细
     */
    @GetMapping("/task/detail/list/get")
    public Result<GetGatherDetailsDTO> getGatherDetails(GetGatherDetailsParam vo){
        return gatherService.getGatherDetails(vo);
    }

    /**
     * 取消任务
     * @param vo
     * @return
     */
    @PostMapping("/task/cancel")
    public Result cancelGatherTask(@RequestBody IdVO vo){
        return gatherService.cancelGatherTask(vo);
    }
}
