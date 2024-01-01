package com.dx.entity;

import lombok.Data;

@Data
public class ChainNet {
    private Integer id;
    /**
     * 主网名称
     */
    private String mainNetName;
    /**
     * 充值网络确认
     */
    private Integer rechargeNetConfirmNum;
    /**
     * 提款网络确认
     */
    private Integer withdrawNetConfirmNum;
    /**
     * 备注
     */
    private String remark;
    /**
     * 运行状态  1：运行 0：关闭
     */
    private Integer RunningStatus;
}
