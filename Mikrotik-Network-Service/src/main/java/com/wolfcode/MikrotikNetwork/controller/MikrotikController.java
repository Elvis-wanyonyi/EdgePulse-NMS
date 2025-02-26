package com.wolfcode.MikrotikNetwork.controller;


import com.wolfcode.MikrotikNetwork.service.MikrotikService;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class MikrotikController {

    private final MikrotikService mikrotikService;

    public MikrotikController(MikrotikService mikrotikService) {
        this.mikrotikService = mikrotikService;
    }


    @GetMapping("/health/{routerName}")
    public ResponseEntity<Map<String, String>> getRouterHealth(@PathVariable String routerName) throws MikrotikApiException {
        Map<String, String> healthData = mikrotikService.getRouterHealth(routerName);
        if (healthData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(healthData);
    }

    @GetMapping("/monitor-traffic/{routerName}")
    public ResponseEntity<String> monitorRouterTraffic(@PathVariable String routerName) {
        try {
            mikrotikService.monitorRouterTraffic(routerName);
            return ResponseEntity.ok("Traffic monitoring started for router: " + routerName);
        } catch (MikrotikApiException e) {
            return ResponseEntity.status(500).body("Failed to monitor traffic: " + e.getMessage());
        }
    }

    @GetMapping("/alerts/{routerName}")
    public ResponseEntity<Map<String, String>> getRouterSystemAlerts(@PathVariable String routerName) throws MikrotikApiException {
        Map<String, String> alertData = mikrotikService.getRouterSystemAlerts(routerName);
        return ResponseEntity.ok(alertData);
    }

    @GetMapping("/logs/{routerName}")
    public ResponseEntity<Map<String, Object>> viewRouterLogs(@PathVariable String routerName) throws MikrotikApiException {
        Map<String, Object> logData = mikrotikService.viewRouterLogs(routerName);
        return ResponseEntity.ok(logData);
    }

    @PostMapping("/action/{routerName}")
    public ResponseEntity<String> changeRouterSystemSettings(@RequestParam String action,
                                                             @PathVariable String routerName) throws MikrotikApiException {
        mikrotikService.changeRouterSystemSettings(action, routerName);
        return ResponseEntity.ok("Action executed successfully");
    }

    @GetMapping("/resources/{routerName}")
    public ResponseEntity<Map<String, Object>> getRouterResources(@PathVariable String routerName) throws MikrotikApiException {
        Map<String, Object> resources = mikrotikService.getRouterResources(routerName);
        if (resources.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/ip-addresses/{routerName}")
    public ResponseEntity<Map<String, Object>> getRouterIpAddresses(@PathVariable String routerName) throws MikrotikApiException {
        Map<String, Object> ipAddresses = mikrotikService.getRouterIpAddresses(routerName);
        if (ipAddresses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(ipAddresses);
    }

}