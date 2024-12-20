package com.wolfcode.MikrotikNetwork.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mpesa.daraja")
public class MpesaConfig {

    private String consumerKey;
    private String consumerSecret;
    private String grantType;
    private String oauthEndpoint;
    private String stkPassKey;
    private String stkPushShortCode;
    private String stkPushRequestUrl;
    private String stkPushRequestCallbackUrl;
    private String stkPushQueryUrl;

}
