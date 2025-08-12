package com.xiaohai.mall.akumall.controller;

import com.xiaohai.mall.akumall.service.GlobalResponse;
import com.xiaohai.mall.akumall.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private static final Logger logger = LoggerFactory.getLogger(SaleController.class);

    @Autowired
    private SaleService saleService;

    @PostMapping
    public GlobalResponse<SaleService.SaleResult> sellCard(@RequestBody Map<String, String> request) {
        String cardCode = request.get("cardCode");

        logger.info("POST /api/sales - cardCode: {}", cardCode);

        if (cardCode == null || cardCode.isEmpty()) {
            return new GlobalResponse<>(400, "Card code is required", null);
        }

        try {
            SaleService.SaleResult result = saleService.sellCard(cardCode);
            return new GlobalResponse<>(result);
        } catch (SaleService.SaleException e) {
            return new GlobalResponse<>(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            return new GlobalResponse<>(500, "销售失败：" + e.getMessage(), null);
        }
    }
} 