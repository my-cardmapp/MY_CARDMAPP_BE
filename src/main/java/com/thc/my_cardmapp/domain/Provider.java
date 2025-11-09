package com.thc.my_cardmapp.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    LOCAL("로컬", "일반 이메일 로그인"),
    KAKAO("카카오", "카카오 소셜 로그인"),
    NAVER("네이버", "네이버 소셜 로그인");

    private final String displayName;
    private final String description;
}
