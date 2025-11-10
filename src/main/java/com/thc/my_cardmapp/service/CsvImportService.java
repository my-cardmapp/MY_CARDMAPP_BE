package com.thc.my_cardmapp.service;

import com.thc.my_cardmapp.domain.Card;
import com.thc.my_cardmapp.domain.Category;
import com.thc.my_cardmapp.domain.Merchant;
import com.thc.my_cardmapp.domain.MerchantCard;
import com.thc.my_cardmapp.repository.CardRepository;
import com.thc.my_cardmapp.repository.CategoryRepository;
import com.thc.my_cardmapp.repository.MerchantCardRepository;
import com.thc.my_cardmapp.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    private final MerchantRepository merchantRepository;
    private final MerchantCardRepository merchantCardRepository;
    private final CategoryRepository categoryRepository;
    private final CardRepository cardRepository;
    private final ResourceLoader resourceLoader;

    private static final int BATCH_SIZE = 1000;
    private static final String CARD_NAME = "지역사랑상품권";

    /**
     * 서울페이 업종명을 카테고리 ID로 매핑
     */
    private Map<String, Long> createCategoryMapping() {
        Map<String, Long> mapping = new HashMap<>();

        // 기존 카테고리 조회
        Category restaurant = categoryRepository.findByName("음식점").orElseThrow();
        Category convenience = categoryRepository.findByName("편의점").orElseThrow();
        Category cafe = categoryRepository.findByName("카페").orElseThrow();
        Category bakery = categoryRepository.findByName("베이커리").orElseThrow();
        Category bookstore = categoryRepository.findByName("서점").orElseThrow();
        Category cinema = categoryRepository.findByName("영화관").orElseThrow();
        Category mart = categoryRepository.findByName("마트").orElseThrow();
        Category general = categoryRepository.findByName("일반상점").orElseThrow();

        // 매핑 규칙 정의
        mapping.put("음식점/식음료업", restaurant.getId());
        mapping.put("식자재/유통", mart.getId());
        mapping.put("문화/체육", cinema.getId());

        // 기타 모든 업종은 일반상점으로 매핑
        mapping.put("생활/리빙", general.getId());
        mapping.put("기타", general.getId());
        mapping.put("의류/잡화", general.getId());
        mapping.put("보건/복지", general.getId());
        mapping.put("기술/기능 교육", general.getId());
        mapping.put("가전/통신", general.getId());
        mapping.put("건축/철물", general.getId());
        mapping.put("자동차/주유", general.getId());
        mapping.put("입시/교습학원", general.getId());
        mapping.put("디자인/인쇄", general.getId());
        mapping.put("여행/숙박", general.getId());
        mapping.put("가구/인테리어", general.getId());
        mapping.put("예술 교육", general.getId());
        mapping.put("부동산/임대", general.getId());
        mapping.put("외국어/언어", general.getId());
        mapping.put("기업/기관", general.getId());
        mapping.put("기타교육기관", general.getId());

        // 기본값 (매핑되지 않은 업종)
        mapping.put("DEFAULT", general.getId());

        return mapping;
    }

    /**
     * CSV 파일에서 가맹점 데이터를 읽어서 DB에 저장
     * @param csvFilePath CSV 파일 경로
     * @param limit 삽입할 최대 레코드 수 (0이면 전체)
     */
    @Transactional
    public void importMerchantsFromCsv(String csvFilePath, int limit) {
        log.info("CSV 임포트 시작: {}", csvFilePath);

        Map<String, Long> categoryMapping = createCategoryMapping();
        Card targetCard = cardRepository.findByName(CARD_NAME)
                .orElseThrow(() -> new RuntimeException("카드를 찾을 수 없습니다: " + CARD_NAME));

        List<Merchant> merchantBatch = new ArrayList<>();
        int totalCount = 0;
        int successCount = 0;
        int skipCount = 0;

        // Resource 로드 (classpath: 또는 file: 지원)
        Resource resource = resourceLoader.getResource(csvFilePath);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), Charset.forName("EUC-KR")))) {

            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                // 헤더 스킵
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // limit 체크
                if (limit > 0 && totalCount >= limit) {
                    break;
                }

                try {
                    String[] fields = parseCsvLine(line);

                    if (fields.length < 6) {
                        log.warn("컬럼 수 부족, 스킵: {}", line);
                        skipCount++;
                        continue;
                    }

                    String name = fields[0].trim();
                    String businessType = fields[1].trim();
                    String district = fields[3].trim();
                    String baseAddress = fields[4].trim();
                    String detailAddress = fields.length > 5 ? fields[5].trim() : "";

                    // 주소 결합
                    String fullAddress = baseAddress;
                    if (!detailAddress.isEmpty()) {
                        fullAddress += " " + detailAddress;
                    }

                    // 카테고리 매핑
                    Long categoryId = categoryMapping.getOrDefault(businessType, categoryMapping.get("DEFAULT"));

                    // Merchant 엔티티 생성
                    Merchant merchant = Merchant.builder()
                            .name(name)
                            .address(fullAddress)
                            .geography(district)
                            .category(categoryRepository.findById(categoryId).orElseThrow())
                            .build();

                    merchantBatch.add(merchant);
                    totalCount++;

                    // 배치 처리
                    if (merchantBatch.size() >= BATCH_SIZE) {
                        successCount += saveMerchantBatch(merchantBatch, targetCard);
                        merchantBatch.clear();
                        log.info("진행률: {}/{} 저장 완료", successCount, totalCount);
                    }

                } catch (Exception e) {
                    log.error("레코드 처리 실패: {}", line, e);
                    skipCount++;
                }
            }

            // 남은 배치 처리
            if (!merchantBatch.isEmpty()) {
                successCount += saveMerchantBatch(merchantBatch, targetCard);
                log.info("최종: {}/{} 저장 완료", successCount, totalCount);
            }

            log.info("CSV 임포트 완료 - 총: {}, 성공: {}, 스킵: {}", totalCount, successCount, skipCount);

        } catch (Exception e) {
            log.error("CSV 파일 읽기 실패", e);
            throw new RuntimeException("CSV 임포트 실패", e);
        }
    }

    /**
     * Merchant 배치 저장 및 MerchantCard 연결
     */
    private int saveMerchantBatch(List<Merchant> merchants, Card card) {
        List<Merchant> savedMerchants = merchantRepository.saveAll(merchants);

        List<MerchantCard> merchantCards = new ArrayList<>();
        for (Merchant merchant : savedMerchants) {
            MerchantCard merchantCard = MerchantCard.builder()
                    .merchant(merchant)
                    .card(card)
                    .build();
            merchantCards.add(merchantCard);
        }

        merchantCardRepository.saveAll(merchantCards);
        return savedMerchants.size();
    }

    /**
     * CSV 라인 파싱 (쌍따옴표 내 쉼표 처리)
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }
}
