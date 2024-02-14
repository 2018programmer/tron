package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GetNetByNameDTO implements Serializable {
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 展示名称
     */
    private String displayName;
    /**
     * 充值网络确认数
     */
    private Integer rechargeNetConfirmNum;

    /**
     * 最小收款数
     */
    private BigDecimal minNum;
    /**
     * 运行状态 0关闭 1开启
     */
    private Integer runningStatus;

}
