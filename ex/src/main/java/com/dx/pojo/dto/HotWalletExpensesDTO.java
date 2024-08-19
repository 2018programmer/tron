package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HotWalletExpensesDTO implements Serializable {
    /**
     * 发起地址
     */
    private String address;
    /**
     * 交易id
     */
    private String txId;
}
