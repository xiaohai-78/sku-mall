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

    /**
     * 获取点卡列表，支持筛选和分页。
     * GET /api/cards
     * @param name 点卡名称 (可选，模糊匹配)
     * @param code 点卡编号 (可选，模糊匹配)
     * @param status 点卡状态 ("on"或"off"，可选)
     * @param dateFrom 有效期开始日期 (YYYY-MM-DD，可选)
     * @param dateTo 有效期结束日期 (YYYY-MM-DD，可选)
     * @param page 页码 (默认1)
     * @param pageSize 每页大小 (默认10)
     * @return 包含点卡列表和总数的响应对象
     */
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

    /**
     * 创建新点卡。
     * POST /api/cards
     * @param card 点卡对象，包含名称、编号、面值、价格、状态、有效期等信息。
     * @return 包含创建成功的点卡信息的响应对象。
     */
    @PostMapping
    public GlobalResponse<Card> createCard(@RequestBody Card card) {
        logger.info("POST /api/cards - Card: {}", card);
        Card createdCard = cardService.createCard(card);
        return new GlobalResponse<>(createdCard);
    }

    /**
     * 更新现有接口。
     * PUT /api/cards/{id}
     * @param id 点卡ID
     * @param card 包含要更新字段的点卡对象
     * @return 包含更新后的点卡信息的响应对象；如果点卡不存在，返回404错误。
     */
    @PutMapping("/{id}")
    public GlobalResponse<Card> updateCard(@PathVariable Long id, @RequestBody Card card) {
        logger.info("PUT /api/cards/{} - Card: {}", id, card);
        Optional<Card> updatedCard = cardService.updateCard(id, card);
        return updatedCard.map(GlobalResponse::new)
                .orElseGet(() -> new GlobalResponse<>(404, "Card not found", null));
    }

    /**
     * 删除点卡。
     * DELETE /api/cards/{id}
     * @param id 点卡ID
     * @return 空响应对象表示成功；如果点卡不存在，返回404错误。
     */
    @DeleteMapping("/{id}")
    public GlobalResponse<Void> deleteCard(@PathVariable Long id) {
        logger.info("DELETE /api/cards/{}", id);
        boolean deleted = cardService.deleteCard(id);
        if (deleted) {
            // Also remove SKUs associated with this card (for in-memory simplicity)
            dataStore.getCardSkus().remove(cardService.getCardById(id).map(Card::getCode).orElse("INVALID"));
            return new GlobalResponse<>(null);
        } else {
            return new GlobalResponse<>(404, "Card not found", null);
        }
    }

    /**
     * 更新点卡状态 (上架/下架)。
     * PATCH /api/cards/{id}/status
     * @param id 点卡ID
     * @param card 包含新状态的Card对象 (status字段)
     * @return 包含更新后的点卡信息的响应对象；如果点卡不存在，返回404错误。
     */
    @PatchMapping("/{id}/status")
    public GlobalResponse<Card> updateCardStatus(@PathVariable Long id, @RequestBody Card card) {
        logger.info("PATCH /api/cards/{}/status - Status: {}", id, card.getStatus());
        Optional<Card> updatedCard = cardService.updateCardStatus(id, card.getStatus());
        return updatedCard.map(GlobalResponse::new)
                .orElseGet(() -> new GlobalResponse<>(404, "Card not found", null));
    }
} 