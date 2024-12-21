package com.wolfcode.MikrotikNetwork.dto.pppoe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PPPoEClientDto {

    private String account;
    private String name;
    private String phone;
    @NotNull(message = "Choose a pppoe plan")
    private Long plan;
    private String payment;
    private String balance;
    @NotNull(message = "Choose router")
    private Long router;
    private String username;
    private String password;
}
