package com.wolfcode.MikrotikNetwork.dto.pppoe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wolfcode.MikrotikNetwork.dto.ActivePeriod;
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
    private String fullName;
    private String email;
    private String address;
    private String phone;
    @NotNull(message = "Select a pppoe plan")
    private Long plan;
    @NotNull(message = "Select router")
    private Long router;
    private String username;
    private String password;
    private ActivePeriod activePeriod;

}
