package com.thc.my_cardmapp.controller;

import com.thc.my_cardmapp.domain.RefreshToken;
import com.thc.my_cardmapp.domain.User;
import com.thc.my_cardmapp.dto.auth.LoginRequest;
import com.thc.my_cardmapp.dto.auth.LoginResponse;
import com.thc.my_cardmapp.dto.auth.RefreshTokenRequest;
import com.thc.my_cardmapp.dto.auth.UserResponse;
import com.thc.my_cardmapp.security.JwtTokenProvider;
import com.thc.my_cardmapp.service.RefreshTokenService;
import com.thc.my_cardmapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("로그인 요청 - 이메일: {}", request.getEmail());

        try {
            // 사용자 인증
            User user = userService.authenticate(request.getEmail(), request.getPassword())
                    .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

            // Access Token 생성
            String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

            // Refresh Token 생성
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
            LocalDateTime refreshTokenExpiry = LocalDateTime.now()
                    .plusSeconds(jwtTokenProvider.getRefreshTokenValidity() / 1000);

            // Refresh Token DB에 저장
            refreshTokenService.createOrUpdateRefreshToken(user, refreshToken, refreshTokenExpiry);

            // 응답 생성
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenValidity())
                    .user(UserResponse.from(user))
                    .build();

            log.info("로그인 성공 - 사용자: {}", user.getEmail());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("로그인 실패 - 이메일: {}, 이유: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("로그인 중 오류 발생", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        log.info("토큰 갱신 요청");

        try {
            // Refresh Token 검증
            if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
                log.warn("유효하지 않은 Refresh Token");
                return ResponseEntity.status(401).build();
            }

            // DB에서 Refresh Token 조회
            RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                    .orElseThrow(() -> new IllegalArgumentException("Refresh Token을 찾을 수 없습니다."));

            // Refresh Token 만료 확인
            if (!refreshTokenService.validateRefreshToken(refreshToken)) {
                log.warn("만료된 Refresh Token");
                return ResponseEntity.status(401).build();
            }

            User user = refreshToken.getUser();

            // 새로운 Access Token 생성
            String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

            // 새로운 Refresh Token 생성 (선택사항)
            String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
            LocalDateTime newRefreshTokenExpiry = LocalDateTime.now()
                    .plusSeconds(jwtTokenProvider.getRefreshTokenValidity() / 1000);

            // Refresh Token 업데이트
            refreshTokenService.createOrUpdateRefreshToken(user, newRefreshToken, newRefreshTokenExpiry);

            // 응답 생성
            LoginResponse response = LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenValidity())
                    .user(UserResponse.from(user))
                    .build();

            log.info("토큰 갱신 성공 - 사용자: {}", user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 사용자의 Refresh Token을 삭제하여 로그아웃합니다.")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        log.info("로그아웃 요청");

        try {
            // Refresh Token 검증
            if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
                log.warn("유효하지 않은 Refresh Token");
                return ResponseEntity.status(401).build();
            }

            // User ID 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());

            // Refresh Token 삭제
            refreshTokenService.deleteByUserId(userId);

            log.info("로그아웃 성공 - 사용자 ID: {}", userId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/test")
    @Operation(summary = "테스트", description = "Auth API 작동 테스트")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API is working!");
    }
}
