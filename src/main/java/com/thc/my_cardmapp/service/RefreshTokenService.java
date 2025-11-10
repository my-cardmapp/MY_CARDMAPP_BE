package com.thc.my_cardmapp.service;

import com.thc.my_cardmapp.domain.RefreshToken;
import com.thc.my_cardmapp.domain.User;
import com.thc.my_cardmapp.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Refresh Token 생성 또는 업데이트
     */
    @Transactional
    public RefreshToken createOrUpdateRefreshToken(User user, String token, LocalDateTime expiryDate) {
        log.debug("Refresh Token 생성/업데이트 - 사용자: {}", user.getEmail());

        // 기존 토큰이 있으면 업데이트, 없으면 생성
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(user.getId());

        RefreshToken refreshToken;
        if (existingToken.isPresent()) {
            refreshToken = existingToken.get();
            refreshToken.updateToken(token, expiryDate);
            log.debug("기존 Refresh Token 업데이트");
        } else {
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(token)
                    .expiryDate(expiryDate)
                    .build();
            log.debug("새 Refresh Token 생성");
        }

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Refresh Token으로 토큰 조회
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Refresh Token 유효성 검증
     */
    public boolean validateRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("만료된 Refresh Token - 사용자: {}", refreshToken.getUser().getEmail());
            return false;
        }
        return true;
    }

    /**
     * 사용자의 Refresh Token 삭제 (로그아웃)
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        log.debug("Refresh Token 삭제 - 사용자 ID: {}", userId);
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * 만료된 Refresh Token 삭제
     */
    @Transactional
    public void deleteExpiredTokens() {
        log.info("만료된 Refresh Token 삭제 시작");
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
