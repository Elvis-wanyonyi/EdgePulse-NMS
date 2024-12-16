package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.PPPoEClientRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/PPPoE")
public class PPPoEController {

    @PostMapping
    public String addPPPoEClient(@RequestBody PPPoEClientRequest ppPoEClientRequest) {
        return "Client added successfully";
    }
}
