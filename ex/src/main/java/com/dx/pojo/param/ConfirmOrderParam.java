package com.dx.pojo.param;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConfirmOrderParam implements Serializable {
    /**
     * 交易id
     */
    private String txId;
    /**
     * 订单号
     */
    private String serial;
}
