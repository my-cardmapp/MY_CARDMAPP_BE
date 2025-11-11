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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleGeocodingService {

    @Value("${google.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 주소 정제 메서드
     * 상세 주소 정보(동/호수, 층수, 괄호 내용)를 제거하여 Google Geocoding이 인식 가능한 형태로 변환
     */
    private String cleanAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return address;
        }

        String cleaned = address;

        // 1. 동호수 제거 (예: ", 102동 204호" -> "")
        cleaned = cleaned.replaceAll(",\\s*\\d+동\\s*\\d+호", "");

        // 2. 층수 제거 (예: ", 2층 202호" -> "")
        cleaned = cleaned.replaceAll(",\\s*\\d+층\\s*\\d+호", "");

        // 3. 단독 동 제거 (예: ", 102동" -> "")
        cleaned = cleaned.replaceAll(",\\s*\\d+동", "");

        // 4. 단독 호수 제거 (예: ", 202호" -> "")
        cleaned = cleaned.replaceAll(",\\s*\\d+호", "");

        // 5. 층수만 제거 (예: ", 2층" -> "")
        cleaned = cleaned.replaceAll(",\\s*\\d+층", "");

        // 6. 괄호 안 내용 제거 (예: " (구산동,갈현현대아파트)" -> "")
        cleaned = cleaned.replaceAll("\\s*\\([^)]*\\)", "");

        // 7. 다중 공백을 단일 공백으로
        cleaned = cleaned.replaceAll("\\s+", " ");

        // 8. 앞뒤 공백 및 쉼표 제거
        cleaned = cleaned.trim().replaceAll(",$", "");

        return cleaned;
    }

    /**
     * 주소를 위도/경도로 변환
     */
    public GeocodingResult geocode(String address) {
        try {
            // 주소 정제
            String cleanedAddress = cleanAddress(address);
            log.debug("주소 정제: [{}] -> [{}]", address, cleanedAddress);

            // URL 생성
            String url = UriComponentsBuilder.fromUriString(GEOCODING_URL)
                    .queryParam("address", cleanedAddress)
                    .queryParam("key", apiKey)
                    .toUriString();

            // API 호출
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            GoogleGeocodingResponse googleResponse = objectMapper.readValue(
                    response.getBody(),
                    GoogleGeocodingResponse.class
            );

            if ("OK".equals(googleResponse.getStatus()) &&
                googleResponse.getResults() != null &&
                !googleResponse.getResults().isEmpty()) {

                GoogleResult result = googleResponse.getResults().get(0);
                double lat = result.getGeometry().getLocation().getLat();
                double lng = result.getGeometry().getLocation().getLng();

                // PostGIS Point 생성 (경도, 위도 순서 주의!)
                Point point = geometryFactory.createPoint(new Coordinate(lng, lat));

                log.debug("Geocoding 성공: {} -> ({}, {})", address, lat, lng);
                return new GeocodingResult(true, lat, lng, point, null);
            } else {
                log.warn("Geocoding 결과 없음: {} - Status: {}", address, googleResponse.getStatus());
                return new GeocodingResult(false, 0, 0, null, "No results found: " + googleResponse.getStatus());
            }

        } catch (Exception e) {
            log.error("Geocoding 실패: {}", address, e);
            return new GeocodingResult(false, 0, 0, null, e.getMessage());
        }
    }

    @Data
    public static class GeocodingResult {
        private final boolean success;
        private final double latitude;
        private final double longitude;
        private final Point point;
        private final String errorMessage;
    }

    @Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleGeocodingResponse {
        private String status;
        private List<GoogleResult> results;
        private String errorMessage;  // error_message 필드 추가
    }

    @Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleResult {
        private String formatted_address;
        private Geometry geometry;
    }

    @Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class Geometry {
        private Location location;
    }

    @Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class Location {
        private double lat;
        private double lng;
    }
}
