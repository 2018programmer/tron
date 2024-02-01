package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CoinDTO implements Serializable {
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
    private String netName;
}
