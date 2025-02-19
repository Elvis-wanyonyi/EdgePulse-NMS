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
@Table(name = "Routers")
public class Routers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "router_name", nullable = false, unique = true)
    private String routerName;
    @Column(name = "router_ip_address",nullable = false, unique = true)
    private String routerIPAddress;
    @Column(nullable = false)
    private String username;
    private String password;
    private String description;

}
