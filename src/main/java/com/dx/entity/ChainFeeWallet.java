package com.dx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChainFeeWallet {
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
     * 币种名称
     */
    private String coinName;
    /**
     * 余额
     */
    private BigDecimal balance;
    /**
     * 启动状态 1:启用0:停用
     */
    private Integer runningStatus;
}
