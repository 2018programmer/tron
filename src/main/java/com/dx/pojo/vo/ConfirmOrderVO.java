package com.dx.pojo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConfirmOrderVO implements Serializable {
    /**
     * 交易id
     */
    private String txId;
    /**
     * 订单号
     */
    private String serial;
}
