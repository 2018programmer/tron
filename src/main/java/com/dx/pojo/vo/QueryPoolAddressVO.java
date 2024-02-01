package com.dx.pojo.vo;

import com.dx.common.PageVO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class QueryPoolAddressVO extends PageVO implements Serializable {
    /**
     * 主网名称
     */
    @NotNull
    private String netName;
    /**
     * 激活状态 0 未激活 1已激活
     */
    private Integer isActivated;
    /**
     * 分配状态 0为分配 1已分配
     */
    private Integer isAssigned;

    /**
     * 分配类型 1 商户 2用户
     */
    private Integer assignType;
    /**
     * 分配id
     */
    private Integer assignId;
}
