package com.dx.pojo.param;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class ManualGatherParam implements Serializable {
    /**
     * 主网名称
     */
    @NotNull
    private String netName;
}
