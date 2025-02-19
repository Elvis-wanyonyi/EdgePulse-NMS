package com.wolfcode.MikrotikNetwork.controller;


import com.wolfcode.MikrotikNetwork.service.MikrotikService;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class TikController {

    private final MikrotikService mikrotikService;

    public TikController(MikrotikService mikrotikService) {
        this.mikrotikService = mikrotikService;
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