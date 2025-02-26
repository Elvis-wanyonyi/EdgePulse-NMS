package com.wolfcode.MikrotikNetwork.dto.network;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouterRequest {

    @NotNull(message = "Router name is required")
    private String routerName;
    @NotBlank(message = "Enter router ip address")
    private String routerIPAddress;
    private String routerInterface;
    private String username;
    private String password;
    private String description;

}
