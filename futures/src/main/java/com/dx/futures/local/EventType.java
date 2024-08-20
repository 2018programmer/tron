package com.dx.futures.local;

public enum EventType {
    SALE, PURCHASE, TIMEOUT_SALESORDER, TIMEOUT_PURCHASEORDER,
    STATS, //修改价格 量等
    STOPPED //交易停止
}