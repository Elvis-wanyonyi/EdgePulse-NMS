package com.wolfcode.MikrotikNetwork.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExternalStkQueryRequest {


    @JsonProperty("BusinessShortCode")
    private String businessShortCode;
    @JsonProperty("Password")
    private String password;
    @JsonProperty("Timestamp")
    private String timestamp;
    @JsonProperty("CheckoutRequestID")
    private String checkoutRequestId;
}
