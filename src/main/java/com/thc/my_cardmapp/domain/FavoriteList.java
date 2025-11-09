package com.thc.my_cardmapp.domain;  // 👈 entity 없이!

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorite_list")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String createdId;
}