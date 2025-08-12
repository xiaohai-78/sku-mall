package com.xiaohai.mall.akumall.controller;

import com.xiaohai.mall.akumall.model.Card;
import com.xiaohai.mall.akumall.service.CardService;
import com.xiaohai.mall.akumall.service.GlobalResponse;
import com.xiaohai.mall.akumall.service.InMemoryDataStore;
import com.xiaohai.mall.akumall.service.ListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @Autowired
    private CardService cardService;
    @Autowired
    private InMemoryDataStore dataStore; // For SKU count

    @GetMapping
    public GlobalResponse<ListResponse<CardService.CardDTO>> getCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        logger.info("GET /api/cards - name: {}, code: {}, status: {}, dateFrom: {}, dateTo: {}, page: {}, pageSize: {}",
                name, code, status, dateFrom, dateTo, page, pageSize);

        List<Card> cards = cardService.searchCards(name, code, status, dateFrom, dateTo, page, pageSize);
        int total = cardService.countCards(name, code, status, dateFrom, dateTo);

        // Convert Card to CardDTO to include stock
        List<CardService.CardDTO> cardDTOs = cards.stream()
                .map(card -> {
                    int stock = dataStore.getCardSkus().getOrDefault(card.getCode(), new ArrayList<>()).stream()
                                .filter(sku -> !sku.isSold()).collect(Collectors.toList()).size();
                    return new CardService.CardDTO(card, stock);
                })
                .collect(Collectors.toList());

        return new GlobalResponse<>(new ListResponse<>(cardDTOs, total));
    }

    @PostMapping
    public GlobalResponse<Card> createCard(@RequestBody Card card) {
        logger.info("POST /api/cards - Card: {}", card);
        Card createdCard = cardService.createCard(card);
        return new GlobalResponse<>(createdCard);
    }

    @PutMapping("/{id}")
    public GlobalResponse<Card> updateCard(@PathVariable Long id, @RequestBody Card card) {
        logger.info("PUT /api/cards/{} - Card: {}", id, card);
        Optional<Card> updatedCard = cardService.updateCard(id, card);
        return updatedCard.map(GlobalResponse::new)
                .orElseGet(() -> new GlobalResponse<>(404, "Card not found", null));
    }

    @DeleteMapping("/{id}")
    public GlobalResponse<Void> deleteCard(@PathVariable Long id) {
        logger.info("DELETE /api/cards/{}", id);
        boolean deleted = cardService.deleteCard(id);
        if (deleted) {
            // Also remove SKUs associated with this card
            dataStore.getCardSkus().remove(cardService.getCardById(id).map(Card::getCode).orElse("INVALID"));
            return new GlobalResponse<>(null);
        } else {
            return new GlobalResponse<>(404, "Card not found", null);
        }
    }

    @PatchMapping("/{id}/status")
    public GlobalResponse<Card> updateCardStatus(@PathVariable Long id, @RequestBody Card card) {
        logger.info("PATCH /api/cards/{}/status - Status: {}", id, card.getStatus());
        Optional<Card> updatedCard = cardService.updateCardStatus(id, card.getStatus());
        return updatedCard.map(GlobalResponse::new)
                .orElseGet(() -> new GlobalResponse<>(404, "Card not found", null));
    }
} 