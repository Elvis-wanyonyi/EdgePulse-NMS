package com.wolfcode.MikrotikNetwork.tenants.entity;

import com.wolfcode.MikrotikNetwork.tenants.dto.ShortCodeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "payment_details",schema = "public")
public class DarajaPaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tenant;
    @Enumerated(EnumType.STRING)
    private ShortCodeType shortCodeType;
    private String consumerKey;
    private String consumerSecret;
    private String stkPassKey;
    @Column(name = "business_shortcode", nullable = false)
    private String ShortCode;
    private String accountReference;

}
