package com.wolfcode.MikrotikNetwork.dto.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherResponse {

    private String voucherCode;
    private String packageType;
    private VoucherStatus status;
    private LocalDateTime createdAt;
    private String redeemedBy;
}
