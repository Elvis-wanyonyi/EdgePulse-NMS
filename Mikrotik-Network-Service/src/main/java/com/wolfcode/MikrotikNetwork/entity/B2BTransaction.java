package com.wolfcode.MikrotikNetwork.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "b2b_transactions", schema = "public")
public class B2BTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String checkoutRequestId;
    private String amount;
    private String conversationId;
    private String responseCode;
    private String responseDescription;
    private LocalDateTime createdAt;

}
