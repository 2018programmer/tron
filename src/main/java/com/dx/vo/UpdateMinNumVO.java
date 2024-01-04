package com.dx.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateMinNumVO implements Serializable {
    @NotNull
    private Integer id;
    /**
     * 最小收款数
     */
    @NotNull
    private Integer minNum;
}
