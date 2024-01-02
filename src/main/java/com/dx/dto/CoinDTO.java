package com.dx.dto;

import lombok.Data;

@Data
public class CoinDTO {
    private Integer id;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 币种类型
     */
    private String coinType;
    /**
     * 浮点数位
     */
    private Integer floatNum;
    /**
     * 最小收款数
     */
    private Integer minNum;
    /**
     * 主网名称
     */
    private String mainNetName;
}
