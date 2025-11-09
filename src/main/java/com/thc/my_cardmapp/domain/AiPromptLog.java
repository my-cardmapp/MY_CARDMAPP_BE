package com.thc.my_cardmapp.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_prompt_log")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPromptLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "raw_prompt", columnDefinition = "TEXT")
    private String rawPrompt;

    @Column(name = "parsed_json", columnDefinition = "JSON")
    private String parsedJson;

    @Column(name = "response_tokens")
    private Integer responseTokens;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(length = 50)
    private String status;
}