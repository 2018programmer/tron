package com.dx.pojo.param;

import com.dx.common.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetGatherDetailsParam extends PageVO implements Serializable {
    /**
     * 任务id
     */
    private Integer id;
}
