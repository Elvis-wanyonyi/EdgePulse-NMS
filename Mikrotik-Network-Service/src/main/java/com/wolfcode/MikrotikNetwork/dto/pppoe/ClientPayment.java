package com.wolfcode.MikrotikNetwork.dto.pppoe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientPayment {

    private String account;
    private String phone;
    private String payment;
    private String balance;
}
