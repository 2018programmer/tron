package com.dx.pojo.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GetGatherDetailsDTO implements Serializable {

    /**
     * 主网名称
     */
    private String netName;
    /**
     * 费币种名称
     */
    private String feeName;
    /**
     * 矿工费用
     */
    private BigDecimal feeAmount;
    /**
     * 已归集k:币种  v:数量
     */
    private List<GatherTotalDTO> gatherList;

    /**
     * 明细列表
     */
    private IPage<GetGatherDetailDTO> details;
}
