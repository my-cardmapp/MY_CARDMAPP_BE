package com.thc.my_cardmapp.controller;

import com.thc.my_cardmapp.domain.Card;
import com.thc.my_cardmapp.domain.Merchant;
import com.thc.my_cardmapp.dto.CardDto;
import com.thc.my_cardmapp.dto.CategoryDto;
import com.thc.my_cardmapp.dto.MerchantDetailDto;
import com.thc.my_cardmapp.dto.MerchantDto;
import com.thc.my_cardmapp.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
@Tag(name = "Merchant", description = "가맹점 관련 API")
public class MerchantController {

        private final MerchantService merchantService;

        @Operation(summary = "모든 가맹점 조회", description = "모든 가맹점 목록을 페이지네이션하여 조회합니다")
        @GetMapping // GET - 모든 가맹점 조회
        public ResponseEntity<Page<MerchantDto>> getAllMerchants(
                        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

                log.info("모든 가맹점 조회 - 페이지: {}, 크기: {}", page, size);

                PageRequest pageRequest = PageRequest.of(page, size);
                Page<Merchant> merchants = merchantService.getAllMerchants(pageRequest);

                // Page<Entity>를 Page<DTO>로 변환
                Page<MerchantDto> merchantDtos = merchants.map(this::convertToDto);

                return ResponseEntity.ok(merchantDtos);
        }

        @Operation(summary = "위치 기반 가맹점 검색", description = "현재 위치 주변의 가맹점을 검색합니다")
        @GetMapping("/nearby") // GET - 근처 가맹점 조회
        public ResponseEntity<List<MerchantDto>> findNearbyMerchants(
                        @Parameter(description = "위도", required = true, example = "37.5665") @RequestParam double lat,
                        @Parameter(description = "경도", required = true, example = "126.9780") @RequestParam double lng,
                        @Parameter(description = "검색 반경(미터)", example = "1000") @RequestParam(defaultValue = "1000") double radius,
                        @Parameter(description = "카드 이름 목록", example = "아동급식카드,문화누리카드") @RequestParam(required = false) List<String> cardNames) {

                log.info("위치 기반 검색 - 위도: {}, 경도: {}, 반경: {}m, 카드: {}", lat, lng, radius, cardNames);

                List<Merchant> merchants;
                if (cardNames != null && !cardNames.isEmpty()) {
                        merchants = merchantService.findNearbyMerchantsByCards(lat, lng, radius, cardNames);
                } else {
                        merchants = merchantService.findNearbyMerchants(lat, lng, radius);
                }

                // Entity를 DTO로 변환
                List<MerchantDto> merchantDtos = merchants.stream()
                                .map(this::convertToDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(merchantDtos);
        }

        @Operation(summary = "가맹점 검색", description = "키워드로 가맹점을 검색합니다")
        @GetMapping("/search") // GET - 근처 가맹점 검색
        public ResponseEntity<Page<MerchantDto>> searchMerchants(
                        @Parameter(description = "검색 키워드", example = "편의점") @RequestParam(required = false) String keyword,
                        @Parameter(description = "카드 ID") @RequestParam(required = false) Long cardId,
                        @Parameter(description = "카테고리 ID") @RequestParam(required = false) Long categoryId,
                        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

                log.info("가맹점 검색 - 키워드: {}, 카드ID: {}, 카테고리ID: {}", keyword, cardId, categoryId);

                PageRequest pageRequest = PageRequest.of(page, size);
                Page<Merchant> results = merchantService.searchMerchants(cardId, categoryId, keyword, pageRequest);

                // Page<Entity>를 Page<DTO>로 변환
                Page<MerchantDto> dtoResults = results.map(this::convertToDto);

                return ResponseEntity.ok(dtoResults);
        }

        @Operation(summary = "가맹점 상세 조회", description = "특정 가맹점의 상세 정보를 조회합니다")
        @GetMapping("/{id}") // GET - 가맹점 정보 상세 조회
        public ResponseEntity<MerchantDetailDto> getMerchantDetail(
                        @Parameter(description = "가맹점 ID", required = true) @PathVariable Long id,
                        @Parameter(description = "사용자 위도 (거리 계산용)") @RequestParam(required = false) Double userLat,
                        @Parameter(description = "사용자 경도 (거리 계산용)") @RequestParam(required = false) Double userLng) {

                log.info("가맹점 상세 조회 - ID: {}", id);
                Map<String, Object> detail = merchantService.getMerchantDetail(id, userLat, userLng);

                Merchant merchant = (Merchant) detail.get("merchant");
                @SuppressWarnings("unchecked")
                List<Card> cards = (List<Card>) detail.get("availableCards");

                MerchantDetailDto dto = MerchantDetailDto.builder()
                                .id(merchant.getId())
                                .name(merchant.getName())
                                .address(merchant.getAddress())
                                .latitude(merchant.getLocation() != null ? merchant.getLocation().getY() : null)
                                .longitude(merchant.getLocation() != null ? merchant.getLocation().getX() : null)
                                .category(merchant.getCategory() != null ? CategoryDto.builder()
                                                .id(merchant.getCategory().getId())
                                                .name(merchant.getCategory().getName())
                                                .build() : null)
                                .availableCards(cards != null ? cards.stream()
                                                .map(card -> CardDto.builder()
                                                                .id(card.getId())
                                                                .name(card.getName())
                                                                .colorHex(card.getColorHex())
                                                                .issuer(card.getIssuer())
                                                                .build())
                                                .collect(Collectors.toList()) : null)
                                .distance(detail.get("distance") != null ? (Double) detail.get("distance") : null)
                                .build();

                return ResponseEntity.ok(dto);
        }

        @Operation(summary = "가맹점 등록", description = "새로운 가맹점을 등록합니다 (관리자 기능)")
        @PostMapping
        public ResponseEntity<MerchantDto> createMerchant(
                        @RequestBody MerchantCreateRequest request) {

                log.info("가맹점 등록 요청 - 이름: {}", request.getName());

                Merchant merchant = Merchant.builder()
                                .name(request.getName())
                                .address(request.getAddress())
                                .build();

                Merchant created = merchantService.createMerchant(merchant, request.getCardIds());

                // 생성된 Entity를 DTO로 변환하여 반환
                MerchantDto createdDto = convertToDto(created);

                return ResponseEntity.ok(createdDto);
        }

        // Entity to DTO 변환 메서드
        private MerchantDto convertToDto(Merchant merchant) {
                return MerchantDto.builder()
                                .id(merchant.getId())
                                .name(merchant.getName())
                                .address(merchant.getAddress())
                                .latitude(merchant.getLocation() != null ? merchant.getLocation().getY() : null)
                                .longitude(merchant.getLocation() != null ? merchant.getLocation().getX() : null)
                                .phone(merchant.getPhone())
                                .businessHours(merchant.getBusinessHours())
                                .category(merchant.getCategory() != null ? CategoryDto.builder()
                                                .id(merchant.getCategory().getId())
                                                .name(merchant.getCategory().getName())
                                                .build() : null)
                                .build();
        }

        // DTO 클래스
        @lombok.Data
        static class MerchantCreateRequest {
                private String name;
                private String address;
                private Long categoryId;
                private Double lat;
                private Double lng;
                private List<Long> cardIds;
        }
}