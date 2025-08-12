package com.xiaohai.mall.akumall.service;

import com.xiaohai.mall.akumall.model.Card;
import com.xiaohai.mall.akumall.model.CardSKU;
import com.xiaohai.mall.akumall.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class SaleService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private OrderService orderService;

    // 时间格式化器，与前端保持一致 (YYYY-MM-DD HH:mm:ss)
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Random RANDOM = new Random();

    public SaleResult sellCard(String cardCode) {
        Optional<Card> cardOpt = dataStore.getCards().stream()
                .filter(c -> c.getCode().equals(cardCode))
                .findFirst();

        if (cardOpt.isEmpty()) {
            throw new SaleException("点卡编号不存在", 1002);
        }

        Card card = cardOpt.get();
        List<CardSKU> skus = dataStore.getCardSkus().getOrDefault(cardCode, new ArrayList<>());

        Optional<CardSKU> availableSKUOpt = skus.stream().filter(sku -> !sku.isSold()).findFirst();

        if (availableSKUOpt.isEmpty()) {
            throw new SaleException("库存不足", 1001);
        }

        CardSKU soldSKU = availableSKUOpt.get();
        soldSKU.setSold(true);

        LocalDateTime dealTime = LocalDateTime.now();
        card.setUpdatedAt(dealTime); // Update card's updated time

        // Create an order
        String orderId = String.format("ORD-%s-%03d", dealTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), RANDOM.nextInt(1000));
        Order newOrder = new Order(dataStore.getOrderIdCounter().incrementAndGet(), orderId, dealTime, card.getName(), card.getCode());
        orderService.createOrder(newOrder);

        return new SaleResult(card.getName(), card.getCode(), soldSKU.getKey(), orderId, dealTime.format(DATE_TIME_FORMATTER));
    }

    public static class SaleResult {
        private String cardName;
        private String cardCode;
        private String skuKey;
        private String orderId;
        private String dealTime;

        public SaleResult(String cardName, String cardCode, String skuKey, String orderId, String dealTime) {
            this.cardName = cardName;
            this.cardCode = cardCode;
            this.skuKey = skuKey;
            this.orderId = orderId;
            this.dealTime = dealTime;
        }

        public String getCardName() {
            return cardName;
        }

        public String getCardCode() {
            return cardCode;
        }

        public String getSkuKey() {
            return skuKey;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getDealTime() {
            return dealTime;
        }
    }

    public static class SaleException extends RuntimeException {
        private final int code;

        public SaleException(String message, int code) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
} 