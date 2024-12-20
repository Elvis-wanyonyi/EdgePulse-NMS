package com.wolfcode.MikrotikNetwork.dto.pppoe;

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
    private String planValidity;
    private String bandwidthLimit;
    private String ipPool;
    private String router;
}
