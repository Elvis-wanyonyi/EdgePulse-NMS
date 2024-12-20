package com.wolfcode.MikrotikNetwork.dto.pppoe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String plan;
    private String payment;
    private String balance;
    private String router;
    private String status;
    private String username;
    private String password;
}
