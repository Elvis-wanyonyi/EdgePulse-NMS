package com.wolfcode.MikrotikNetwork.dto.payment;

import lombok.Data;

@Data
public class TransactionStatusRequest {
    private String initiator;
    private String securityCredential;
    private String commandID = "TransactionStatusQuery";
    private String transactionID;
    private String partyA;
    private String identifierType = "4";
    private String remarks = "Checking transaction status";
    private String queueTimeOutURL;
    private String resultURL;
    private String occasion;
}
