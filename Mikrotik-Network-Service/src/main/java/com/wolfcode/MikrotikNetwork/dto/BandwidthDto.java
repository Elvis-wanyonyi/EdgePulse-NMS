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
public class BandwidthDto {

    @NotNull(message = "Field is required")
    private String name;
    @NotBlank(message = "Field is required")
    private String upload;
    @NotBlank(message = "Field is required")
    private String download;

}
