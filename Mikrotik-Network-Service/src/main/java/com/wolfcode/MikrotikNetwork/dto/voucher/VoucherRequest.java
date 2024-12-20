package com.wolfcode.MikrotikNetwork.dto.voucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherRequest {

    private String voucherCode;
    private VoucherStatus status;
    private String redeemedBy;

}
