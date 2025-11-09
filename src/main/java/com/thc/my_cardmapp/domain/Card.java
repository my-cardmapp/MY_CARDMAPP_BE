package com.thc.my_cardmapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(length = 100)
    private String issuer;

    // 관계 매핑
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore // @JsonManagedReference와 @JsonBackReference로 인한 순환 참조 방지
    private List<MerchantCard> merchantCards = new ArrayList<>();
}