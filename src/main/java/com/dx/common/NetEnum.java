package com.dx.common;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NetEnum {
    TRON("TRON", "TRX");

    private final String netName;
    private final String baseCoin;

}
