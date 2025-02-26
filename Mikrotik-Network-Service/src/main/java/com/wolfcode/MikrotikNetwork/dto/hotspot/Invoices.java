package com.wolfcode.MikrotikNetwork.dto.hotspot;

import com.wolfcode.MikrotikNetwork.dto.LoginBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoices {

    private String invoice;
    private int amount;
    private String plan;
    private LocalDateTime createdOn;
    private LocalDateTime dueDate;
    private LoginBy method;
}
