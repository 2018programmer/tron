package com.dx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 计数器
 */
@Data
public class HitCounter {
    /**
     * 槽
     */
    @TableId(value = "slot")
    private Integer slot;
    /**
     * 数量
     */
    private Integer cnt;
}
