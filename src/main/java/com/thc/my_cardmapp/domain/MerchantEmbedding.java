package com.thc.my_cardmapp.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_embedding")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(columnDefinition = "TEXT")
    private String embedding;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;
}