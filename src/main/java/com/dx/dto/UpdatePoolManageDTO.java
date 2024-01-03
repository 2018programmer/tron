package com.dx.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UpdatePoolManageDTO implements Serializable {
    /**
     * 币种编码
     */
    private String coinCode;
    /**
     * 阀值
     */
    private BigDecimal threshold;
}
