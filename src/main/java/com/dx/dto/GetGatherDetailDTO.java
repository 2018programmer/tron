package com.dx.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GetGatherDetailDTO {
    private Integer id;
    /**
     * 任务id
     */
    private Integer taskId;

    /**
     * 交易id
     */
    private String txId;
    /**
     * 归集地址
     */
    private String gatherAddress;
    /**
     * 矿工费地址
     */
    private String feeAddress;

    /**
     * 矿工费币种单位
     */
    private String feeCoinName;
    /**
     * 矿工费数量
     */
    private BigDecimal feeAmount;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 归集数量
     */
    private BigDecimal amount;
    /**
     * 阶段 1 转出矿工费 2回收数字币
     */
    private Integer gatherStage;
    /**
     * 状态 0未开始 1进行中 2失败待重试 3已完成
     */
    private Integer gatherStatus;
    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 完成
     */
    private Long finishTime;
    /**
     * 耗时
     */
    private String totalTime;
}
