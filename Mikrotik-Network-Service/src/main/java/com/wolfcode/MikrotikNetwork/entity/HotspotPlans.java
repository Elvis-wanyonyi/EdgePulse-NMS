package com.wolfcode.MikrotikNetwork.entity;

import com.wolfcode.MikrotikNetwork.dto.PlanDuration;
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
@Table(name = "plans")
public class HotspotPlans {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private  Long id;
    @Column(name = "package_plan", nullable = false, unique = true)
    private String packageName;
    private String dataLimit;
    private Integer planValidity;
    @Enumerated(EnumType.STRING)
    private PlanDuration planDuration;
    private int price;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "bandwidth_id")
    private BandwidthLimits bandwidthLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "router_id")
    private Routers router;

}
