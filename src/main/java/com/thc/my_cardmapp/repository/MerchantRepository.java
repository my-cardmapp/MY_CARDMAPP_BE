package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.Merchant;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    // 1. 위치 기반 검색 (반경 내 가맹점)
    @Query(value = "SELECT m.*, ST_Distance(m.location, :point) as distance " +
            "FROM merchant m " +
            "WHERE ST_DWithin(m.location, :point, :radius) " +
            "ORDER BY distance",
            nativeQuery = true)
    List<Merchant> findNearbyMerchants(@Param("point") Point point,
                                       @Param("radius") double radius);

    // 2. 카드 타입별 + 위치 기반 검색
    @Query(value = "SELECT DISTINCT m.*, ST_Distance(m.location, :point) as distance " +
            "FROM merchant m " +
            "JOIN merchant_card mc ON m.id = mc.merchant_id " +
            "JOIN card c ON mc.card_id = c.id " +
            "WHERE ST_DWithin(m.location, :point, :radius) " +
            "AND c.name IN :cardNames " +
            "ORDER BY distance",
            nativeQuery = true)
    List<Merchant> findNearbyMerchantsByCards(@Param("point") Point point,
                                              @Param("radius") double radius,
                                              @Param("cardNames") List<String> cardNames);

    // 3. 카테고리별 + 위치 기반 검색
    @Query("SELECT m FROM Merchant m " +
            "WHERE FUNCTION('ST_DWithin', m.location, :point, :radius) = true " +
            "AND m.category.id = :categoryId " +
            "ORDER BY FUNCTION('ST_Distance', m.location, :point)")
    List<Merchant> findNearbyMerchantsByCategory(@Param("point") Point point,
                                                 @Param("radius") double radius,
                                                 @Param("categoryId") Long categoryId);

    // 4. 텍스트 검색 (Full-text search 준비)
    @Query("SELECT m FROM Merchant m WHERE " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Merchant> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 5. 카드별 가맹점 조회 (페이징)
    @Query("SELECT m FROM Merchant m " +
            "JOIN m.merchantCards mc " +
            "WHERE mc.card.id = :cardId")
    Page<Merchant> findByCardId(@Param("cardId") Long cardId, Pageable pageable);

    // 6. 복합 검색 (카드 + 카테고리 + 키워드)
    @Query("SELECT DISTINCT m FROM Merchant m " +
            "JOIN m.merchantCards mc " +
            "WHERE (:cardId IS NULL OR mc.card.id = :cardId) " +
            "AND (:categoryId IS NULL OR m.category.id = :categoryId) " +
            "AND (:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Merchant> findByMultipleFilters(@Param("cardId") Long cardId,
                                         @Param("categoryId") Long categoryId,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);

    // 7. 가맹점 통계용 쿼리
    @Query("SELECT COUNT(DISTINCT m) FROM Merchant m " +
            "JOIN m.merchantCards mc " +
            "WHERE mc.card.id = :cardId")
    Long countMerchantsByCardId(@Param("cardId") Long cardId);

    // 8. 좌표가 있는 가맹점 개수
    long countByLocationIsNotNull();
}