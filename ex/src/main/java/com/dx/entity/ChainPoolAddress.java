package com.dx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ChainPoolAddress {
    @TableId(value = "id", type = IdType.AUTO)
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
    private Long createTime;
    /**
     * 分配状态
     */
    private Integer isAssigned;
    /**
     * 激活状态
     */
    private Integer isActivated;
    /**
     * 分配类型 0 无 1 商户 2钱包用户 3 商户用户  4  三方临时订单池
     */
    private Integer assignType;
    /**
     * 主网名字
     */
    private String netName;
    /**
     * 分配id
     */
    private String assignedId;
    /**
     * 业务删除 0 未删除 1 已删除
     */
    private Integer isDelete;
}