package com.wolfcode.MikrotikNetwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IPPoolDto {

    @NotNull(message = "Enter IP pool name")
    private String poolName;
    @NotBlank(message = "Provide the ip range")
    private String ipRange;
    @NotNull(message = "Enter a valid router")
    private String router;
}
