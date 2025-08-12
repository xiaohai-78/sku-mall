package com.xiaohai.mall.akumall.service;

import com.xiaohai.mall.akumall.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class OrderService {

    @Autowired
    private InMemoryDataStore dataStore;

    // 时间格式化器，与前端保持一致 (YYYY-MM-DD HH:mm:ss)
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Order> searchOrders(String orderId, String cardName, String cardCode, String dateFrom, String dateTo, int page, int pageSize) {
        List<Order> filteredOrders = dataStore.getOrders().stream()
                .filter(order -> (orderId == null || order.getOrderId().toLowerCase().contains(orderId.toLowerCase())))
                .filter(order -> (cardName == null || order.getCardName().toLowerCase().contains(cardName.toLowerCase())))
                .filter(order -> (cardCode == null || order.getCardCode().toLowerCase().contains(cardCode.toLowerCase())))
                .filter(order -> {
                    if (dateFrom == null || dateFrom.isEmpty()) return true;
                    // dateFrom should be YYYY-MM-DD
                    LocalDateTime fromDateTime = LocalDateTime.parse(dateFrom + " 00:00:00", DATE_TIME_FORMATTER);
                    return !order.getDealTime().isBefore(fromDateTime);
                })
                .filter(order -> {
                    if (dateTo == null || dateTo.isEmpty()) return true;
                    // dateTo should be YYYY-MM-DD
                    LocalDateTime toDateTime = LocalDateTime.parse(dateTo + " 23:59:59", DATE_TIME_FORMATTER);
                    return !order.getDealTime().isAfter(toDateTime);
                })
                .sorted(Comparator.comparing(Order::getDealTime).reversed()) // 默认按成交时间倒序
                .collect(Collectors.toList());

        // Pagination
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredOrders.size());
        if (start < filteredOrders.size()) {
            return filteredOrders.subList(start, end);
        } else {
            return new ArrayList<>();
        }
    }

    public int countOrders(String orderId, String cardName, String cardCode, String dateFrom, String dateTo) {
        List<Order> filteredOrders = dataStore.getOrders().stream()
                .filter(order -> (orderId == null || order.getOrderId().toLowerCase().contains(orderId.toLowerCase())))
                .filter(order -> (cardName == null || order.getCardName().toLowerCase().contains(cardName.toLowerCase())))
                .filter(order -> (cardCode == null || order.getCardCode().toLowerCase().contains(cardCode.toLowerCase())))
                .filter(order -> {
                    if (dateFrom == null || dateFrom.isEmpty()) return true;
                    LocalDateTime fromDateTime = LocalDateTime.parse(dateFrom + " 00:00:00", DATE_TIME_FORMATTER);
                    return !order.getDealTime().isBefore(fromDateTime);
                })
                .filter(order -> {
                    if (dateTo == null || dateTo.isEmpty()) return true;
                    LocalDateTime toDateTime = LocalDateTime.parse(dateTo + " 23:59:59", DATE_TIME_FORMATTER);
                    return !order.getDealTime().isAfter(toDateTime);
                })
                .collect(Collectors.toList());
        return filteredOrders.size();
    }

    public Order createOrder(Order order) {
        Long newId = dataStore.getOrderIdCounter().incrementAndGet();
        order.setId(newId);
        dataStore.getOrders().add(0, order); // Add to the beginning
        return order;
    }
} 