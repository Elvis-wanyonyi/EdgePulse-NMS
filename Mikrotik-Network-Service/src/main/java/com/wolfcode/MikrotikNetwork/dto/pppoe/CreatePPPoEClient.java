package com.wolfcode.MikrotikNetwork.dto.pppoe;

import com.wolfcode.MikrotikNetwork.dto.ActivePeriod;
import com.wolfcode.MikrotikNetwork.dto.LoginBy;
import com.wolfcode.MikrotikNetwork.dto.ServiceType;
import com.wolfcode.MikrotikNetwork.entity.Plans;
import com.wolfcode.MikrotikNetwork.entity.Routers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePPPoEClient {

    private String account;
    private String fullName;
    private String email;
    private String address;
    private String phone;
    private Integer payment;
    private String mpesaRef;
    private Integer balance;
    private String username;
    private String password;
    private ActivePeriod activePeriod;
    private ServiceType type;
    private LocalDateTime createdOn;
    private LocalDateTime expiresOn;
    private LoginBy loginBy;
    private Plans plan;
    private Routers router;
}
