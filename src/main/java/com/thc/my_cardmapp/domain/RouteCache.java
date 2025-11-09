package com.thc.my_cardmapp.domain;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_cache")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point origin;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point dest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @Column(name = "distance_m")
    private Integer distanceM;

    @Column(name = "travel_time_s")
    private Integer travelTimeS;

    @Column(columnDefinition = "geometry")
    private String path;

    @Column(name = "expires_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime expiresAt;
}