package com.thc.my_cardmapp.service;

import com.thc.my_cardmapp.domain.Category;
import com.thc.my_cardmapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 모든 활성 카테고리 조회
     */
    @Cacheable(value = "activeCategories")
    public List<Category> getActiveCategories() {
        log.debug("활성 카테고리 목록 조회");
        return categoryRepository.findActiveCategories();
    }

    /**
     * 카드별 인기 카테고리 조회
     */
    @Cacheable(value = "popularCategories", key = "#cardId")
    public List<Map<String, Object>> getPopularCategoriesByCard(Long cardId) {
        List<Object[]> results = categoryRepository.findPopularCategoriesByCardId(cardId);

        return results.stream()
                .map(row -> Map.of(
                        "category", row[0],
                        "merchantCount", row[1]
                ))
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 생성 (관리자 기능)
     */
    @Transactional
    public Category createCategory(String name) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다: " + name);
        }

        Category category = Category.builder()
                .name(name)
                .build();

        return categoryRepository.save(category);
    }
}