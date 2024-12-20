package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPOEProfileDto;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoEClientDto;
import com.wolfcode.MikrotikNetwork.entity.*;
import com.wolfcode.MikrotikNetwork.repository.*;
import lombok.RequiredArgsConstructor;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PPPOEService {

    private final MikrotikClient mikrotikClient;
    private final PPPoERepository pppoeProfileRepository;
    private final PPPOEClientsRepo pppoeClientsRepo;
    private final RouterRepository routerRepository;
    private final IPPoolRepository poolRepository;
    private final BandwidthRepository bandwidthRepository;


    public List<PPPoEClientDto> getPppoeClients() {
        return null;
    }

    public void createPppoeProfile(PPPOEProfileDto profileDto) throws MikrotikApiException {
        Routers router  = routerRepository.findByRouterName(profileDto.getRouter());
        if (router == null) {
            throw new IllegalArgumentException("Router not found");
        }
        IPPool pool = poolRepository.findByPoolName(profileDto.getIpPool());
        if (pool == null) {
            throw new IllegalArgumentException("IP pool not found");
        }
        Optional<BandwidthLimits> bandwidth = bandwidthRepository.findByName(profileDto.getBandwidthLimit());
        if (bandwidth.isEmpty()){
            throw new IllegalArgumentException("Bandwidth limit not found");
        }
        BandwidthLimits bandwidthLimit = bandwidth.get();


        PPPoEPlans plans = pppoeProfileRepository.findByName(profileDto.getName());
        if (plans == null) {

            PPPoEPlans plan = PPPoEPlans.builder()
                    .name(profileDto.getName())
                    .ipPool(pool)
                    .planValidity(profileDto.getPlanValidity())
                    .router(router)
                    .bandwidthLimit(bandwidthLimit)
                    .build();
            pppoeProfileRepository.save(plan);

            mikrotikClient.createPppoeProfile(plan);
        }
    }

    public List<PPPoEClientDto> getAllPppoeClients() {
        List<PPPoEClients> clientsList = pppoeClientsRepo.findAll();
        return clientsList.stream().map(this::mapToResponse).toList();
    }

    private PPPoEClientDto mapToResponse(PPPoEClients clients) {
        return PPPoEClientDto.builder()
                .name(clients.getName())
                .plan(clients.getPlan())
                .account(clients.getAccount())
                .phone(clients.getPhone())
                .payment(clients.getPayment())
                .balance(clients.getBalance())
                .status(clients.getStatus())
                .router(clients.getRouter().getRouterName())
                .build();
    }

    public void deletePppoeProfile(String name, String router) throws MikrotikApiException {
        mikrotikClient.removePppoeProfile(name,router);

        pppoeProfileRepository.deleteProfileByName(name);

    }

    public void updatePppoeProfile(String name, PPPOEProfileDto profileDto) {
        PPPoEPlans plan = pppoeProfileRepository.findByName(name);
        if (plan != null) {
            plan.setName(name);
            plan.setPlanValidity(profileDto.getPlanValidity());
            plan.setRouter(Routers.builder().routerName(profileDto.getRouter()).build());
            plan.setIpPool(IPPool.builder().poolName(profileDto.getIpPool()).build());
            plan.setBandwidthLimit(BandwidthLimits.builder().name(profileDto.getBandwidthLimit()).build());
        }
    }

    public PPPoEClientDto getClientById(Long id) {
        PPPoEClients client = pppoeClientsRepo.findById(id).orElse(null);
        assert client != null;
        return PPPoEClientDto.builder()
                .name(client.getName())
                .plan(client.getPlan())
                .account(client.getAccount())
                .phone(client.getPhone())
                .payment(client.getPayment())
                .balance(client.getBalance())
                .status(client.getStatus())
                .router(client.getRouter().getRouterName())
                .build();
    }

    public void addPppoeClient(PPPoEClientDto request) throws MikrotikApiException {
        mikrotikClient.createPppoeClient(request);
        PPPoEClients client = PPPoEClients.builder()
                .name(request.getName())
                .plan(request.getPlan())
                .account(request.getAccount())
                .phone(request.getPhone())
                .payment(request.getPayment())
                .balance(request.getBalance())
                .status(request.getStatus())
                .router(Routers.builder().routerName(request.getRouter()).build())
                .build();
        pppoeClientsRepo.save(client);
    }
}
