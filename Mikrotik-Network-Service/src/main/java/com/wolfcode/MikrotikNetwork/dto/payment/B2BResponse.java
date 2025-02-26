package com.wolfcode.MikrotikNetwork.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class B2BResponse {
    private String ResponseCode;
    private String ResponseDescription;
    private String OriginatorConversationID;
    private String ConversationID;
}
