package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetCurrencyListDTO implements Serializable {
    private Long id;

    private String currency;
    /**
     * 类型 1-法币 2-数字币
     */
    private Integer type;

    private Integer isDefault;

    private Integer num;
}
