package com.wolfcode.MikrotikNetwork.controller;


import com.wolfcode.MikrotikNetwork.dto.ClientResponse;
import com.wolfcode.MikrotikNetwork.dto.hotspot.ActiveUsersResponse;
import com.wolfcode.MikrotikNetwork.dto.hotspot.RouterClientResponse;
import com.wolfcode.MikrotikNetwork.service.MikrotikService;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class TikController {

    private final MikrotikService mikrotikService;

    public TikController(MikrotikService mikrotikService) {
        this.mikrotikService = mikrotikService;
    }



    @PostMapping("/{routerName}/delete-user/{username}")
    public String deleteUser(@PathVariable String routerName,
                             @PathVariable String username) throws MikrotikApiException {
        mikrotikService.deleteUser(routerName, username);
        return "success";
    }


    @GetMapping("/clients")
    public Page<ClientResponse> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return mikrotikService.getAllClients(page, size);
    }



    @GetMapping("/totalActive-users/{routerName}")
    public int getTotalActiveClients(@PathVariable String routerName) throws MikrotikApiException {
        return mikrotikService.getTotalActiveClients(routerName);
    }

    @GetMapping("/active-clients/{routerName}")
    public List<ActiveUsersResponse> getAllActiveClients(@PathVariable String routerName) throws MikrotikApiException {
        return mikrotikService.getAllActiveClients(routerName);
    }

    @GetMapping("/total-users/{routerName}")
    public int getTotalConnectedUsers(@PathVariable String routerName) throws MikrotikApiException {
        return mikrotikService.getTotalConnectedUsers(routerName);
    }

    @GetMapping("/users/{routerName}")
    public List<RouterClientResponse> getConnectedUsers(@PathVariable String routerName) throws MikrotikApiException {
        return mikrotikService.getConnectedUsers(routerName);
    }

    // ROUTER MONITORING //
    @GetMapping("/health/{routerName}")
    public ResponseEntity<Map<String, String>> getRouterHealth(@PathVariable String routerName) throws MikrotikApiException {
        Map<String, String> healthData = mikrotikService.getRouterHealth(routerName);
        if (healthData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(healthData);
    }

    @GetMapping("/monitor-traffic")
    public CompletableFuture<Map<String, Object>> monitorTraffic(
            @RequestParam String routerInterface,
            @RequestParam String routerName) throws MikrotikApiException {

        return mikrotikService.getRouterTraffic(routerInterface, routerName);
    }

    @GetMapping("/alerts/{routerName}")
    public ResponseEntity<Map<String, String>> getRouterSystemAlerts(@PathVariable String routerName) throws MikrotikApiException {
        Map<String, String> alertData = mikrotikService.getRouterSystemAlerts(routerName);
        return ResponseEntity.ok(alertData);
    }

    @GetMapping("/logs/{routerName}")
    public ResponseEntity<Map<String, Object>> viewRouterLogs(@PathVariable String routerName)
            throws MikrotikApiException {

        Map<String, Object> logData = mikrotikService.viewRouterLogs(routerName);
        return ResponseEntity.ok(logData);
    }

    @PostMapping("/action/{routerName}")
    public String changeRouterSystemSettings(@RequestParam String action,
                                             @PathVariable String routerName) throws MikrotikApiException {
        mikrotikService.changeRouterSystemSettings(action, routerName);
        return "success";
    }
}