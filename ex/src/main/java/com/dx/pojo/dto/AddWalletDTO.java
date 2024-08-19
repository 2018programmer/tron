package com.dx.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddWalletDTO implements Serializable {
    @NotNull
    private String netName;
}
