package com.wolfcode.MikrotikNetwork.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemItem {

    @NotNull(message = "Field is required")
    @JsonProperty("Name")
    private String name;

    @NotNull(message = "Field is required")
    @JsonProperty("Value")
    private String value;
}