package com.dx.pojo.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class GetUserAddressParam implements Serializable {
    /**
     * 类型 1 商户 2钱包用户 3商户用户
     */
    @NotNull(message = "类型不能为空")
    private Integer assignType;
    /**
     * 商户或用户的id
     */
    @NotNull(message = "id不能为空")
    private String assignedId;
    /**
     * 主网名称
     */
    @NotNull(message = "主网名称不能为空")
    private String netName;
}
