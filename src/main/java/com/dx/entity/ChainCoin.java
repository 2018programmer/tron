package com.dx.entity;

import lombok.Data;

@Data
public class ChainCoin {
    private Integer id;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 币种编码
     */
    private String coinCode;
    /**
     * 主网名称
     */
    private String mainNetName;
    /**
     * 浮点数位
     */
    private Integer floatNum;
    /**
     * 最小收款数
     */
    private Integer minNum;
    /**
     * 运行状态
     */
    private Integer runningStatus;

}
