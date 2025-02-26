package com.wolfcode.MikrotikNetwork.dto.hotspot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientDetailsResponse {

    private String account;
    private String fullName;
    private String email;
    private String address;
    private String phone;
    private Integer payment;
    private Integer balance;
    private String mpesaRef;
    private String username;
    private String  type;
    private LocalDateTime createdOn;
    private LocalDateTime expiresOn;
    private String dataUsage;
    private String loginBy;
    private String status;
    private String plan;
    private String router;

}
