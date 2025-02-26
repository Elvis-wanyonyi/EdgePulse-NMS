package com.wolfcode.MikrotikNetwork.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mpesa")
public class B2BMpesaConfig {

    private String b2bRequestUrl;

    // The initiator's name registered with Safaricom
    private String initiatorName;

    // The plain text OP PIN (will be encrypted before sending)
    private String initiatorPassword;

    // Path to the certificate used for encryption (in PEM or pfx converted to PEM)
    private String certificatePath;

    // B2B command ID, e.g., "BusinessPayBill"
    private String commandId;

    // Identifier types (usually "4" for till numbers)
    private String senderIdentifierType;
    private String receiverIdentifierType;

    // Organization's proxy paybill number (PartyA)
    private String orgPaybillNumber;

    // Callback URLs for B2B transactions
    private String b2bResultUrl;
    private String b2bTimeoutUrl;
}
