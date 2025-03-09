package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.pppoe.CreatePPPoEClient;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPOEProfileDto;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoEClientsResponse;
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


    @PostMapping("/profile")
    public String createPPPoEProfile(@RequestBody PPPOEProfileDto profileDto) throws MikrotikApiException {
        pppoeService.createPppoeProfile(profileDto);
        return "Success!";
    }

    @GetMapping("/clients")
    public List<PPPoEClientsResponse> getPppoeClients(){
        return pppoeService.getAllPppoeClients();
    }

    @GetMapping("/clients/{id}")
    public PPPoEClientsResponse getPppoeClientById(@PathVariable("id") Long id){
        return pppoeService.getPppoeClientById(id);
    }

    @GetMapping("/clients/total-active")
    public int getTotalActivePppoeClients(){
        return pppoeService.getTotalActivePppoeClients();
    }

    @GetMapping("/clients/active")
    public List<PPPoEClientsResponse> getActivePppoeClients(){
        return pppoeService.getActivePppoeClients();
    }

    @DeleteMapping("/{id}")
    public String deletePppoeProfile(@PathVariable Long id) throws MikrotikApiException {
        pppoeService.deletePppoeProfile(id);
        return "Success";
    }

    @PutMapping("/{id}")
    public String updatePppoeProfile(@PathVariable Long id, @RequestBody PPPOEProfileDto profileDto) {
        pppoeService.updatePppoeProfile(id,profileDto);
        return "Profile updated";
    }

    @PostMapping("/add-client")
    public String addPPPoEClient(@RequestBody CreatePPPoEClient request) throws MikrotikApiException {
        pppoeService.addPppoeClient(request);
        return "Client added successfully";
    }

    @PutMapping("/edit-account/{id}")
    public String editPppoeClientAccount(@PathVariable Long id, @RequestBody CreatePPPoEClient request) throws MikrotikApiException {
        pppoeService.editClientAccount(id, request);
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
    public String subscribe(@PathVariable Long clientId, @RequestBody PPPoESubscription subscription) {
        pppoeService.clientSubscription(clientId, subscription);
        return "Success!";
    }

}

