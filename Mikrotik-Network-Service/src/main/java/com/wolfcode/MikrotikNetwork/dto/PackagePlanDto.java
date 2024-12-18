package com.wolfcode.MikrotikNetwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackagePlanDto {

    @NotBlank(message = "Enter router name")
    private String routerName;
    @NotNull(message = "Enter plan name")
    private String packageName;
    @NotNull(message = "Choose bandwidth limit")
    private String bandwidthLimit;
    private String dataLimit;
    @NotNull(message = "Enter plan validity")
    private Integer planValidity;
    private PlanDuration planDuration;
    @NotNull(message = "Enter plan price (ksh)")
    private int price;

}
