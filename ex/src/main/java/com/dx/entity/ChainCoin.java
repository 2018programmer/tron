package com.dx.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChainCoin {
    private Integer id;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 币种类型 base 主链币 contract 合约币
     */
    private String coinType;
    /**
     * 币种编码
     */
    private String coinCode;
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 浮点数位
     */
    private Integer floatNum;
    /**
     * 最小收款数
     */
    private BigDecimal minNum;
    /**
     * 自动归集 0:关闭 1:开启
     */
    private Integer autoGather;
    /**
     * 阀值
     */
    private BigDecimal threshold;
    /**
     * 运行状态
     */
    private Integer runningStatus;

}
