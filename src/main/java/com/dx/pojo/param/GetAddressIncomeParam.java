package com.dx.pojo.param;

import com.dx.common.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetAddressIncomeParam extends PageVO implements Serializable {

    /**
     * 地址
     */
    private String address;

    /**
     * 订单
     */

    private String serial;

    /**
     * 币种名称
     */
    private String coinName;


    /**
     * 有效性 0无效 1有效
     */
    private Integer effective;

}