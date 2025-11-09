package com.thc.my_cardmapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "merchant")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "geography(Point, 4326)")
    @JsonIgnore  // Point 타입 순환 참조 방지
    private Point location;

    @Column(columnDefinition = "TEXT")
    private String geography;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String businessHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 관계 매핑
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore  // 순환 참조 방지
    private List<MerchantCard> merchantCards = new ArrayList<>();

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore  // 순환 참조 방지
    private List<SearchHistory> searchHistories = new ArrayList<>();
}