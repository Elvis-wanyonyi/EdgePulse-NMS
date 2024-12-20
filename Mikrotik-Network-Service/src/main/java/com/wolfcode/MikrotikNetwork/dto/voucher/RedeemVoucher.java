package com.wolfcode.MikrotikNetwork.dto.voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedeemVoucher {

    @NotNull(message = "Enter Voucher")
    private String voucherCode;
    @NotBlank(message = "MAC required")
    private String macAddress;
    private String ipAddress;

}
