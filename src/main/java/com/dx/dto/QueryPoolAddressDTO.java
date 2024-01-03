package com.dx.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class QueryPoolAddressDTO implements Serializable {
    /**
     * 主网名称
     */
    @NotNull
    private String netName;
    /**
     * 分配状态 0为分配 1已分配
     */
    private Integer isAssigned;
    /**
     * 激活状态 0 未激活 1已激活
     */
    private Integer isActivated;

    /**
     * 分配类型 1 商户 2用户
     */
    private Integer assignType;
    /**
     * 分配id
     */
    private Integer assignId;
    /**
     * 页码
     */
    private Integer pageNum;
    /**
     * 分页大小
     */
    private Integer pageSize;
}
