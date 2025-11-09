package com.thc.my_cardmapp.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "search_history")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String query;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_merchant_ids")
    private Merchant merchant;

    @Column(name = "shown_reason", columnDefinition = "TEXT")
    private String shownReason;
}