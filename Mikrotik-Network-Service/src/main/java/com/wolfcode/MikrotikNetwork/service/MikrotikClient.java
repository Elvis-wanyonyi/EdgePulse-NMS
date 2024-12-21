package com.wolfcode.MikrotikNetwork.service;


import com.wolfcode.MikrotikNetwork.dto.ClientResponse;
import com.wolfcode.MikrotikNetwork.dto.hotspot.ActiveUsersResponse;
import com.wolfcode.MikrotikNetwork.dto.hotspot.RouterClientResponse;
import com.wolfcode.MikrotikNetwork.dto.network.IPPoolDto;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoEClientDto;
import com.wolfcode.MikrotikNetwork.entity.PPPoEPlans;
import com.wolfcode.MikrotikNetwork.entity.Routers;
import com.wolfcode.MikrotikNetwork.repository.RouterRepository;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import me.legrange.mikrotik.ResultListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class MikrotikClient {

    private final RouterRepository routerRepository;
    private ApiConnection connection;

    public MikrotikClient(RouterRepository routerRepository) {
        this.routerRepository = routerRepository;
    }


    public void connectRouter(String routerName) throws MikrotikApiException {
        Routers routerConfig = routerRepository.findByRouterName(routerName);
        if (routerConfig == null) {
            throw new MikrotikApiException("Router not found: " + routerName);
        }

        try {
            connection = ApiConnection.connect(routerConfig.getRouterIPAddress());

            connection.login(routerConfig.getUsername(), routerConfig.getPassword());
        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Error connecting to Mikrotik API", e);
        }
    }


    public void createHotspotUser(String ipAddress,
                                  String macAddress, String profile, String uptimeLimit, String routerName)
            throws MikrotikApiException {
        connectRouter(routerName);
        String command = String.format(
                "/ip/hotspot/user/add address=%s mac-address=%s profile=%s limit-uptime=%s",
                ipAddress, macAddress, profile, uptimeLimit
        );
        connection.execute(command);
    }

    public void redeemVoucher(String voucherCode, String ipAddress, String macAddress,
                              String profile, String uptimeLimit, String routerName)
            throws MikrotikApiException {
        connectRouter(routerName);
        String command = String.format(
                "/ip/hotspot/user/add name=%s password=%s address=%s mac-address=%s profile=%s limit-uptime=%s",
                voucherCode, voucherCode, ipAddress, macAddress, profile, uptimeLimit
        );
        System.out.println("Executing command : " + command);

        connection.execute(command);
    }

    public List<ClientResponse> getTotalActiveClients(String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        String command = "/ip/hotspot/active/print";
        List<Map<String, String>> response = connection.execute(command);

        List<ClientResponse> activeClients = new ArrayList<>();
        for (Map<String, String> clientData : response) {
            ClientResponse activeClient = new ClientResponse();
            activeClient.setIpAddress(clientData.get("address"));
            activeClient.setUsername(clientData.get("mac-address"));
            activeClients.add(activeClient);
        }
        return activeClients;
    }

    public List<ActiveUsersResponse> getAllActiveClients(String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        String command = "/ip/hotspot/active/print";
        List<Map<String, String>> response = connection.execute(command);

        List<ActiveUsersResponse> activeClients = new ArrayList<>();
        for (Map<String, String> clientData : response) {
            ActiveUsersResponse activeClient = new ActiveUsersResponse();

            activeClient.setName(clientData.get("user"));
            activeClient.setIpAddress(clientData.get("address"));
            activeClient.setMacAddress(clientData.get("mac-address"));
            activeClient.setUptime(clientData.get("uptime"));
            activeClient.setRxRateTxRate(clientData.get("limit-uptime"));
            activeClients.add(activeClient);
        }
        return activeClients;

    }

    public List<ClientResponse> getTotalConnectedUsers(String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        String command = "/ip/hotspot/user/print";
        List<Map<String, String>> response = connection.execute(command);

        List<ClientResponse> connectedUsers = new ArrayList<>();
        for (Map<String, String> clientData : response) {
            ClientResponse connectedUser = new ClientResponse();
            connectedUser.setIpAddress(clientData.get("address"));
            connectedUser.setUsername(clientData.get("mac-address"));
            connectedUsers.add(connectedUser);
        }
        return connectedUsers;
    }

    public List<RouterClientResponse> getConnectedUsers(String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        String command = "/ip/hotspot/user/print";
        List<Map<String, String>> response = connection.execute(command);

        List<RouterClientResponse> connectedUsers = new ArrayList<>();
        for (Map<String, String> clientData : response) {
            RouterClientResponse connectedUser = new RouterClientResponse();
            connectedUser.setName(clientData.getOrDefault("name", "Unknown"));
            connectedUser.setProfile(clientData.getOrDefault("profile", "Unknown"));
            connectedUser.setIpAddress(clientData.getOrDefault("address", "No IP Assigned"));
            connectedUser.setMacAddress(clientData.getOrDefault("mac-address", "No MAC Assigned"));

            connectedUsers.add(connectedUser);
        }
        return connectedUsers;
    }

    public Map<String, String> getRouterHealth(String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        Map<String, String> healthData = new HashMap<>();
        List<Map<String, String>> results = connection.execute("/system/resource/print");
        if (!results.isEmpty()) {
            Map<String, String> result = results.get(0);
            healthData.put("uptime", result.get("uptime"));
            healthData.put("cpuLoad", result.get("cpu-load") + "%");
            healthData.put("memoryUsage", result.get("free-memory") + " / " + result.get("total-memory"));
        }
        System.out.println(healthData);
        return healthData;
    }

    public CompletableFuture<Map<String, Object>> getRouterTraffic(String routerInterface, String routerName) throws MikrotikApiException {
        connectRouter(routerName);

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        connection.execute(
                "/interface/monitor-traffic interface=" + routerInterface + " once",
                new ResultListener() {

                    @Override
                    public void receive(Map<String, String> result) {
                        System.out.println("Traffic Data: " + result);

                        Map<String, Object> trafficData = new HashMap<>(result);
                        future.complete(trafficData);
                    }

                    @Override
                    public void error(MikrotikApiException e) {
                        future.completeExceptionally(e);
                    }

                    @Override
                    public void completed() {
                        System.out.println("Asynchronous command has finished");
                    }
                }
        );

        return future;
    }

    public Map<String, String> getRouterSystemAlerts(String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        Map<String, String> alerts = new HashMap<>();
        List<Map<String, String>> logEntries = connection.execute("/log/print");

        for (Map<String, String> logEntry : logEntries) {
            String message = logEntry.get("message");
            if (message.contains("login failure")) {
                alerts.put("unauthorizedAccess", "Unauthorized login attempt detected.");
            }
        }

        alerts.put("downtime", "No downtime detected in the logs.");

        return alerts;
    }

    public Map<String, Object> viewRouterLogs(String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        Map<String, Object> logs = new HashMap<>();
        List<Map<String, String>> logEntries = connection.execute("/log/print");

        for (int i = 0; i < logEntries.size(); i++) {
            logs.put("log" + (i + 1), logEntries.get(i).get("message"));
        }

        return logs;
    }

    public void changeRouterSystemSettings(String action, String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        try {
            if ("reboot".equalsIgnoreCase(action)) {
                connection.execute("/system/reboot");
            } else if ("shutdown".equalsIgnoreCase(action)) {
                connection.execute("/system/shutdown");
            } else {
                System.out.println("Invalid action specified: " + action);
            }
        } catch (MikrotikApiException e) {
            System.err.println("Error executing action '" + action + "' on router '" + routerName + "': " + e.getMessage());
            throw e;
        }
    }

    public void deleteUser(String routerName, String username) throws MikrotikApiException {
        connectRouter(routerName);
        try {
            String findCommand = "/ip/hotspot/user/print where name=" + username;
            List<Map<String, String>> users = connection.execute(findCommand);

            if (users.isEmpty()) {
                throw new MikrotikApiException("User not found: " + username);
            }
            String userId = users.get(0).get(".id");

            String deleteCommand = "/ip/hotspot/user/remove .id=" + userId;
            connection.execute(deleteCommand);

        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Failed to remove hotspot user: " + username, e);
        }
    }

    public void removeExpiredUser(String routerName, String username) throws MikrotikApiException {
        connectRouter(routerName);
        try {
            String findCommand = "/ip/hotspot/user/print where name=" + username;
            List<Map<String, String>> users = connection.execute(findCommand);

            if (users.isEmpty()) {
                throw new MikrotikApiException("User not found: " + username);
            }
            String userId = users.get(0).get(".id");

            String removeCommand = "/ip/hotspot/user/remove .id=" + userId;
            connection.execute(removeCommand);

        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Failed to remove expired hotspot user: " + username, e);
        }
    }


    public void createIPPool(IPPoolDto ipPoolDto, String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        System.out.println(ipPoolDto);
        try {
            String command = String.format(
                    "/ip/pool/add name=%s ranges=%s", ipPoolDto.getPoolName(), ipPoolDto.getIpRange()
            );
            System.out.println(command);
            connection.execute(command);

        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to create Ip pool");
        }
    }

    public void deleteIPPool(String name, String router) throws MikrotikApiException {
        connectRouter(router);
        try {
            List<Map<String, String>> results = connection.execute("/ip/pool/print where name=" + name);
            if (results.isEmpty()) {
                throw new RuntimeException("IP pool not found: " + name);
            }
            String id = results.get(0).get(".id");

            connection.execute("/ip/pool/remove .id=" + id);
        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Failed to delete IP pool: " + e.getMessage(), e);
        }
    }

    public void createPppoeProfile(PPPoEPlans plan) throws MikrotikApiException {
        connectRouter(plan.getRouter().getRouterName());
        try {
            String poolName = plan.getIpPool().getPoolName();
            String rateLimit = plan.getBandwidthLimit().concatBandwidthLimit();

            String command = String.format(
                    "/ppp/profile/add name=%s local-address=%s remote-address=%s" +
                            " dns-server=8.8.8.8,8.8.4.4 rate-limit=%s",
                    plan.getName(), poolName, poolName, rateLimit);

            connection.execute(command);
        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Failed to create PPPoE profile: " + e.getMessage(), e);
        }
    }

    public void removePppoeProfile(String name, String router) throws MikrotikApiException {
        connectRouter(router);
        try {
            List<Map<String, String>> results = connection.execute("/ppp/profile/print where name=" + name);
            if (results.isEmpty()) {
                throw new RuntimeException("PPPoE profile not found: " + name);
            }
            String id = results.get(0).get(".id");

            connection.execute("/ppp/profile/remove .id=" + id);
        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Failed to remove PPPoE profile: " + e.getMessage(), e);
        }
    }

    public void createPppoeClient(PPPoEClientDto request, String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        try {
            String command = String.format(
                    "/ppp/secret/add name=%s password=%s service=pppoe profile=%s", request.getUsername(),
                    request.getPassword(), request.getPlan());
            connection.execute(command);
        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Failed to create PPPoE client: " + e.getMessage(), e);
        }
    }
}