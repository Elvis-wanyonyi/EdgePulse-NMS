package com.wolfcode.MikrotikNetwork.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pppoe_clients")
public class PPPoEClients {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String account;
    private String name;
    private String phone;
    private String plan;
    private String payment;
    private String balance;
    private String status;
    private String username;
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    private Routers router;


}
