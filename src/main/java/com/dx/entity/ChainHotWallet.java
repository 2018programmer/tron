package com.dx.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ChainHotWallet {

    private Integer id;
    /**
     * 地址
     */
    private String address;
    /**
     * 私钥
     */
    private String privatekey;
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 状态 0禁用 1开启
     */
    private Integer runningStatus;
    /**
     * 主币余额
     */
    private String balance;
    /**
     * 创建时间
     */
    private Date createTime;


}
