package com.wolfcode.MikrotikNetwork.dto.hotspot;

import com.wolfcode.MikrotikNetwork.dto.ClientStatus;
import com.wolfcode.MikrotikNetwork.dto.LoginBy;
import com.wolfcode.MikrotikNetwork.dto.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveUsersResponse {

    private String phone;
    private String mpesaRef;
    private String username;
    private ServiceType type;
    private LocalDateTime createdOn;
    private LocalDateTime expiresOn;
    private LoginBy loginBy;
    private String plan;
    private ClientStatus status;
    private String router;

}
