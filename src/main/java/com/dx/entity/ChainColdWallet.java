package com.dx.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChainColdWallet implements Serializable {

    private Integer id;
    /**
     * 主网名称 唯一
     */
    private String netName;
    /**
     * 地址
     */
    private String address;
    /**
     * 更新时间
     */
    private Date updateTime;
}
