package com.wolfcode.MikrotikNetwork.entity;

import com.wolfcode.MikrotikNetwork.dto.voucher.VoucherStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String voucherCode;
    @Enumerated(EnumType.STRING)
    private VoucherStatus status;
    private LocalDateTime createdAt;
    private String redeemedBy;
    private String ipAddress;
    private LocalDateTime expiryDate;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "plan", referencedColumnName = "package_plan")
    private PackagePlans plan;

    @ManyToOne(fetch = FetchType.EAGER)
    private Routers router;
}
