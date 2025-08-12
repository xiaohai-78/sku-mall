package com.xiaohai.mall.akumall.controller;

import com.xiaohai.mall.akumall.service.GlobalResponse;
import com.xiaohai.mall.akumall.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private static final Logger logger = LoggerFactory.getLogger(StockController.class);

    @Autowired
    private StockService stockService;

    /**
     * 添加点卡库存 (SKU 密钥)。
     * POST /api/stock
     * @param request 包含 "cardCode" (点卡编号) 和 "skuKey" (SKU 密钥) 的请求体。
     * @return 成功返回消息；如果点卡编号不存在或 SKU 密钥已存在，返回错误信息。
     */
    @PostMapping
    public GlobalResponse<Map<String, String>> addStock(@RequestBody Map<String, String> request) {
        String cardCode = request.get("cardCode");
        String skuKey = request.get("skuKey");

        logger.info("POST /api/stock - cardCode: {}, skuKey: {}", cardCode, skuKey);

        if (cardCode == null || cardCode.isEmpty() || skuKey == null || skuKey.isEmpty()) {
            return new GlobalResponse<>(400, "Card code and SKU key are required", null);
        }

        boolean success = stockService.addSkuToCard(cardCode, skuKey);
        if (success) {
            return new GlobalResponse<>(Map.of("message", "SKU 添加成功"));
        } else {
            // More specific error handling could be implemented in StockService
            // For simplicity, returning a generic error if addSkuToCard returns false
            return new GlobalResponse<>(409, "库存添加失败，点卡编号不存在或 SKU 密钥已存在", null); // 409 Conflict
        }
    }
} 