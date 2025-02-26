package com.wolfcode.MikrotikNetwork.service;


import com.wolfcode.MikrotikNetwork.dto.ClientResponse;
import com.wolfcode.MikrotikNetwork.dto.ClientStatus;
import com.wolfcode.MikrotikNetwork.dto.hotspot.ActiveUsersResponse;
import com.wolfcode.MikrotikNetwork.dto.hotspot.RouterClientResponse;
import com.wolfcode.MikrotikNetwork.dto.network.IPPoolDto;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoEClientDto;
import com.wolfcode.MikrotikNetwork.entity.Clients;
import com.wolfcode.MikrotikNetwork.entity.Plans;
import com.wolfcode.MikrotikNetwork.entity.Routers;
import com.wolfcode.MikrotikNetwork.entity.UserSession;
import com.wolfcode.MikrotikNetwork.repository.ClientsRepository;
import com.wolfcode.MikrotikNetwork.repository.RouterRepository;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import me.legrange.mikrotik.ResultListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class MikrotikClient {

    private final RouterRepository routerRepository;
    private ApiConnection connection;
    private ClientsRepository clientsRepository;

    public MikrotikClient(RouterRepository routerRepository) {
        this.routerRepository = routerRepository;
    }


    public void connectRouter(String routerName) throws MikrotikApiException {
        Routers routerConfig = routerRepository.findByRouterName(routerName)
                .orElseThrow(() -> new MikrotikApiException("Router not found"));

        try {
            connection = ApiConnection.connect(routerConfig.getRouterIPAddress());

            connection.login(routerConfig.getUsername(), routerConfig.getPassword());
        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Error connecting to Mikrotik API", e);
        }
    }


    public void createHotspotUser(String ipAddress,
                                  String macAddress, String profile, String uptimeLimit, Routers routers)
            throws MikrotikApiException {
        connectRouter(routers.getRouterName());
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
            String username = clientData.get("user");

            Clients client = clientsRepository.findByUsername(username)
                    .orElse(null);

            ActiveUsersResponse activeClient = new ActiveUsersResponse();
            activeClient.setUsername(username);

            if (client != null) {
                activeClient.setPhone(client.getPhone());
                activeClient.setMpesaRef(client.getMpesaRef());
                activeClient.setType(client.getType());
                activeClient.setCreatedOn(client.getCreatedOn());
                activeClient.setExpiresOn(client.getExpiresOn());
                activeClient.setLoginBy(client.getLoginBy());
                activeClient.setPlan(client.getPlan().getPlanName());
                activeClient.setRouter(client.getRouter().getRouterName());
                activeClient.setStatus(ClientStatus.ACTIVE);
            }
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
            connectedUser.setUsername(clientData.get("mac-address"));
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

    public CompletableFuture<Map<String, Object>> monitorRouterTraffic(String interfaceName) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        try {
            connection.execute("/interface/monitor-traffic interface=" + interfaceName + " once", new ResultListener() {
                @Override
                public void receive(Map<String, String> result) {
                    future.complete(new HashMap<>(result));
                }
                @Override
                public void error(MikrotikApiException e) {
                    future.completeExceptionally(e);
                }
                @Override
                public void completed() {
                    System.out.println("Traffic monitoring completed.");
                }
            });

        } catch (MikrotikApiException e) {
            future.completeExceptionally(e);
        }
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
            throw new MikrotikApiException("Failed to "+ action + " router '" + routerName + "'", e);
        }
    }

    public void deleteUser(UserSession user) throws MikrotikApiException {
        connectRouter(user.getRouterName());
        try {
            String username = user.getUsername();
            String findCommand = "/ip/hotspot/user/print where name=" + username;
            List<Map<String, String>> users = connection.execute(findCommand);

            if (users.isEmpty()) {
                throw new MikrotikApiException("User not found: " + username);
            }
            String userId = users.get(0).get(".id");

            String deleteCommand = "/ip/hotspot/user/remove .id=" + userId;
            connection.execute(deleteCommand);

        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Failed to remove hotspot user ");
        }
    }

    public void removeExpiredUser(UserSession session) throws MikrotikApiException {
        connectRouter(session.getRouterName());
        String username = session.getUsername();
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


//PPPOE OPS & MANAGEMENT

    public void createPppoeProfile(Plans plan) throws MikrotikApiException {
        connectRouter(plan.getRouter().getRouterName());
        try {
            String poolName = plan.getIpPool().getPoolName();
            String rateLimit = plan.getBandwidthLimit().concatBandwidthLimit();

            String command = String.format(
                    "/ppp/profile/add name=%s local-address=%s remote-address=%s" +
                            " dns-server=8.8.8.8,8.8.4.4 rate-limit=%s",
                    plan.getPlanName(), poolName, poolName, rateLimit);

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

    public void editPppoeClient(Clients client) throws MikrotikApiException {
        connectRouter(client.getRouter().getRouterName());
        try {
            var id = findCommand(client);

            String editCommand = String.format("/ppp/secret/set =.id=%s =password=%s", id, client.getPassword());
            connection.execute(editCommand);
        } catch (Exception e) {
            throw new MikrotikApiException("Failed to edit PPPoE client: " + e.getMessage(), e);
        }
    }

    private String findCommand(Clients client) throws MikrotikApiException {
        String findCommand = String.format("/ppp/secret/print where name=%s", client.getUsername());
        List<Map<String, String>> results = connection.execute(findCommand);

        if (results.isEmpty()) {
            throw new MikrotikApiException("Client not found: " + client.getUsername());
        }
        return results.get(0).get(".id");
    }

    public void deactivateClient(Clients client) throws MikrotikApiException {
        connectRouter(client.getRouter().getRouterName());
        try {
            var id = findCommand(client);

            String disableCommand = String.format("/ppp/secret/disable =.id=%s", id);
            connection.execute(disableCommand);
        } catch (Exception e) {
            throw new MikrotikApiException("Failed to deactivate PPPoE client: " + e.getMessage(), e);
        }
    }

    public void deleteClient(Optional<Clients> client) throws MikrotikApiException {
        if (client.isPresent()) {
            connectRouter(client.get().getRouter().getRouterName());
            try {
                String findCommand = String.format("/ppp/secret/print where name=%s", client.get().getUsername());
                List<Map<String, String>> results = connection.execute(findCommand);

                if (results.isEmpty()) {
                    throw new MikrotikApiException("Client not found: " + client.get().getUsername());
                }
                String id = results.get(0).get(".id");

                String removeCommand = String.format("/ppp/secret/remove =.id=%s", id);
                connection.execute(removeCommand);
            } catch (Exception e) {
                throw new MikrotikApiException("Failed to delete PPPoE client: " + e.getMessage(), e);
            }
        } else {
            throw new MikrotikApiException("No client information provided.");
        }
    }

    public void reWriteAccount(Clients client) throws MikrotikApiException {
        connectRouter(client.getRouter().getRouterName());
        try {
            var id = findCommand(client);
            String enableCommand = String.format("/ppp/secret/enable =.id=%s", id);
            connection.execute(enableCommand);

            String updateCommand = String.format("/ppp/secret/set =.id=%s =password=%s", id, client.getPassword());
            connection.execute(updateCommand);
        } catch (Exception e) {
            throw new MikrotikApiException("Failed to rewrite PPPoE account: " + e.getMessage(), e);
        }
    }

    public void disconnectOverdueClients(UserSession session) throws MikrotikApiException {
        connectRouter(session.getRouterName());
        try {
            String findCommand = String.format("/ppp/secret/print where name=%s", session.getUsername());
            List<Map<String, String>> results = connection.execute(findCommand);

            if (results.isEmpty()) {
                throw new MikrotikApiException("PPPoE client not found: " + session.getUsername());
            }
            String id = results.get(0).get(".id");
            String disableCommand = String.format("/ppp/secret/disable .id=%s", id);
            connection.execute(disableCommand);

            System.out.println("Disabled PPPoE client: " + session.getUsername());
        } catch (Exception e) {
            throw new MikrotikApiException("Failed to deactivate PPPoE client: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getRouterResources(String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        Map<String, Object> resourceData = new HashMap<>();
        try {
            List<Map<String, String>> result = connection.execute("/system/resource/print");

            if (!result.isEmpty()) {
                Map<String, String> data = result.get(0);
                resourceData.put("uptime", data.get("uptime"));
                resourceData.put("cpuLoad", data.get("cpu-load"));
                resourceData.put("freeMemory", data.get("free-memory"));
                resourceData.put("totalMemory", data.get("total-memory"));
                resourceData.put("freeHddSpace", data.get("free-hdd-space"));
                resourceData.put("totalHddSpace", data.get("total-hdd-space"));
                resourceData.put("architectureName", data.get("architecture-name"));
                resourceData.put("boardName", data.get("board-name"));
                resourceData.put("version", data.get("version"));
            }
        } catch (MikrotikApiException e) {
            throw new MikrotikApiException("Error fetching router resources:");
        }
        return resourceData;
    }

    public Map<String, Object> getRouterIpAddresses(String routerName) throws MikrotikApiException {
        connectRouter(routerName);
        //command to fetch ip addresses
        return null;
    }
}