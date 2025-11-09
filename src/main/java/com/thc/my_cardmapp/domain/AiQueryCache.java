package com.thc.my_cardmapp.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_query_cache")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiQueryCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cache_key")
    private Long cacheKey;

    @Column(columnDefinition = "JSON")
    private String result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}