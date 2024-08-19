package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AssetHotDTO implements Serializable {
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 余额
     */
    private BigDecimal balance;
    /**
     * 钱包id
     */
    private Integer id;
    /**
     * 币种编码
     */
    private String coinCode;
}
