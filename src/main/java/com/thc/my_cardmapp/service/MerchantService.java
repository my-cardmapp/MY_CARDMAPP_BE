package com.thc.my_cardmapp.service;

import com.thc.my_cardmapp.domain.Card;
import com.thc.my_cardmapp.domain.Merchant;
import com.thc.my_cardmapp.domain.MerchantCard;
import com.thc.my_cardmapp.repository.MerchantRepository;
import com.thc.my_cardmapp.repository.MerchantCardRepository;
import com.thc.my_cardmapp.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // DB 작업의 전부 성공 or 전부 실패를 보장하는 단위
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantCardRepository merchantCardRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    /**
     * SRID(Spatial Reference System Identifier: 공간 참조 시스템) 4326을 사용하는 GeometryFactory
     * 3. 다른 SRID 예시
     *   - SRID 3857: Web Mercator (구글맵, 네이버맵 등 웹 지도)
     *   - SRID 5179: Korea 2000 (한국 지역 특화)
     *   - SRID 5186: Korean 1985 (구 한국 좌표계)
     */

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 위치 기반 가맹점 검색
     */
    public List<Merchant> findNearbyMerchants(double lat, double lng, double radius) {
        log.debug("위치 기반 가맹점 검색 - 위도: {}, 경도: {}, 반경: {}m", lat, lng, radius);

        Point location = createPoint(lng, lat);
        return merchantRepository.findNearbyMerchants(location, radius);
    }

    /**
     * 카드 타입별 + 위치 기반 검색
     */
    public List<Merchant> findNearbyMerchantsByCards(double lat, double lng, double radius, List<String> cardNames) {
        log.debug("카드별 위치 기반 검색 - 카드: {}", cardNames);

        Point location = createPoint(lng, lat);
        return merchantRepository.findNearbyMerchantsByCards(location, radius, cardNames);
    }

    /**
     * 특정 카드를 사용할 수 있는 모든 가맹점 조회
     */
    public List<Merchant> findMerchantsByCardId(Long cardId) {
        log.debug("카드별 가맹점 조회 - 카드ID: {}", cardId);

        return merchantCardRepository.findByCard_Id(cardId).stream()
                .map(MerchantCard::getMerchant)
                .collect(Collectors.toList());
    }

    /**
     * 복합 필터 검색
     */
    public Page<Merchant> searchMerchants(Long cardId, Long categoryId, String keyword, Pageable pageable) {
        log.debug("복합 검색 - 카드ID: {}, 카테고리ID: {}, 키워드: {}", cardId, categoryId, keyword);

        return merchantRepository.findByMultipleFilters(cardId, categoryId, keyword, pageable);
    }

    /**
     * 키워드 검색
     */
    @Transactional
    public Page<Merchant> searchByKeyword(String keyword, Long userId, Pageable pageable) {
        log.debug("키워드 검색: {}", keyword);

        Page<Merchant> results = merchantRepository.searchByKeyword(keyword, pageable);

        // 검색 기록 저장 (비동기 처리 권장)
        if (userId != null && !results.isEmpty()) {
            saveSearchHistory(userId, keyword, results.getContent());
        }

        return results;
    }

    /**
     * 가맹점 상세 정보
     */
    public Map<String, Object> getMerchantDetail(Long merchantId, Double userLat, Double userLng) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다. ID: " + merchantId));

        // 사용 가능한 카드 목록 조회
        List<Card> availableCards = merchantCardRepository.findCardsByMerchantId(merchantId);

        // 사용자 위치가 있으면 거리 계산
        if (userLat != null && userLng != null && merchant.getLocation() != null) {
            double distance = calculateDistance(userLat, userLng,
                    merchant.getLocation().getY(), merchant.getLocation().getX());

            return Map.of(
                    "merchant", merchant,
                    "availableCards", availableCards,
                    "distance", distance
            );
        }

        return Map.of(
                "merchant", merchant,
                "availableCards", availableCards
        );
    }

    /**
     * 가맹점 등록 (관리자 기능)
     */
    @Transactional
    public Merchant createMerchant(Merchant merchant, List<Long> cardIds) {
        log.info("새 가맹점 등록: {}", merchant.getName());

        // 위치 정보가 있다면 Point 객체 생성
        if (merchant.getLocation() != null) {
            // location이 이미 Point 객체인 경우 SRID만 설정
            merchant.getLocation().setSRID(4326);
        }

        // 가맹점 저장
        Merchant savedMerchant = merchantRepository.save(merchant);

        // 카드 연결
        if (cardIds != null && !cardIds.isEmpty()) {
            for (Long cardId : cardIds) {
                MerchantCard merchantCard = MerchantCard.builder()
                        .merchant(savedMerchant)
                        .card(Card.builder().id(cardId).build())
                        .build();
                merchantCardRepository.save(merchantCard);
            }
        }

        return savedMerchant;
    }

    /**
     * 가맹점 정보 업데이트
     */
    @Transactional
    public Merchant updateMerchant(Long merchantId, Merchant updateData) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다."));

        // 업데이트 로직
        if (updateData.getName() != null) {
            merchant.setName(updateData.getName());
        }
        if (updateData.getAddress() != null) {
            merchant.setAddress(updateData.getAddress());
        }
        if (updateData.getCategory() != null) {
            merchant.setCategory(updateData.getCategory());
        }
        if (updateData.getLocation() != null) {
            updateData.getLocation().setSRID(4326);
            merchant.setLocation(updateData.getLocation());
        }

        return merchantRepository.save(merchant);
    }

    // Helper Methods

    /**
     * Point 객체 생성 (SRID 4326 설정 포함)
     */
    private Point createPoint(double longitude, double latitude) {
        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point point = geometryFactory.createPoint(coordinate);
        // SRID는 GeometryFactory에서 자동 설정됨
        return point;
    }

    /**
     * 두 지점 간 거리 계산 (Haversine formula)
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return Math.round(distance * 100.0) / 100.0; // round to 2 decimal places
    }

    /**
     * 검색 기록 저장
     */
    @Transactional
    private void saveSearchHistory(Long userId, String query, List<Merchant> results) {
        // TODO: SearchHistory 저장 로직 구현
        log.debug("검색 기록 저장 - userId: {}, query: {}", userId, query);
    }
}