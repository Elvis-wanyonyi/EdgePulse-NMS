package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.network.BandwidthDto;
import com.wolfcode.MikrotikNetwork.dto.network.IPPoolDto;
import com.wolfcode.MikrotikNetwork.dto.network.RouterRequest;
import com.wolfcode.MikrotikNetwork.entity.BandwidthLimits;
import com.wolfcode.MikrotikNetwork.entity.IPPool;
import com.wolfcode.MikrotikNetwork.entity.Routers;
import com.wolfcode.MikrotikNetwork.repository.BandwidthRepository;
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
    private final BandwidthRepository bandwidthRepository;


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
        Routers router = routerRepository.findByRouterName(routerName)
                .orElseThrow(()-> new IllegalArgumentException("Router not found"));
            router.setRouterName(routerRequest.getRouterName());
            router.setRouterIPAddress(routerRequest.getRouterIPAddress());
            router.setUsername(routerRequest.getUsername());
            router.setPassword(routerRequest.getPassword());
            router.setDescription(routerRequest.getDescription());
            routerRepository.save(router);
    }

    public void deleteRouter(String routerName) {
        routerRepository.deleteByRouterName(routerName);
    }

    public List<Routers> getAllRouters() {
        return routerRepository.findAll();
    }


    public void createIPPool(IPPoolDto ipPoolDto) throws MikrotikApiException {
        Routers router = routerRepository.findById(ipPoolDto.getRouter())
                .orElseThrow(() -> new IllegalArgumentException("Router not found : " + ipPoolDto.getRouter()));

        String routerName = router.getRouterName();
        mikrotikClient.createIPPool(ipPoolDto,routerName);

        IPPool ipPool = IPPool.builder()
                .poolName(ipPoolDto.getPoolName())
                .ipRange(ipPoolDto.getIpRange())
                .router(router)
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
        poolDto.setRouter(ipPool.getRouter().getId());

        return poolDto;
    }

    public void deleteIPPool(String name, String router) throws MikrotikApiException {
        mikrotikClient.deleteIPPool(name, router);
        poolRepository.deletePoolByPoolName(name);

    }

    public void addBandwidthPlan(BandwidthDto request) {
        Routers router = routerRepository.findById(request.getRouter())
                .orElseThrow(() -> new IllegalArgumentException("Router not found : "));
        BandwidthLimits bandwidthLimits = BandwidthLimits.builder()
                .name(request.getName())
                .uploadSpeed(request.getUploadSpeed())
                .uploadUnit(request.getUploadUnit())
                .downloadSpeed(request.getDownloadSpeed())
                .downloadUnit(request.getDownloadUnit())
                .router(router)
                .build();
        bandwidthRepository.save(bandwidthLimits);
    }

    public List<BandwidthDto> getBandwidthPlans() {
        List<BandwidthLimits> bandwidthLimits = bandwidthRepository.findAll();
        return bandwidthLimits.stream().map(this::MapToDto).toList();
    }

    private BandwidthDto MapToDto(BandwidthLimits limits) {
        return BandwidthDto.builder()
                .name(limits.getName())
                .uploadSpeed(limits.getUploadSpeed())
                .uploadUnit(limits.getUploadUnit())
                .downloadSpeed(limits.getDownloadSpeed())
                .downloadUnit(limits.getDownloadUnit())
                .router(limits.getRouter().getId())
                .build();
    }

    public void editBandwidthPlan(Long id, BandwidthDto request) {
        bandwidthRepository.findById(id).ifPresent(bandwidth -> {
            bandwidth.setUploadSpeed(request.getUploadSpeed());
            bandwidth.setUploadUnit(request.getUploadUnit());
            bandwidth.setDownloadSpeed(request.getDownloadSpeed());
            bandwidth.setDownloadUnit(request.getDownloadUnit());
            bandwidth.setName(request.getName());
            bandwidthRepository.save(bandwidth);
        });
    }

    public void deleteBandwidthPlan(Long id) {
        bandwidthRepository.deleteById(id);
    }
}
