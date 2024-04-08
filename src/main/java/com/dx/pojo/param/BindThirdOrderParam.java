package com.dx.pojo.param;

import lombok.Data;

import java.io.Serializable;

@Data
public class BindThirdOrderParam implements Serializable {
    /**
     * 三方订单号
     */
    private String thirdSerial;
    /**
     * 商户id
     */
    private String merchantId;

    /**
     * 主网
     */
    private String netName;

}
