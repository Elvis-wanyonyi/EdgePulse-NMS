package com.wolfcode.MikrotikNetwork.dto.network;

import com.wolfcode.MikrotikNetwork.dto.PlanDuration;
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
public class HotspotPlanDto {

    @NotBlank(message = "Enter router name")
    private Long router;
    @NotNull(message = "Enter plan name")
    private String packageName;
    @NotNull(message = "Choose bandwidth limit")
    private Long bandwidthLimit;
    private String dataLimit;
    @NotNull(message = "Enter plan validity")
    private Integer planValidity;
    private PlanDuration planDuration;
    @NotNull(message = "Enter plan price (ksh)")
    private int price;

}
