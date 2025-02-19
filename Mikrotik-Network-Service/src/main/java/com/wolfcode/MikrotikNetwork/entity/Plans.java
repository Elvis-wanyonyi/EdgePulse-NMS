package com.wolfcode.MikrotikNetwork.entity;

import com.wolfcode.MikrotikNetwork.dto.ServiceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "plans")
public class Plans {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String planName;
    private Integer planValidity;
    private Integer price;
    private ServiceType serviceType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bandwidth_id")
    private BandwidthLimits bandwidthLimit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ip_pool_id")
    private IPPool ipPool;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "router_id")
    private Routers router;

}
