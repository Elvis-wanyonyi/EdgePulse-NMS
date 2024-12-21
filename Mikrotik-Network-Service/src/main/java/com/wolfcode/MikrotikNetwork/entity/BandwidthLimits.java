package com.wolfcode.MikrotikNetwork.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bandwidth_limits")
public class BandwidthLimits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer uploadSpeed;

    @Column(nullable = false)
    private Integer downloadSpeed;

    @Column(nullable = false)
    private String uploadUnit;

    @Column(nullable = false)
    private String downloadUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "router_id")
    private Routers router;


    @Transient
    public String concatBandwidthLimit() {
        return String.format("%d%s/%d%s", uploadSpeed, uploadUnit, downloadSpeed, downloadUnit);
    }
}
