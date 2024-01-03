package com.dx.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateColdWalletDTO implements Serializable {

    @NotNull
    private Integer id;

    /**
     * 地址
     */
    @NotNull
    private String address;
}
