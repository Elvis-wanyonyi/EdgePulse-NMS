package com.wolfcode.MikrotikNetwork.tenants.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantsResponse {

    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
