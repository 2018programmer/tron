package com.dx.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class ChainAddressExpenses implements Serializable {

    private Integer id;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 主网名称
     */
    private String netName;

    /**
     * 出款数量
     */
    private BigDecimal amount;
    /**
     * 提款订单号
     */
    private String serial;

    /**
     *  状态 1 等待中 2处理中 3异常 4已完成
     */
    private Integer expensesStatus;
    /**
     * 尝试次数
     */
    private Integer tryTime;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 完成时间
     */
    private Long finishTime;
    /**
     * 热钱包地址
     */
    private String address;
    /**
     * 矿工费
     */
    private BigDecimal fee;
    /**
     * 矿工费币种名称
     */
    private String feeCoinName;
}
