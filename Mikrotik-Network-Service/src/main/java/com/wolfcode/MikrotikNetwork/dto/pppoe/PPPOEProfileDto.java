package com.wolfcode.MikrotikNetwork.dto.pppoe;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PPPOEProfileDto {

    private String name;
    private Integer planValidity;
    @NotNull(message = "Enter bandwidth Limit")
    private Long bandwidthLimit;
    @NotNull(message = "Choose an ip pool")
    private Long ipPool;
    @NotNull(message = "Router is required")
    private Long router;
}
