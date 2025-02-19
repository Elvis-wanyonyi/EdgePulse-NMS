package com.wolfcode.MikrotikNetwork.dto;

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
    private ServiceType serviceType;
    private String router;
    private String phoneNumber;
    private int amount;
    private LoginBy loginBy;
}
