package com.dx.pojo.vo;

import com.dx.common.PageVO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetPoolManageVO extends PageVO implements Serializable {
    @NotNull
    private String netName;
}
