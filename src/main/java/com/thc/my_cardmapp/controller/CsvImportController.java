package com.thc.my_cardmapp.controller;

import com.thc.my_cardmapp.service.CsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/csv-import")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CSV Import", description = "CSV 데이터 임포트 관리")
public class CsvImportController {

    private final CsvImportService csvImportService;

    @PostMapping("/merchants")
    @Operation(summary = "가맹점 CSV 임포트", description = "서울사랑상품권 가맹점 CSV 파일을 DB에 임포트합니다.")
    public ResponseEntity<String> importMerchants(
            @RequestParam String csvFilePath,
            @RequestParam(defaultValue = "0") int limit) {
        try {
            log.info("CSV 임포트 요청 - 파일: {}, 제한: {}", csvFilePath, limit);

            csvImportService.importMerchantsFromCsv(csvFilePath, limit);

            String message = limit > 0
                    ? String.format("CSV 임포트 완료 (최대 %d개)", limit)
                    : "CSV 임포트 완료 (전체)";

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("CSV 임포트 실패", e);
            return ResponseEntity.internalServerError()
                    .body("CSV 임포트 실패: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    @Operation(summary = "테스트", description = "API 작동 테스트")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("CSV Import API is working!");
    }
}
