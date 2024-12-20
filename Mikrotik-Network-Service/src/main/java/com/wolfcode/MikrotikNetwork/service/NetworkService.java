package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.IPPoolDto;
import com.wolfcode.MikrotikNetwork.dto.network.RouterRequest;
import com.wolfcode.MikrotikNetwork.entity.IPPool;
import com.wolfcode.MikrotikNetwork.entity.Routers;
import com.wolfcode.MikrotikNetwork.repository.IPPoolRepository;
import com.wolfcode.MikrotikNetwork.repository.RouterRepository;
import lombok.RequiredArgsConstructor;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NetworkService {

    private final MikrotikClient mikrotikClient;
    private final IPPoolRepository poolRepository;
    private final RouterRepository routerRepository;


    public void addRouter(RouterRequest routerRequest) {
        Routers router = Routers.builder()
                .routerName(routerRequest.getRouterName())
                .routerIPAddress(routerRequest.getRouterIPAddress())
                .username(routerRequest.getUsername())
                .password(routerRequest.getPassword())
                .description(routerRequest.getDescription())
                .build();
        routerRepository.save(router);
    }

    public void updateRouter(String routerName, RouterRequest routerRequest) {
        Routers router = routerRepository.findByRouterName(routerName);
        if (router != null) {
            router.setRouterName(routerRequest.getRouterName());
            router.setRouterIPAddress(routerRequest.getRouterIPAddress());
            router.setUsername(routerRequest.getUsername());
            router.setPassword(routerRequest.getPassword());
            router.setDescription(routerRequest.getDescription());
            routerRepository.save(router);
        } else {
            throw new IllegalArgumentException("Router not found : " + routerName);
        }
    }

    public void deleteRouter(String routerName) {
        routerRepository.deleteByRouterName(routerName);
    }

    public List<Routers> getAllRouters() {
        return routerRepository.findAll();
    }


    public void createIPPool(IPPoolDto ipPoolDto) throws MikrotikApiException {
        Routers router = routerRepository.findByRouterName(ipPoolDto.getRouter());
        if (router == null) {
            throw new IllegalArgumentException("Router not found");
        }
        mikrotikClient.createIPPool(ipPoolDto);

        IPPool ipPool = IPPool.builder()
                .poolName(ipPoolDto.getPoolName())
                .ipRange(ipPoolDto.getIpRange())
                .router(Routers.builder().routerName(ipPoolDto.getRouter()).build())
                .build();
        poolRepository.save(ipPool);
    }

    public List<IPPoolDto> getIPPools() {
        List<IPPool> ipPoolList = poolRepository.findAll();
        return ipPoolList.stream().map(this::mapToPoolDto).toList();
    }

    private IPPoolDto mapToPoolDto(IPPool ipPool) {
        IPPoolDto poolDto = new IPPoolDto();
        poolDto.setPoolName(ipPool.getPoolName());
        poolDto.setIpRange(ipPool.getIpRange());
        poolDto.setRouter(ipPool.getRouter().getRouterName());

        return poolDto;
    }

    public void deleteIPPool(String name, String router) throws MikrotikApiException {
        mikrotikClient.deleteIPPool(name, router);
        poolRepository.deletePoolByPoolName(name);

    }
}
