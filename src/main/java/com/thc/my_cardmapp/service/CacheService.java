package com.thc.my_cardmapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    /**
     * 특정 캐시 삭제
     */
    public void evictCache(String cacheName) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        log.info("캐시 삭제: {}", cacheName);
    }

    /**
     * 특정 키의 캐시 삭제
     */
    public void evictCacheByKey(String cacheName, Object key) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).evict(key);
        log.debug("캐시 키 삭제: {} - {}", cacheName, key);
    }

    /**
     * 모든 캐시 삭제
     */
    public void evictAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName ->
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear()
        );
        log.info("모든 캐시 삭제 완료");
    }

    /**
     * 주기적 캐시 정리 (매일 새벽 3시)
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledCacheEvict() {
        log.info("스케줄된 캐시 정리 시작");
        evictCache("nearbyMerchants");
        evictCache("activeCards");
        evictCache("activeCategories");
    }
}