package com.dx.pojo.param;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FreezeBalanceParam implements Serializable {
    /**
     * 钱包id
     */
    @NotNull
    private Integer id;
    /**
     * 币种编码列表
     */
    @NotEmpty
    private List<String> coinCodeList;
}
