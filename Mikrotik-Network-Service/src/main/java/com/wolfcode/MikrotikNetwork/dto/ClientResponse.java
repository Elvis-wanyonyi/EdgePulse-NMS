package com.wolfcode.MikrotikNetwork.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse{

    private String username;
    private LocalDateTime createdOn;
    private LocalDateTime expiresOn;
    private String plan;
    private String ipAddress;
    private String router;
    private String mpesaReceiptNumber;
    private String phoneNumber;
    private int amount;
    @Enumerated(EnumType.STRING)
    private LoginBy loginBy;
}
