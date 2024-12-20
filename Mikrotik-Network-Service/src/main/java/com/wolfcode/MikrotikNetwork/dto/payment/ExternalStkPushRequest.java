package com.wolfcode.MikrotikNetwork.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExternalStkPushRequest {


    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("CallBackURL")
    private String callBackURL;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("PartyA")
    private String partyA;

    @JsonProperty("PartyB")
    private String partyB;

    @JsonProperty("AccountReference")
    private String accountReference;

    @JsonProperty("TransactionDesc")
    private String transactionDesc;

    @JsonProperty("BusinessShortCode")
    private String businessShortCode;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("Password")
    private String password;
}
