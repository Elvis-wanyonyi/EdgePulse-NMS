package com.wolfcode.MikrotikNetwork.dto.network;

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
    private String routerName;

}
