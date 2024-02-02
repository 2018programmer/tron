package com.dx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChainAssets {
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
     * 币种编码
     */
    private String coinCode;
    /**
     * 资产类型 1 热钱包 2 地址池
     */
    private Integer assetType;
    /**
     * 币种名称
     */
    private String coinName;
    /**
     * 余额
     */
    private BigDecimal balance;


}
