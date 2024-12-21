package com.wolfcode.MikrotikNetwork.dto.network;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BandwidthDto {

    private String name;
    private Integer uploadSpeed;
    private String uploadUnit;
    private Integer downloadSpeed;
    private String downloadUnit;
    @NotNull(message = "Choose router")
    private Long router;

}
