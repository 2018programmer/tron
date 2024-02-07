package com.dx.pojo.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UpdateMinNumVO implements Serializable {
    @NotNull
    private Integer id;
    /**
     * 最小收款数
     */
    @NotNull
    private BigDecimal minNum;
}
