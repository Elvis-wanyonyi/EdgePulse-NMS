package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.entity.Routers;
import com.wolfcode.MikrotikNetwork.repository.RouterRepository;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RouterConnectionPool {

    private final RouterRepository routerRepository;
    private final Map<String, ApiConnection> connectionMap = new ConcurrentHashMap<>();

    public RouterConnectionPool(RouterRepository routerRepository) {
        this.routerRepository = routerRepository;
    }

    public ApiConnection getConnection(String routerName) throws MikrotikApiException {
        return connectionMap.computeIfAbsent(routerName, rn -> {
            Routers router = routerRepository.findByRouterName(rn)
                    .orElseThrow(() -> new RuntimeException("Router not found: " + rn));
            try {
                ApiConnection conn = ApiConnection.connect(router.getRouterIPAddress());
                conn.login(router.getUsername(), router.getPassword());
                return conn;
            } catch (MikrotikApiException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
