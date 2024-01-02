package com.dx.common;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ResultMsg {
    private int code = 200;
    private String message = "";
}
