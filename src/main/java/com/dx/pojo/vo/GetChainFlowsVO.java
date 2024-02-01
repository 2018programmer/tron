package com.dx.pojo.vo;

import com.dx.common.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetChainFlowsVO extends PageVO implements Serializable {

    /**
     * 开始时间 时间戳
     */
    private Long beginTime;

    /**
     * 结束时间 时间戳
     */
    private Long endTime;

    /**
     * 钱包类型 1 地址池钱包 2 矿工费钱包 3 热钱包 4 冷钱包
     */
    private Integer walletType;

    /**
     * 收支类型 0支出 1收入
     */
    private Integer transferType;

    /**
     * 1充值 2提款 3矿工费 4 归集 5冷却
     */
    private Integer flowWay;
}
