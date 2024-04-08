package com.dx.pojo.param;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddThirdOrderAddressParam implements Serializable {
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 创建数量
     */
    private Integer num;

}
