package com.thc.my_cardmapp.service;

import com.thc.my_cardmapp.domain.Card;
import com.thc.my_cardmapp.repository.CardRepository;
import com.thc.my_cardmapp.repository.MerchantCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final MerchantCardRepository merchantCardRepository;

    /**
     * 모든 활성 카드 조회
     */
    @Cacheable(value = "activeCards", cacheManager = "cacheManager")
    public List<Card> getActiveCards() {
        log.debug("활성 카드 목록 조회");
        return cardRepository.findActiveCards();
    }

    /**
     * 카드 상세 정보 조회
     */
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다. ID: " + cardId));
    }

    /**
     * 카드 이름으로 조회
     */
    public Card getCardByName(String name) {
        return cardRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다. 이름: " + name));
    }

    /**
     * 카드별 가맹점 수 통계
     */
    @Cacheable(value = "cardStatistics", key = "#cardId")
    public Map<String, Object> getCardStatistics(Long cardId) {
        Card card = getCardById(cardId);
        Long merchantCount = merchantCardRepository.countByCardId(cardId);

        return Map.of(
                "card", card,
                "merchantCount", merchantCount,
                "isActive", merchantCount > 0
        );
    }

    /**
     * 카드 검색
     */
    public List<Card> searchCards(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return cardRepository.findAll();
        }
        return cardRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * 새 카드 등록 (관리자 기능)
     */
    @Transactional
    public Card createCard(Card card) {
        log.info("새 카드 등록: {}", card.getName());

        // 중복 체크
        if (cardRepository.findByName(card.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카드명입니다: " + card.getName());
        }

        return cardRepository.save(card);
    }
}