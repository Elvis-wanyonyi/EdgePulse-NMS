package com.wolfcode.MikrotikNetwork.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MpesaC2BRequest {

    @JsonProperty("ShortCode")
    private String shortCode;

    @JsonProperty("Msisdn")
    private String msisdn;

    @JsonProperty("BillRefNumber")
    private String billRefNumber;

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("CommandID")
    private String commandID;
}
