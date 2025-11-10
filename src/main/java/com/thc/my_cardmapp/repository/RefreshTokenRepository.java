package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 토큰으로 조회
    Optional<RefreshToken> findByToken(String token);

    // 유효한 토큰 조회 (토큰 + 만료일 체크)
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.token = :token " +
           "AND rt.expiryDate > :now")
    Optional<RefreshToken> findByTokenAndExpiryDateAfter(@Param("token") String token,
                                                          @Param("now") LocalDateTime now);

    // 사용자의 최신 토큰 1개 조회
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.user.id = :userId " +
           "ORDER BY rt.createdAt DESC")
    Optional<RefreshToken> findByUserId(@Param("userId") Long userId);

    // 사용자의 모든 토큰 조회 (최신순)
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.user.id = :userId " +
           "ORDER BY rt.createdAt DESC")
    List<RefreshToken> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 사용자의 유효한 토큰만 조회
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.user.id = :userId " +
           "AND rt.expiryDate > :now " +
           "ORDER BY rt.createdAt DESC")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId,
                                                @Param("now") LocalDateTime now);

    // 사용자의 모든 토큰 삭제 (로그아웃)
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // 만료된 토큰 삭제 (배치 작업용)
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteByExpiryDateBefore(@Param("now") LocalDateTime now);

    // Alias for deleteByExpiryDateBefore
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

    // 토큰 존재 여부 확인
    boolean existsByToken(String token);

    // 특정 사용자의 유효한 토큰 개수 (다중 기기 로그인 제한용)
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
           "WHERE rt.user.id = :userId " +
           "AND rt.expiryDate > :now")
    long countValidTokensByUserId(@Param("userId") Long userId,
                                   @Param("now") LocalDateTime now);
}
