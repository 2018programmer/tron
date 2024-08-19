package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetStatisticsDTO implements Serializable {
    /**
     * 主网数量
     */
    private Integer netCount;
    /**
     * 币种数量
     */
    private Integer coinCount;
    /**
     * 激活地址数量
     */
    private Integer activeAddressCount;
    /**
     * 热钱包数量
     */
    private Integer hotWalletCount;
    /**
     * 出款中数量
     */
    private Integer expensesingCount;
    /**
     * 收款异常数量
     */
    private Integer expensesWrongCount;
    /**
     * 出款队列数量
     */
    private Integer expensesQueueCount;
    /**
     * 归集数量
     */
    private Integer gatherCount;
}
