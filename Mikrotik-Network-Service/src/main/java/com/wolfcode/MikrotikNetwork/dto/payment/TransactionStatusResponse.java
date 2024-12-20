package com.wolfcode.MikrotikNetwork.dto.payment;

import lombok.Data;

@Data
public class TransactionStatusResponse {
    private String responseCode;
    private String responseDescription;
    private String resultCode;
    private String resultDescription;
}
