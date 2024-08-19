package com.dx.pojo.param;

import com.dx.common.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetAddressExpensesParam extends PageVO implements Serializable {
    /**
     *  状态 1 等待中 2处理中 3异常 4已完成
     */
    private Integer expensesStatus;

    /**
     * 订单号
     */
    private String serial;

}
