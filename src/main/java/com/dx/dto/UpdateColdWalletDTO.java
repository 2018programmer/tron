package com.dx.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateColdWalletDTO {

    @NotNull
    private Integer id;

    /**
     * 地址
     */
    @NotNull
    private String address;
}
