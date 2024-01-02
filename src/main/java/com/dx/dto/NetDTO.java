package com.dx.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class NetDTO implements Serializable {
    /**
     * 主网名称
     */
    private String netName;
    /**
     * logo
     */
    private String logo;

    /**
     * 支持币总数
     */
    private Integer coinNum;
    /**
     * 充值网络确认
     */
    private Integer rechargeNetConfirmNum;
    /**
     * 提款网络确认
     */
    private Integer withdrawNetConfirmNum;

    /**
     * 运行状态  1：运行 0：关闭
     */
    private Integer RunningStatus;
}
