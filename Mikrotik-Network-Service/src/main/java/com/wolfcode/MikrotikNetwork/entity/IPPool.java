package com.wolfcode.MikrotikNetwork.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ip_pool")
public class IPPool {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false, name = "pool_name")
    private String poolName;
    private String ipRange;

    @ManyToOne(fetch = FetchType.LAZY)
    private Routers router;
}
