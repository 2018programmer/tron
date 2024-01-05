package com.dx.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GetGatherTasksDTO implements Serializable {
    private Integer id;

    /**
     * 任务方式  0 手动 1自动
     */
    private String gatherType;

    /**
     * 归集入钱包地址
     */
    private String address;

    /**
     * 开始时间
     */
    private Long createTime;
    /**
     * 结束时间
     */
    private Long endTime;
    /**
     * 总耗时
     */
    private String totalTime;
    /**
     * 主网时间
     */
    private String netName;
    /**
     * 任务状态  1进行中  5已完成
     */
    private Integer taskStatus;
    /**
     * 归集总数
     */
    private Integer totalNum ;
    /**
     * 完成数量
     */
    private Integer finishNum;
}
