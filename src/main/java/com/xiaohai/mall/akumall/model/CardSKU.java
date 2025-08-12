package com.xiaohai.mall.akumall.model;

public class CardSKU {
    private String key;
    private boolean sold;

    public CardSKU(String key, boolean sold) {
        this.key = key;
        this.sold = sold;
    }

    public CardSKU() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    @Override
    public String toString() {
        return "CardSKU{" +
               "key='" + key + '\'' +
               ", sold=" + sold +
               '}';
    }
} 