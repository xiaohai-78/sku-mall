package com.xiaohai.mall.akumall.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xiaohai.mall.akumall.model.Card;
import com.xiaohai.mall.akumall.model.CardSKU;
import com.xiaohai.mall.akumall.model.Order;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryDataStore {

    private final List<Card> cards = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final Map<String, List<CardSKU>> cardSkus = new HashMap<>(); // cardCode -> List<CardSKU>

    private final AtomicLong cardIdCounter = new AtomicLong(0);
    private final AtomicLong orderIdCounter = new AtomicLong(0);

    @PostConstruct
    public void loadInitialData() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Register module for Java 8 Date and Time API

        try {
            File file = ResourceUtils.getFile("classpath:data.json");
            InitialData initialData = mapper.readValue(file, InitialData.class);

            // Load cards
            for (Card card : initialData.getCards()) {
                cards.add(card);
                cardIdCounter.set(Math.max(cardIdCounter.get(), card.getId()));
            }

            // Load orders
            for (Order order : initialData.getOrders()) {
                orders.add(order);
                orderIdCounter.set(Math.max(orderIdCounter.get(), order.getId()));
            }

            // Load SKUs (assuming initialData also has a way to provide them or we generate them)
            // For now, let's keep SKUs internal to Card objects for simplicity as in frontend mock
            // In a real scenario, CardSKU would be a separate entity.

        } catch (IOException e) {
            System.err.println("Failed to load initial data from data.json: " + e.getMessage());
            // Optionally, populate with default dummy data if file not found/error
        }
    }

    public List<Card> getCards() {
        return cards;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public Map<String, List<CardSKU>> getCardSkus() {
        return cardSkus;
    }

    public AtomicLong getCardIdCounter() {
        return cardIdCounter;
    }

    public AtomicLong getOrderIdCounter() {
        return orderIdCounter;
    }

    // Helper class to map the structure of data.json
    static class InitialData {
        private List<Card> cards;
        private List<Order> orders;

        public List<Card> getCards() {
            return cards;
        }

        public void setCards(List<Card> cards) {
            this.cards = cards;
        }

        public List<Order> getOrders() {
            return orders;
        }

        public void setOrders(List<Order> orders) {
            this.orders = orders;
        }
    }
} 