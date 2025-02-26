package com.wolfcode.MikrotikNetwork.entity;

import com.wolfcode.MikrotikNetwork.dto.ActivePeriod;
import com.wolfcode.MikrotikNetwork.dto.ClientStatus;
import com.wolfcode.MikrotikNetwork.dto.LoginBy;
import com.wolfcode.MikrotikNetwork.dto.ServiceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients")
public class Clients {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String account;
    private String fullName;
    private String email;
    private String address;
    private String phone;
    private Integer payment;
    private String mpesaRef;
    private Integer balance;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private ActivePeriod activePeriod;
    @Enumerated(EnumType.STRING)
    private ServiceType type;
    private LocalDateTime createdOn;
    private LocalDateTime expiresOn;
    @Enumerated(EnumType.STRING)
    private LoginBy loginBy;
    @Enumerated(EnumType.STRING)
    private ClientStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plans plan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "router_id")
    private Routers router;


}
