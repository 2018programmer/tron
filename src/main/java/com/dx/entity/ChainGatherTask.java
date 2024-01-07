package com.dx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class ChainGatherTask {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 任务方式  0 手动 1自动
     */
    private Integer gatherType;

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
}
