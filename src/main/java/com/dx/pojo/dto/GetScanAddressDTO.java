package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetScanAddressDTO implements Serializable {

    /**
     * 主网名称
     */
    private String netName;
    /**
     * 地址
     */
    private String address;
    /**
     * 主网展示名称
     */
    private String netDispalyName;

}
