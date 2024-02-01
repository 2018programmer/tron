package com.dx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ChainHotWallet {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 地址
     */
    private String address;
    /**
     * 私钥
     */
    private String privateKey;
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 状态 0禁用 1开启
     */
    private Integer runningStatus;
    /**
     * 创建时间
     */
    private Long createTime;


}
