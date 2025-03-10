package com.wolfcode.MikrotikNetwork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PeriodReportsResponse {

    private String username;
    private String  serviceType;
    private LocalDateTime createdOn;
    private LocalDateTime expiresOn;
    private String loginBy;
    private String planName;
    private String planPrice;
    private String router;

}
