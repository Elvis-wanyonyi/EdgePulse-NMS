package com.wolfcode.MikrotikNetwork.tenants.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {

    private String name;
    private String email;
    private String phone;
    private String username;
    private String password;

}
