package com.wolfcode.MikrotikNetwork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevenueReportRequest {

    private String router;
    private String loginBy;
    private String serviceType;
    private LocalDate startDate;
    private LocalDate endDate;
}
