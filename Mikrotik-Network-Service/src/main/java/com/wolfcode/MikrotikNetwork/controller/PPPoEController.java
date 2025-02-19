package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPOEProfileDto;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoEClientDto;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoESubscription;
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

    @GetMapping("/clients")
    public List<PPPoEClientDto> getPppoeClients(){
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

    @GetMapping("/clients/{id}")
    public PPPoEClientDto getPppoeClientById(@PathVariable Long id){
        return pppoeService.getClientById(id);
    }

    @PutMapping("/edit-account/{id}")
    public String editPppoeClientAccount(@PathVariable Long id, @RequestBody PPPoEClientDto clientDto) throws MikrotikApiException {
        pppoeService.editClientAccount(id, clientDto);
        return "Account updated";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivatePppoeClient(@PathVariable Long id) throws MikrotikApiException {
        pppoeService.deactivateClient(id);
        return "Client deactivated";
    }

    @DeleteMapping("/delete/{id}")
    public String deletePppoeClient(@PathVariable Long id) throws MikrotikApiException {
        pppoeService.removeClient(id);
        return "Client removed";
    }

    @PostMapping("/Rewrite-account/{id}")
    public String reWriteAccount(@PathVariable Long id) throws MikrotikApiException {
        pppoeService.reWriteAccount(id);
        return "Account reWrite successfully";
    }

    @PostMapping("/recharge-account/{id}")
    public String rechargePPPOEAccount(@PathVariable Long id) throws MikrotikApiException {
        pppoeService.rechargeClientAccount(id);
        return "Account recharged successfully";
    }

    @PostMapping("/subscribe/{clientId}")
    public String subscribe(@PathVariable Long clientId, @RequestBody PPPoESubscription subscription) throws MikrotikApiException {
        pppoeService.clientSubscription(clientId, subscription);
        return "Success!";
    }

}

