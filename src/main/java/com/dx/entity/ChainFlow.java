package com.dx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class ChainFlow implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 流水组id
     */
    private String groupId;
    /**
     * 钱包类型 1 地址池钱包 2 矿工费钱包 3 热钱包 4 冷钱包
     */
    private Integer walletType;

    /**
     * 主网名称
     */
    private String netName;

    /**
     *  钱包地址
     */
    private String address;

    /**
     * 交易id
     */
    private String txId;
    /**
     * 收支类型 0支出 1收入
     */
    private Integer transferType;
    /**
     *  币种名称
     */
    private String coinName;

    /**
     * 数量
     */
    private BigDecimal amount;

    /**
     * 目标地址
     */
    private String targetAddress;

    /**
     * 1充值 2提款 3矿工费 4 归集 5冷却
     */
    private Integer flowWay;

    /**
     * 创建时间
     */
    private Long createTime;
}
