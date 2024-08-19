package com.dx.pojo.param;

import lombok.Data;

import java.io.Serializable;

@Data
public class UnbindAddressParam implements Serializable {
    /**
     * 地址
     */
    private String address;
}
