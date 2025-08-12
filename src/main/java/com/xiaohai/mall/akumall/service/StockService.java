package com.xiaohai.mall.akumall.service;

import com.xiaohai.mall.akumall.model.Card;
import com.xiaohai.mall.akumall.model.CardSKU;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private CardService cardService;

    /**
     * Adds a new SKU key to the inventory of a specific card.
     * @param cardCode The code of the card to add SKU to.
     * @param skuKey The unique SKU key to add.
     * @return true if SKU was added, false if cardCode not found or skuKey already exists.
     */
    public boolean addSkuToCard(String cardCode, String skuKey) {
        Optional<Card> cardOpt = dataStore.getCards().stream()
                .filter(c -> c.getCode().equals(cardCode))
                .findFirst();

        if (cardOpt.isEmpty()) {
            return false; // Card not found
        }

        Card card = cardOpt.get();
        List<CardSKU> skus = dataStore.getCardSkus().computeIfAbsent(cardCode, k -> new ArrayList<>());

        boolean skuExists = skus.stream().anyMatch(sku -> sku.getKey().equals(skuKey));
        if (skuExists) {
            return false; // SKU already exists
        }

        skus.add(new CardSKU(skuKey, false)); // Add as not sold
        card.setUpdatedAt(LocalDateTime.now()); // Update card's updated time
        return true;
    }

    /**
     * Retrieves all SKUs for a given card code.
     * @param cardCode The code of the card.
     * @return List of CardSKU objects.
     */
    public List<CardSKU> getSkusByCardCode(String cardCode) {
        return dataStore.getCardSkus().getOrDefault(cardCode, new ArrayList<>());
    }
} 