package com.dx.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageVO implements Serializable {
    /**
     * 页码
     */
    private Integer pageNum=1;
    /**
     * 分页大小
     */
    private Integer pageSize=20;

}
