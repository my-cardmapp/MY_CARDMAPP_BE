package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    // 가맹점이 있는 카테고리만 조회
    @Query("SELECT DISTINCT c FROM Category c " +
            "JOIN c.merchants m " +
            "WHERE SIZE(m.merchantCards) > 0")
    List<Category> findActiveCategories();

    // 특정 카드의 인기 카테고리 조회
    @Query("SELECT c, COUNT(m) as merchantCount FROM Category c " +
            "JOIN c.merchants m " +
            "JOIN m.merchantCards mc " +
            "WHERE mc.card.id = :cardId " +
            "GROUP BY c " +
            "ORDER BY merchantCount DESC")
    List<Object[]> findPopularCategoriesByCardId(Long cardId);
}