package com.dx.pojo.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UpdatePoolManageParam implements Serializable {
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
