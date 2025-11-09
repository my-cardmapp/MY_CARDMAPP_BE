package com.thc.my_cardmapp.repository;

import com.thc.my_cardmapp.domain.MerchantEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MerchantEmbeddingRepository extends JpaRepository<MerchantEmbedding, Long> {

    // 업데이트가 필요한 임베딩 조회
    @Query("SELECT me FROM MerchantEmbedding me " +
            "WHERE me.updatedAt < :threshold OR me.updatedAt IS NULL")
    List<MerchantEmbedding> findOutdatedEmbeddings(LocalDateTime threshold);
}