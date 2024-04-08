package com.dx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ChainThirdOrder {
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
     * 三方订单号
     */
    private String serial;
    /**
     * 解绑时间
     */
    private Long unbindTime;
}
