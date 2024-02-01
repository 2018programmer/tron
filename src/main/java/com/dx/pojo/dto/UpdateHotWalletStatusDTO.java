package com.dx.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateHotWalletStatusDTO implements Serializable {

    @NotNull
    private Integer id;
    /**
     * 状态 0禁用 1开启
     */
    @NotNull
    private Integer runningStatus;
}