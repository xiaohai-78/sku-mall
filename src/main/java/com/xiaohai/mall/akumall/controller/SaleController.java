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

    /**
     * 销售点卡并获取 SKU 密钥。
     * POST /api/sales
     * @param request 包含 "cardCode" (要销售的点卡编号) 的请求体。
     * @return 成功返回销售结果 (包括点卡名称、编号、SKU密钥、订单ID和成交时间)；库存不足或点卡不存在时返回错误信息。
     */
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