package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PoolManageDTO implements Serializable {
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 归集状态  0:关闭 1:开启
     */
    private Integer gatherStatus;
    /**
     * 地址总数
     */
    private Integer totalNum;
    /**
     * 剩余总数
     */
    private Integer noAssignedNum;

}
