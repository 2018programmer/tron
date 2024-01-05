package com.dx.dto;

import lombok.Data;

@Data
public class GetGatherNumDTO {
    /**
     * 地址数量
     */
    private Integer num;

    /**
     * 主网名称
     */
    private String netName;
}
