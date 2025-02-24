package com.wolfcode.MikrotikNetwork.dto.payment;

import com.wolfcode.MikrotikNetwork.tenants.dto.ShortCodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MpesaPaymentGateway {

    private ShortCodeType shortCodeType;
    private String consumerKey;
    private String consumerSecret;
    private String stkPassKey;
    private String shortCode;
}

