package com.xiaohai.mall.akumall.service;

public class GlobalResponse<T> {
    private int code;
    private String message;
    private T data;

    public GlobalResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public GlobalResponse(T data) {
        this.code = 0;
        this.message = "ok";
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
} 