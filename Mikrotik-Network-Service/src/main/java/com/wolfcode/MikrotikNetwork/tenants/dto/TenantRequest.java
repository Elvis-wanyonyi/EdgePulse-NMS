package com.wolfcode.MikrotikNetwork.tenants.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {

    @NotNull(message = "Field is required")
    private String name;
    @Email( message = "Enter valid email")
    private String email;
    private String phone;
    @NotNull(message = "Enter your password")
    private String password;
    @NotNull(message = "Confirm your password")
    private String confirmPassword;

}
