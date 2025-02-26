package com.wolfcode.MikrotikNetwork.dto.hotspot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientLogs {

    private LocalDate time;
    private String topic;
    private String message;
    private String router;
}
