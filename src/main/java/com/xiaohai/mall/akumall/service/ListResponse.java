package com.xiaohai.mall.akumall.service;

import java.util.List;

public class ListResponse<T> {
    private List<T> list;
    private int total;

    public ListResponse(List<T> list, int total) {
        this.list = list;
        this.total = total;
    }

    public ListResponse() {
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
} 