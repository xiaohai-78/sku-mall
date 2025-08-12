package com.xiaohai.mall.akumall.service;

import com.xiaohai.mall.akumall.model.Card;
import com.xiaohai.mall.akumall.model.CardSKU;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class CardService {

    @Autowired
    private InMemoryDataStore dataStore;

    // 时间格式化器，与前端保持一致 (YYYY-MM-DD HH:mm:ss)
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Card> getAllCards() {
        return dataStore.getCards().stream()
                .peek(card -> {
                    // 动态计算 stock 字段
                    List<CardSKU> skus = dataStore.getCardSkus().getOrDefault(card.getCode(), new ArrayList<>());
                    long availableStock = skus.stream().filter(sku -> !sku.isSold()).count();
                    // 这里我们不能直接设置 Card 对象的 stock 字段，因为 Card 原始模型没有这个字段
                    // 在返回 DTO 时再添加这个字段。这里只是确保数据是准备好的。
                })
                .collect(Collectors.toList());
    }

    public Optional<Card> getCardById(Long id) {
        return dataStore.getCards().stream()
                .filter(card -> card.getId().equals(id))
                .findFirst();
    }

    public Card createCard(Card card) {
        Long newId = dataStore.getCardIdCounter().incrementAndGet();
        card.setId(newId);
        LocalDateTime now = LocalDateTime.now();
        card.setCreatedAt(now);
        card.setUpdatedAt(now);
        dataStore.getCards().add(0, card); // Add to the beginning to simulate latest first
        return card;
    }

    public Optional<Card> updateCard(Long id, Card updatedCard) {
        Optional<Card> existingCardOpt = getCardById(id);
        if (existingCardOpt.isPresent()) {
            Card existingCard = existingCardOpt.get();
            existingCard.setName(updatedCard.getName() != null ? updatedCard.getName() : existingCard.getName());
            existingCard.setCode(updatedCard.getCode() != null ? updatedCard.getCode() : existingCard.getCode());
            existingCard.setFaceValue(updatedCard.getFaceValue() != null ? updatedCard.getFaceValue() : existingCard.getFaceValue());
            existingCard.setPrice(updatedCard.getPrice() != null ? updatedCard.getPrice() : existingCard.getPrice());
            existingCard.setStatus(updatedCard.getStatus() != null ? updatedCard.getStatus() : existingCard.getStatus());
            existingCard.setValidFrom(updatedCard.getValidFrom() != null ? updatedCard.getValidFrom() : existingCard.getValidFrom());
            existingCard.setValidTo(updatedCard.getValidTo() != null ? updatedCard.getValidTo() : existingCard.getValidTo());
            existingCard.setDescription(updatedCard.getDescription() != null ? updatedCard.getDescription() : existingCard.getDescription());
            existingCard.setUpdatedAt(LocalDateTime.now());
            return Optional.of(existingCard);
        }
        return Optional.empty();
    }

    public boolean deleteCard(Long id) {
        return dataStore.getCards().removeIf(card -> card.getId().equals(id));
    }

    public Optional<Card> updateCardStatus(Long id, String status) {
        Optional<Card> existingCardOpt = getCardById(id);
        if (existingCardOpt.isPresent()) {
            Card existingCard = existingCardOpt.get();
            existingCard.setStatus(status);
            existingCard.setUpdatedAt(LocalDateTime.now());
            return Optional.of(existingCard);
        }
        return Optional.empty();
    }

    // 用于列表查询的过滤和分页方法
    public List<Card> searchCards(String name, String code, String status, String dateFrom, String dateTo, int page, int pageSize) {
        List<Card> filteredCards = dataStore.getCards().stream()
                .filter(card -> (name == null || card.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(card -> (code == null || card.getCode().toLowerCase().contains(code.toLowerCase())))
                .filter(card -> (status == null || status.isEmpty() || card.getStatus().equalsIgnoreCase(status)))
                .filter(card -> {
                    if (dateFrom == null || dateFrom.isEmpty()) return true;
                    // Assuming dateFrom/dateTo are YYYY-MM-DD
                    return !card.getValidFrom().isBefore(java.time.LocalDate.parse(dateFrom));
                })
                .filter(card -> {
                    if (dateTo == null || dateTo.isEmpty()) return true;
                    return !card.getValidTo().isAfter(java.time.LocalDate.parse(dateTo));
                })
                .sorted(Comparator.comparing(Card::getUpdatedAt).reversed()) // 默认按更新时间倒序
                .collect(Collectors.toList());

        // Pagination
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredCards.size());
        if (start < filteredCards.size()) {
            return filteredCards.subList(start, end);
        } else {
            return new ArrayList<>();
        }
    }

    public int countCards(String name, String code, String status, String dateFrom, String dateTo) {
        List<Card> filteredCards = dataStore.getCards().stream()
                .filter(card -> (name == null || card.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(card -> (code == null || card.getCode().toLowerCase().contains(code.toLowerCase())))
                .filter(card -> (status == null || status.isEmpty() || card.getStatus().equalsIgnoreCase(status)))
                .filter(card -> {
                    if (dateFrom == null || dateFrom.isEmpty()) return true;
                    return !card.getValidFrom().isBefore(java.time.LocalDate.parse(dateFrom));
                })
                .filter(card -> {
                    if (dateTo == null || dateTo.isEmpty()) return true;
                    return !card.getValidTo().isAfter(java.time.LocalDate.parse(dateTo));
                })
                .collect(Collectors.toList());
        return filteredCards.size();
    }

    // Helper for frontend Card DTO with stock count
    public static class CardDTO extends Card {
        private int stock;

        public CardDTO(Card card, int stock) {
            super(card.getId(), card.getName(), card.getCode(), card.getFaceValue(), card.getPrice(),
                    card.getStatus(), card.getValidFrom(), card.getValidTo(), card.getDescription(),
                    card.getCreatedAt(), card.getUpdatedAt());
            this.stock = stock;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }
    }
} 