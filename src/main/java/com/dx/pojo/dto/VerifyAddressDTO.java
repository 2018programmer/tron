package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class VerifyAddressDTO implements Serializable {
    /**
     * 分配的id
     */
    private String assignedId;
    /**
     * 分配的类型 1 商户 2钱包用户 3 商户用户
     */
    private Integer assignedType;
    /**
     * 地址是否正确 0不正确 1正确
     */
    private Integer effective;
    /**
     * 是否系统内地址 0不是 1是
     */
    private Integer isAssigned;
}
