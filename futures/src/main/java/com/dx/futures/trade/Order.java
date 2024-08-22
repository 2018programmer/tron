package com.dx.futures.trade;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
 public class Order {
    private String id;
    private String symbol;
    private String type; // ORDER_TYPE_BUY 或 ORDER_TYPE_SELL
    private double amount;
    private double price;
    private String status; // ORDER_STATUS_OPEN, ORDER_STATUS_FILLED, ORDER_STATUS_PARTIALLY_FILLED, ORDER_STATUS_CANCELLED
    private long timestamp;
    private boolean systemTradable; // 是否可以系统级别交易

    public Order(String symbol, String type, double amount, double price, String status, boolean systemTradable) {
        this.symbol = symbol;
        this.type = type;
        this.amount = amount;
        this.price = price;
        this.status = status;
        this.systemTradable = systemTradable;
    }

    public Order() {
    }

    public String getId() {
        return id;
    }

    public boolean isSystemTradable() {
        return systemTradable;
    }

    public void setSystemTradable(boolean systemTradable) {
        this.systemTradable = systemTradable;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}