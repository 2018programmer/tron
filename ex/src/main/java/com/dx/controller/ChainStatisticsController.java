package com.dx.controller;

import com.dx.common.Result;
import com.dx.pojo.dto.GetStatisticsDTO;
import com.dx.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计接口接口
 */
@RestController
@RequestMapping("/statistics")
public class ChainStatisticsController {

    @Autowired
    private StatisticsService statisticsService;


    /**
     * 获取链服务统计
     */
    @GetMapping("/info/get")
    public Result<GetStatisticsDTO> getStatistics(){
        return statisticsService.getStatistics();
    }
}
