package com.dx.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class ManualGatherVO implements Serializable {
    /**
     * 主网名称
     */
    @NotNull
    private String netName;
}
