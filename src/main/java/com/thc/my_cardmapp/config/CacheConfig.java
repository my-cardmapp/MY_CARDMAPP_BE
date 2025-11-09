package com.thc.my_cardmapp.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Redis 캐시 매니저 설정은 나중에 추가
}