package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.AiQueryCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiQueryCacheRepository extends JpaRepository<AiQueryCache, Long> {

    // user.id가 아닌 user 필드를 통해 조회
    @Query("SELECT aqc FROM AiQueryCache aqc WHERE aqc.user.id = :userId")
    List<AiQueryCache> findByUserId(Long userId);

    // 최근 캐시 조회
    @Query("SELECT aqc FROM AiQueryCache aqc " +
            "WHERE aqc.user.id = :userId " +
            "ORDER BY aqc.cacheKey DESC")
    List<AiQueryCache> findRecentCachesByUserId(Long userId);
}