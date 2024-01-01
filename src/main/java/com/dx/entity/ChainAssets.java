package com.dx.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChainAssets {

    private Integer id;
    /**
     * 地址
     */
    private String address;
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 币种编码
     */
    private String coinCode;
    /**
     * 余额
     */
    private BigDecimal balance;


}
