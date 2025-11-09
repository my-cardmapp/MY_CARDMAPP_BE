package com.thc.my_cardmapp.service;

import com.thc.my_cardmapp.domain.SearchHistory;
import com.thc.my_cardmapp.domain.User;
import com.thc.my_cardmapp.domain.Merchant;
import com.thc.my_cardmapp.repository.SearchHistoryRepository;
import com.thc.my_cardmapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 검색 기록 저장 (비동기)
     */
    @Async
    @Transactional
    public void saveSearchHistory(Long userId, String query, Merchant selectedMerchant, String reason) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;

            SearchHistory history = SearchHistory.builder()
                    .user(user)
                    .query(query)
                    .merchant(selectedMerchant)
                    .shownReason(reason)
                    .build();

            searchHistoryRepository.save(history);
            log.debug("검색 기록 저장: userId={}, query={}", userId, query);

        } catch (Exception e) {
            log.error("검색 기록 저장 실패", e);
            // 검색 기록 저장 실패는 메인 프로세스에 영향을 주지 않음
        }
    }

    /**
     * 사용자의 최근 검색어 조회
     */
    public List<String> getUserRecentSearches(Long userId, int limit) {
        return searchHistoryRepository.findRecentQueriesByUserId(
                userId,
                PageRequest.of(0, limit)
        );
    }

    /**
     * 인기 검색어 조회
     */
    public List<Map<String, Object>> getPopularSearchQueries(int limit) {
        List<Object[]> results = searchHistoryRepository.findPopularSearchQueries(
                PageRequest.of(0, limit)
        );

        return results.stream()
                .map(row -> Map.of(
                        "query", row[0],
                        "count", row[1]
                ))
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 검색 기록 조회
     */
    public Page<SearchHistory> getUserSearchHistory(Long userId, PageRequest pageRequest) {
        return searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);
    }
}