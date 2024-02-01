package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class HotWalletDTO implements Serializable {
    private Integer id;
    /**
     * 地址
     */
    private String address;
    /**
     * 主币余额
     */
    private BigDecimal balance;
    /**
     * 支出次数
     */
    private Integer outCount;
    /**
     * 收入次数
     */
    private Integer inCount;
    /**
     * 折合余额
     */
    private BigDecimal convertBalance;
    /**
     * 状态 0禁用 1开启
     */
    private Integer runningStatus;
    /**
     * 创建时间
     */
    private Long createTime;
}
