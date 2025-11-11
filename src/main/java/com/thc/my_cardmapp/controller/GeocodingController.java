package com.thc.my_cardmapp.controller;

import com.thc.my_cardmapp.domain.Merchant;
import com.thc.my_cardmapp.repository.MerchantRepository;
import com.thc.my_cardmapp.service.NaverGeocodingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/geocode")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Geocoding", description = "주소 좌표 변환 API")
public class GeocodingController {

    private final MerchantRepository merchantRepository;
    private final NaverGeocodingService geocodingService;

    @PostMapping("/update-all")
    @Operation(summary = "전체 가맹점 좌표 변환",
               description = "DB의 모든 가맹점 주소를 위도/경도로 변환하여 업데이트합니다. (Rate limit: 10req/sec)")
    public ResponseEntity<Map<String, Object>> updateAllMerchantCoordinates() {
        log.info("전체 가맹점 좌표 변환 시작");

        List<Merchant> merchants = merchantRepository.findAll();
        int total = merchants.size();
        int success = 0;
        int failed = 0;
        int skipped = 0;

        for (int i = 0; i < merchants.size(); i++) {
            Merchant merchant = merchants.get(i);

            // 이미 좌표가 있으면 스킵
            if (merchant.getLocation() != null) {
                skipped++;
                continue;
            }

            // Geocoding 수행
            NaverGeocodingService.GeocodingResult result = geocodingService.geocode(merchant.getAddress());

            if (result.isSuccess()) {
                merchant.setLocation(result.getPoint());
                merchantRepository.save(merchant);
                success++;
                log.info("진행률: {}/{} - 성공: {}, ID: {}, 주소: {}",
                         i + 1, total, success, merchant.getId(), merchant.getAddress());
            } else {
                failed++;
                log.warn("Geocoding 실패: ID: {}, 주소: {}, 에러: {}",
                         merchant.getId(), merchant.getAddress(), result.getErrorMessage());
            }

            // Rate limit 대응: 초당 10개 요청 (100ms 간격)
            if (i < merchants.size() - 1) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Thread interrupted", e);
                    break;
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("success", success);
        response.put("failed", failed);
        response.put("skipped", skipped);

        log.info("좌표 변환 완료 - 총: {}, 성공: {}, 실패: {}, 스킵: {}", total, success, failed, skipped);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update/{merchantId}")
    @Operation(summary = "특정 가맹점 좌표 변환", description = "특정 가맹점의 주소를 위도/경도로 변환합니다.")
    public ResponseEntity<Map<String, Object>> updateMerchantCoordinate(@PathVariable Long merchantId) {
        log.info("가맹점 좌표 변환 요청: ID={}", merchantId);

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + merchantId));

        NaverGeocodingService.GeocodingResult result = geocodingService.geocode(merchant.getAddress());

        Map<String, Object> response = new HashMap<>();
        if (result.isSuccess()) {
            merchant.setLocation(result.getPoint());
            merchantRepository.save(merchant);

            response.put("success", true);
            response.put("merchantId", merchantId);
            response.put("address", merchant.getAddress());
            response.put("latitude", result.getLatitude());
            response.put("longitude", result.getLongitude());
            log.info("좌표 변환 성공: ID={}, ({}, {})", merchantId, result.getLatitude(), result.getLongitude());
        } else {
            response.put("success", false);
            response.put("merchantId", merchantId);
            response.put("address", merchant.getAddress());
            response.put("error", result.getErrorMessage());
            log.warn("좌표 변환 실패: ID={}, 에러={}", merchantId, result.getErrorMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "좌표 변환 상태", description = "좌표가 있는/없는 가맹점 수를 확인합니다.")
    public ResponseEntity<Map<String, Object>> getGeocodingStatus() {
        long totalCount = merchantRepository.count();
        long withCoordinates = merchantRepository.countByLocationIsNotNull();
        long withoutCoordinates = totalCount - withCoordinates;

        Map<String, Object> response = new HashMap<>();
        response.put("total", totalCount);
        response.put("withCoordinates", withCoordinates);
        response.put("withoutCoordinates", withoutCoordinates);
        response.put("percentage", totalCount > 0 ? (withCoordinates * 100.0 / totalCount) : 0);

        return ResponseEntity.ok(response);
    }
}
