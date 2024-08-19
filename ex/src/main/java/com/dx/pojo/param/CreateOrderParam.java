package com.dx.pojo.param;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderParam {
    /**
     * 币种名称
     */
    private String exchangeCurrency;
    /**
     * 金额
     */
    private BigDecimal exchangeAmount;
    /**
     * 类型 1-商户id 2-用户id
     */
    private Integer type;
    /**
     * 账户id
     */
    private String accountId;
    /**
     * 主网 1-tron 2-eth
     */
    private Integer mainNet;
    /**
     * 转账地址
     */
    private String fromAddr;
    /**
     * 收款地址
     */
    private String toAddr;
    /**
     * 交易id
     */
    private String tranId;
    /**
     * 三方订单号
     */
    private String tradeOrderId;
}
