package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // 사용자별 검색 기록 - JPQL 사용
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user.id = :userId ORDER BY sh.createdAt DESC")
    Page<SearchHistory> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // 인기 검색어 (전체)
    @Query("SELECT sh.query, COUNT(sh) as searchCount " +
            "FROM SearchHistory sh " +
            "GROUP BY sh.query " +
            "ORDER BY searchCount DESC")
    List<Object[]> findPopularSearchQueries(Pageable pageable);

    // 사용자의 최근 검색어
    @Query("SELECT DISTINCT sh.query FROM SearchHistory sh " +
            "WHERE sh.user.id = :userId " +
            "ORDER BY sh.createdAt DESC")
    List<String> findRecentQueriesByUserId(@Param("userId") Long userId, Pageable pageable);
}