package com.wolfcode.MikrotikNetwork.controller;


import com.wolfcode.MikrotikNetwork.dto.*;
import com.wolfcode.MikrotikNetwork.entity.Routers;
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
@RequestMapping("/hotspot")
public class TikController {

    private final MikrotikService mikrotikService;

    public TikController(MikrotikService mikrotikService) {
        this.mikrotikService = mikrotikService;
    }


    @PostMapping("/connect")
    public String connectUser(@RequestParam String ipAddress,
                              @RequestParam String packageType,
                              @RequestParam String macAddress,
                              @RequestParam String routerName, String phoneNumber,
                              String mpesaReceiptNumber, String amount, String checkoutRequestID) {
        try {
            mikrotikService.connectUser(ipAddress, macAddress, packageType,
                    phoneNumber, mpesaReceiptNumber, amount, routerName, checkoutRequestID);

            System.out.println("Connecting user: " + phoneNumber);
            return "Connecting user....";

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/connect-query")
    public UserCredentials connectUserWithQuery(String checkoutRequestID, @RequestParam String ipAddress, @RequestParam String macAddress,
                                                @RequestParam String packageType, @RequestParam String routerName,
                                                String phoneNumber, String amount, String transactionRefNo) {
        try {
            return mikrotikService.connectUserWithQuery(ipAddress, macAddress, packageType,
                    phoneNumber, amount, routerName, checkoutRequestID, transactionRefNo);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/{routerName}/delete-user/{username}")
    public String deleteUser(@PathVariable String routerName,
                             @PathVariable String username) throws MikrotikApiException {
        mikrotikService.deleteUser(routerName, username);
        return "success";
    }

    @PostMapping("/code")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, String>> loginWithMpesaCode(@RequestBody MpesaCodeRequest mpesaCodeRequest) {
        Map<String, String> userCredentials = mikrotikService.loginWithMpesaCode(mpesaCodeRequest);
        if (userCredentials != null) {
            return ResponseEntity.ok(userCredentials);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @GetMapping("/clients")
    public Page<ClientResponse> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return mikrotikService.getAllClients(page, size);
    }

    @PostMapping("/add-router")
    public String addRouter(@RequestBody RouterRequest routerRequest) {
        mikrotikService.addRouter(routerRequest);
        return "success";
    }

    @GetMapping("/all-routers")
    private List<Routers> getAllRouters() {
        return mikrotikService.getAllRouters();
    }

    @DeleteMapping("/delete/{routerName}")
    public String deleteRouter(@PathVariable String routerName) {
        mikrotikService.deleteRouter(routerName);
        return "success";
    }

    @PutMapping("/{routerName}")
    public String updateRouter(@PathVariable String routerName, @RequestBody RouterRequest routerRequest) {
        mikrotikService.updateRouter(routerName, routerRequest);
        return "success";
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