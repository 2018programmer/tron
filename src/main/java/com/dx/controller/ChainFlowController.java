package com.dx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dx.common.Result;
import com.dx.entity.ChainFlow;
import com.dx.mapper.ChainFlowMapper;
import com.dx.service.ChainFlowService;
import com.dx.vo.GetChainFlowsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流水接口
 */
@RestController
@RequestMapping("/flow")
public class ChainFlowController {


    @Autowired
    private ChainFlowService flowService;
    /**
     * 获取流水
     * @param vo
     */
    @GetMapping("/list/get")
    public Result<IPage<ChainFlow>> getChainFlows(GetChainFlowsVO vo){
        return flowService.getChainFlows(vo);
    }
}
