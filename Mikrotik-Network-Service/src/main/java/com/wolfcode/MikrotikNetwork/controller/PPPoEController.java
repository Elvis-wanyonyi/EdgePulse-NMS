package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPOEProfileDto;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoEClientDto;
import com.wolfcode.MikrotikNetwork.service.PPPOEService;
import lombok.RequiredArgsConstructor;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/PPPoE")
public class PPPoEController {

    private final PPPOEService pppoeService;


    @PostMapping
    public String createPPPoEProfile(@RequestBody PPPOEProfileDto profileDto) throws MikrotikApiException {
        pppoeService.createPppoeProfile(profileDto);
        return "Success!";
    }

    @GetMapping
    public List<PPPoEClientDto> getPppoeProfiles(){
        return pppoeService.getAllPppoeClients();
    }

    @DeleteMapping("/{router}/{name}")
    public String deletePppoeProfile(@PathVariable String router, @PathVariable String name) throws MikrotikApiException {
        pppoeService.deletePppoeProfile(router, name);
        return "Success";
    }

    @PutMapping("/{name}")
    public String updatePppoeProfile(@PathVariable String name, @RequestBody PPPOEProfileDto profileDto) {
        pppoeService.updatePppoeProfile(name,profileDto);
        return "Profile updated";
    }

    @PostMapping("/add-client")
    public String addPPPoEClient(@RequestBody PPPoEClientDto request) throws MikrotikApiException {
        pppoeService.addPppoeClient(request);
        return "Client added successfully";
    }

    @GetMapping("/clients")
    public List<PPPoEClientDto> getAllPppoeClients(){
        return pppoeService.getPppoeClients();
    }
    @GetMapping("/clients/{id}")
    public PPPoEClientDto getPppoeClientById(@PathVariable Long id){
        return pppoeService.getClientById(id);
    }
}
