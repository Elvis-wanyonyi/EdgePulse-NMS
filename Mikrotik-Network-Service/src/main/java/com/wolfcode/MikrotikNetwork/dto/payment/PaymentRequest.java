package com.wolfcode.MikrotikNetwork.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Enter Amount in Ksh.")
    private String amount;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Required")
    private String ipAddress;
    @NotBlank(message = "Required")
    private String macAddress;
    @NotBlank(message = "Required")
    private String packageType;
    private String routerName;
}
