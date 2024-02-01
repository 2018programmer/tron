package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CoinManageDTO implements Serializable {
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 币种编码
     */
    private String coinCode;
    /**
     * 余额总数
     */
    private BigDecimal totalBalance;
    /**
     * 自动归集 0:关闭 1:开启
     */
    private Integer autoGather;
    /**
     * 阀值
     */
    private BigDecimal threshold;
}
