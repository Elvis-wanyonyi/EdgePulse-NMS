package com.wolfcode.MikrotikNetwork.entity;

import com.wolfcode.MikrotikNetwork.dto.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "pppoe_payments")
public class PPPoETransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String account;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(name = "mpesa_code", nullable = false)
    private String mpesaRef;
    @Column(nullable = false)
    private String amount;
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String router;
}
