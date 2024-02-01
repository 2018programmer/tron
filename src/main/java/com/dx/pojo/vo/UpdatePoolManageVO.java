package com.dx.pojo.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UpdatePoolManageVO implements Serializable {
    /**
     * 币种编码
     */
    @NotNull
    private String coinCode;
    /**
     * 自动归集 0:关闭 1:开启
     */
    private Integer autoGather;
    /**
     * 阀值
     */
    private BigDecimal threshold;
}
