package com.thc.my_cardmapp.repository;
import com.thc.my_cardmapp.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository; // 구현체 코드가 없는데도 JPA가 작동하게 해줌
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // 카드 코드로 조회 -> (CHILD_MEAL, CULTURE_NURI 등)
    Optional<Card> findByName(String name);

    // 활성화된 카드만 조회
    @Query("SELECT c FROM Card c WHERE c.id IN (SELECT DISTINCT mc.card.id FROM MerchantCard mc)")
    List<Card> findActiveCards();

    // 발급사별 카드 조회
    List<Card> findByIssuer(String issuer);

    /* 카드명 키워드 검색
    * 사용자가 카드 이름을 정확히 모를 때, '아동'만 검색해도 나올 수 있게끔 하는 것임
    * */
    List<Card> findByNameContainingIgnoreCase(String keyword);
}