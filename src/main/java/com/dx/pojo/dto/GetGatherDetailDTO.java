package com.dx.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

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
     * 常识次数
     */
    private Integer tryTime;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 归集数量
     */
    private BigDecimal amount;
    /**
     * 阶段 0 未开始1 转出矿工费 2回收数字币 3回收成功
     */
    private Integer gatherStage;
    /**
     * 状态 0未开始 1进行中 2失败待重试 3已完成 4已取消
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
