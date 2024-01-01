package com.dx.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ChainPoolAddress {

    private Integer id;
    /**
     * 地址 32位16进制
     */
    private String address;
    /**
     * 私钥
     */
    private String privateKey;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 分配状态
     */
    private Integer isAssigned;
    /**
     * 激活状态
     */
    private Integer isActivated;
    /**
     * 主网id
     */
    private Integer MainNetId;
    /**
     * 分配id
     */
    private Integer assignedId;
}
