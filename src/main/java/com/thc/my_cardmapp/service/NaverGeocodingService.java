package com.thc.my_cardmapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverGeocodingService {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GEOCODING_URL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode";
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 주소를 위도/경도로 변환
     */
    public GeocodingResult geocode(String address) {
        try {
            // URL 생성
            String url = UriComponentsBuilder.fromUriString(GEOCODING_URL)
                    .queryParam("query", address)
                    .toUriString();

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // 응답 파싱
            NaverGeocodingResponse naverResponse = objectMapper.readValue(
                    response.getBody(),
                    NaverGeocodingResponse.class
            );

            // 결과가 있으면 첫 번째 결과 반환
            if (naverResponse.getAddresses() != null && !naverResponse.getAddresses().isEmpty()) {
                NaverAddress naverAddress = naverResponse.getAddresses().get(0);
                double lat = Double.parseDouble(naverAddress.getY());
                double lng = Double.parseDouble(naverAddress.getX());

                // PostGIS Point 생성 (경도, 위도 순서 주의!)
                Point point = geometryFactory.createPoint(new Coordinate(lng, lat));

                log.debug("Geocoding 성공: {} -> ({}, {})", address, lat, lng);
                return new GeocodingResult(true, lat, lng, point, null);
            } else {
                log.warn("Geocoding 결과 없음: {}", address);
                return new GeocodingResult(false, 0, 0, null, "No results found");
            }

        } catch (Exception e) {
            log.error("Geocoding 실패: {}", address, e);
            return new GeocodingResult(false, 0, 0, null, e.getMessage());
        }
    }

    /**
     * Geocoding 결과
     */
    @Data
    public static class GeocodingResult {
        private final boolean success;
        private final double latitude;
        private final double longitude;
        private final Point point;
        private final String errorMessage;
    }

    /**
     * 네이버 Geocoding API 응답
     */
    @Data
    private static class NaverGeocodingResponse {
        private String status;
        private Meta meta;
        private List<NaverAddress> addresses;
    }

    @Data
    private static class Meta {
        private int totalCount;
        private int page;
        private int count;
    }

    @Data
    private static class NaverAddress {
        private String roadAddress;
        private String jibunAddress;
        private String englishAddress;
        private String x; // 경도 (longitude)
        private String y; // 위도 (latitude)
        private double distance;
    }
}
