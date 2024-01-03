package com.dx.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PoolManageDTO implements Serializable {
    /**
     * 主网名称
     */
    private String netName;
    /**
     * 归集状态
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
