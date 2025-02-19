package com.wolfcode.MikrotikNetwork.dto.pppoe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PPPoESubscription {

    @NotNull(message = "Enter Amount in Ksh.")
    private String amount;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

}
