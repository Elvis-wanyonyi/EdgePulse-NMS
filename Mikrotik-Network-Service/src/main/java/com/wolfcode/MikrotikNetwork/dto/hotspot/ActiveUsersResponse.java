package com.wolfcode.MikrotikNetwork.dto.hotspot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveUsersResponse {

    private String name;
    private String macAddress;
    private String ipAddress;
    private String uptime;
    private String rxRateTxRate;// the bandwidth consumption
}
