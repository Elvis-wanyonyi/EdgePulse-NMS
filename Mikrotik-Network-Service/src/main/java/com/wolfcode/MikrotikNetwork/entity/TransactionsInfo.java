package com.wolfcode.MikrotikNetwork.entity;

import com.wolfcode.MikrotikNetwork.dto.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "mpesa_transactions")
public class TransactionsInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String macAddress;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(name = "mpesa_code", nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String amount;
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    private Status status;

}
