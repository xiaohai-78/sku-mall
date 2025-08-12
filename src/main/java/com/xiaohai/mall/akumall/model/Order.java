package com.xiaohai.mall.akumall.model;

import java.time.LocalDateTime;

public class Order {
    private Long id;
    private String orderId;
    private LocalDateTime dealTime;
    private String cardName;
    private String cardCode;

    public Order(Long id, String orderId, LocalDateTime dealTime, String cardName, String cardCode) {
        this.id = id;
        this.orderId = orderId;
        this.dealTime = dealTime;
        this.cardName = cardName;
        this.cardCode = cardCode;
    }

    public Order() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getDealTime() {
        return dealTime;
    }

    public void setDealTime(LocalDateTime dealTime) {
        this.dealTime = dealTime;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    @Override
    public String toString() {
        return "Order{" +
               "id=" + id +
               ", orderId='" + orderId + '\'' +
               ", dealTime=" + dealTime +
               ", cardName='" + cardName + '\'' +
               ", cardCode='" + cardCode + '\'' +
               '}';
    }
} 