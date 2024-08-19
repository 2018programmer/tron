package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ContactDTO implements Serializable {

    /**
     * 转账地址
     */
    private String fromAddress;
    /**
     * 接收地址
     */
    private String toAddress;
    /**
     * 币种编码
     */
    private String coinCode;
    /**
     * 数量
     */
    private BigDecimal amount;
    /**
     * 交易id
     */
    private String txId;
}
