package com.dx.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetNetByNameDTO implements Serializable {
    /**
     * 充值网络确认数
     */
    private Integer rechargeNetConfirmNum;

    /**
     * 最小收款数
     */
    private Integer minNum;

}
