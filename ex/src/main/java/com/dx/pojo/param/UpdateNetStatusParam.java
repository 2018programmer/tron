package com.dx.pojo.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateNetStatusParam implements Serializable {
    /**
     * 主网名字
     */
    @NotNull
    private String netName;
    /**
     * 即将修改的状态  1：运行 0：关闭
     */
    @NotNull
    private Integer status;
}
