package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PoolAddressDTO implements Serializable {
    /**
     * 地址
     */
    private String address;
    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 分配状态 0为分配 1已分配
     */
    private Integer isAssigned;
    /**
     * 激活状态 0 未激活 1已激活
     */
    private Integer isActivated;

    /**
     * 分配类型 0 无 1 商户 2用户
     */
    private Integer assignType;
    /**
     * 分配id
     */
    private String assignId;
    /**
     * 主网名称
     */
    private String netName;

    /**
     * 余额估值
     */
    private BigDecimal estimateBalance;
}
