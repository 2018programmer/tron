package com.dx.pojo.param;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetScanAddressParam implements Serializable {

    /**
     * 主网名称
     */
    String netName;
}
