package com.thc.my_cardmapp.controller;

import com.thc.my_cardmapp.domain.Card;
import com.thc.my_cardmapp.domain.Merchant;
import com.thc.my_cardmapp.dto.CategoryDto;
import com.thc.my_cardmapp.dto.MerchantDto;
import com.thc.my_cardmapp.service.CardService;
import com.thc.my_cardmapp.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Card", description = "카드 관련 API")  // Swagger 문서에 표시될 설명
public class CardController {

    private final CardService cardService;
    private final MerchantService merchantService;

    @Operation(summary = "전체 카드 목록 조회", description = "등록된 모든 복지카드 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<List<Card>> getAllCards() {
        log.info("전체 카드 목록 조회 요청");
        List<Card> cards = cardService.getActiveCards();
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "카드 상세 조회", description = "카드 ID로 특정 카드의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<Card> getCardById(
            @Parameter(description = "카드 ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("카드 상세 조회 요청 - ID: {}", id);
        Card card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "카드 검색", description = "카드명으로 카드를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<Card>> searchCards(
            @Parameter(description = "검색 키워드", example = "아동급식")
            @RequestParam(required = false) String keyword) {
        log.info("카드 검색 요청 - 키워드: {}", keyword);
        List<Card> cards = cardService.searchCards(keyword);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "새 카드 등록", description = "새로운 복지카드를 등록합니다. (관리자 전용)")
    @PostMapping
    public ResponseEntity<Card> createCard(@RequestBody Card card) {
        log.info("새 카드 등록 요청 - 카드명: {}", card.getName());
        Card createdCard = cardService.createCard(card);
        return ResponseEntity.ok(createdCard);
    }

    @Operation(summary = "카드별 가맹점 조회", description = "특정 카드를 사용할 수 있는 가맹점 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "카드를 찾을 수 없음")
    })
    @GetMapping("/{id}/merchants")
    public ResponseEntity<List<MerchantDto>> getMerchantsByCard(
            @Parameter(description = "카드 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "위도 (거리 계산용)", example = "37.5")
            @RequestParam(required = false) Double lat,
            @Parameter(description = "경도 (거리 계산용)", example = "127.03")
            @RequestParam(required = false) Double lng,
            @Parameter(description = "반경 (미터)", example = "5000")
            @RequestParam(defaultValue = "5000") double radius) {

        log.info("카드별 가맹점 조회 - 카드ID: {}, 위치: ({}, {}), 반경: {}m", id, lat, lng, radius);

        // 카드 존재 여부 확인
        Card card = cardService.getCardById(id);

        List<Merchant> merchants;
        if (lat != null && lng != null) {
            // 위치 정보가 있으면 근처 가맹점만 조회
            merchants = merchantService.findNearbyMerchantsByCards(lat, lng, radius, List.of(card.getName()));
        } else {
            // 위치 정보가 없으면 해당 카드의 모든 가맹점 조회
            merchants = merchantService.findMerchantsByCardId(id);
        }

        // Entity를 DTO로 변환
        List<MerchantDto> merchantDtos = merchants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("조회된 가맹점 수: {}", merchantDtos.size());
        return ResponseEntity.ok(merchantDtos);
    }

    // Entity to DTO 변환 메서드
    private MerchantDto convertToDto(Merchant merchant) {
        return MerchantDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .address(merchant.getAddress())
                .latitude(merchant.getLocation() != null ? merchant.getLocation().getY() : null)
                .longitude(merchant.getLocation() != null ? merchant.getLocation().getX() : null)
                .category(merchant.getCategory() != null ?
                        CategoryDto.builder()
                                .id(merchant.getCategory().getId())
                                .name(merchant.getCategory().getName())
                                .build() : null)
                .build();
    }
}