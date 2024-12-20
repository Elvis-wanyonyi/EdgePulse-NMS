package com.wolfcode.MikrotikNetwork.entity;

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
@Table(name = "pppoe_profile")
public class PPPoEPlans {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    private String planValidity;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "bandwidth", referencedColumnName = "name")
    private BandwidthLimits bandwidthLimit;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "ip_pool", referencedColumnName = "pool_name")
    private IPPool ipPool;

    @ManyToOne(fetch = FetchType.EAGER)
    private Routers router;

}
