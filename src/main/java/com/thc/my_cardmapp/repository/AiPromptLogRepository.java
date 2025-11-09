package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.AiPromptLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiPromptLogRepository extends JpaRepository<AiPromptLog, Long> {

    // 사용자별 AI 사용 기록
    Page<AiPromptLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 특정 기간 동안의 토큰 사용량 통계
    @Query("SELECT SUM(apl.responseTokens) FROM AiPromptLog apl " +
            "WHERE apl.user.id = :userId " +
            "AND apl.createdAt BETWEEN :startDate AND :endDate")
    Long calculateTokenUsage(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // 상태별 로그 조회
    List<AiPromptLog> findByStatus(String status);
}