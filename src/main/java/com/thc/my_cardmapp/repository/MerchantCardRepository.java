package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.Card;
import com.thc.my_cardmapp.domain.MerchantCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantCardRepository extends JpaRepository<MerchantCard, Long> {

    // 가맹점-카드 관계 확인
    Optional<MerchantCard> findByMerchantIdAndCardId(Long merchantId, Long cardId);

    // 가맹점의 사용 가능한 카드 목록
    @Query("SELECT mc.card FROM MerchantCard mc WHERE mc.merchant.id = :merchantId")
    List<Card> findCardsByMerchantId(Long merchantId);

    // 카드로 사용 가능한 가맹점 수
    @Query("SELECT COUNT(mc) FROM MerchantCard mc WHERE mc.card.id = :cardId")
    Long countByCardId(Long cardId);

    // 특정 카드를 사용할 수 있는 가맹점 목록
    List<MerchantCard> findByCard_Id(Long cardId);
}