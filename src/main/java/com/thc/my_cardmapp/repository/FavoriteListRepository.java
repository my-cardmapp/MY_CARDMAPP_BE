package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.FavoriteList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteListRepository extends JpaRepository<FavoriteList, Long> {

    // 사용자의 즐겨찾기 목록
    List<FavoriteList> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 사용자와 이름으로 즐겨찾기 찾기
    Optional<FavoriteList> findByUserIdAndName(Long userId, String name);

    // 즐겨찾기 이름 중복 확인
    boolean existsByUserIdAndName(Long userId, String name);
}