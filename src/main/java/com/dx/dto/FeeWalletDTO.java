package com.dx.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FeeWalletDTO implements Serializable {

    private Integer id;
    /**
     * 地址
     */
    private String address;
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 余额
     */
    private String balance;
    /**
     * 启动状态 1:启用0:停用
     */
    private Integer runningStatus;
}
