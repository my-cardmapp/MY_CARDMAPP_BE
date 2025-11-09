package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.RouteCache;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteCacheRepository extends JpaRepository<RouteCache, Long> {

    // 경로 캐시 조회 (출발지, 도착지로)
    @Query("SELECT rc FROM RouteCache rc " +
            "WHERE FUNCTION('ST_Equals', rc.origin, :origin) = true " +
            "AND FUNCTION('ST_Equals', rc.dest, :dest) = true " +
            "AND rc.expiresAt > :now")
    Optional<RouteCache> findValidCache(@Param("origin") Point origin,
                                        @Param("dest") Point dest,
                                        @Param("now") LocalDateTime now);

    // 만료된 캐시 삭제
    @Modifying
    @Query("DELETE FROM RouteCache rc WHERE rc.expiresAt < :now")
    void deleteExpiredCaches(@Param("now") LocalDateTime now);

    // 특정 가맹점 관련 캐시 조회
    @Query("SELECT rc FROM RouteCache rc " +
            "WHERE rc.merchant.id = :merchantId " +
            "AND rc.expiresAt > :now")
    List<RouteCache> findByMerchantId(@Param("merchantId") Long merchantId,
                                      @Param("now") LocalDateTime now);
}