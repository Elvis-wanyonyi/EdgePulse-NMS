package com.wolfcode.MikrotikNetwork.dto.payment;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2BRequest {
    private String Initiator;
    private String SecurityCredential;
    private String CommandID;
    private String SenderIdentifierType;
    private String RecieverIdentifierType;
    private String Amount;
    private String PartyA;
    private String PartyB;
    private String Remarks;
    private String QueueTimeOutURL;
    private String ResultURL;
}
