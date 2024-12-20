package com.wolfcode.MikrotikNetwork.entity;

import com.wolfcode.MikrotikNetwork.dto.LoginBy;
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
@Table(name = "clients_info")
public class HotspotClients {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private LocalDateTime createdOn;
    private LocalDateTime expiresOn;
    private String plan;
    private String username;
    private String ipAddress;
    @Column(nullable = false)
    private String router;
    private String mpesaReceiptNumber;
    private String phoneNumber;
    private int amount;
    @Enumerated(EnumType.STRING)
    private LoginBy loginBy;

}
