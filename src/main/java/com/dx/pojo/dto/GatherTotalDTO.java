package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GatherTotalDTO implements Serializable {

    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 数量
     */
    private BigDecimal amount;

}
