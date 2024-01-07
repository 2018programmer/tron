package com.dx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 */
@Data
public class ChainAddressIncome {

    @TableId(value = "id", type = IdType.AUTO)
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
     * 交易id
     */
    private String txId;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 数量
     */
    private BigDecimal amount;
    /**
     * 有效性 0无效 1有效
     */
    private Integer effective;
    /**
     * 链确认 0 未确认 1已确认
     */
    private Integer chainConfirm;
    /**
     * 订单
     */
    private String serial;

    /**
     * 监听时间
     */
    private Long createTime;

}
