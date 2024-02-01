package com.dx.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class HotWalletExpensesVO implements Serializable {
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 收款地址
     */
    private String address;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 数额
     */
    private BigDecimal amount;
    /**
     * 订单号
     */
    private String orderId;
}
