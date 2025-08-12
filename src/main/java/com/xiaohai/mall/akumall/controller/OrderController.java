package com.xiaohai.mall.akumall.controller;

import com.xiaohai.mall.akumall.model.Order;
import com.xiaohai.mall.akumall.service.GlobalResponse;
import com.xiaohai.mall.akumall.service.ListResponse;
import com.xiaohai.mall.akumall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表，支持筛选和分页。
     * GET /api/orders
     * @param orderId 订单ID (可选，模糊匹配)
     * @param cardName 点卡名称 (可选，模糊匹配)
     * @param cardCode 点卡编号 (可选，模糊匹配)
     * @param dateFrom 成交时间开始日期 (YYYY-MM-DD，可选)
     * @param dateTo 成交时间结束日期 (YYYY-MM-DD，可选)
     * @param page 页码 (默认1)
     * @param pageSize 每页大小 (默认10)
     * @return 包含订单列表和总数的响应对象。
     */
    @GetMapping
    public GlobalResponse<ListResponse<Order>> getOrders(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String cardCode,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        logger.info("GET /api/orders - orderId: {}, cardName: {}, cardCode: {}, dateFrom: {}, dateTo: {}, page: {}, pageSize: {}",
                orderId, cardName, cardCode, dateFrom, dateTo, page, pageSize);

        List<Order> orders = orderService.searchOrders(orderId, cardName, cardCode, dateFrom, dateTo, page, pageSize);
        int total = orderService.countOrders(orderId, cardName, cardCode, dateFrom, dateTo);
        return new GlobalResponse<>(new ListResponse<>(orders, total));
    }
} 