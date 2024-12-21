package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPOEProfileDto;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoEClientDto;
import com.wolfcode.MikrotikNetwork.entity.*;
import com.wolfcode.MikrotikNetwork.repository.*;
import lombok.RequiredArgsConstructor;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PPPOEService {

    private final MikrotikClient mikrotikClient;
    private final PPPoERepository pppoeProfileRepository;
    private final PPPOEClientsRepo pppoeClientsRepo;
    private final RouterRepository routerRepository;
    private final IPPoolRepository poolRepository;
    private final BandwidthRepository bandwidthRepository;


    public void createPppoeProfile(PPPOEProfileDto profileDto) throws MikrotikApiException {
        Routers router = routerRepository.findById(profileDto.getRouter())
                .orElseThrow(() -> new IllegalArgumentException("Router not found"));

        IPPool pool = poolRepository.findById(profileDto.getIpPool())
                .orElseThrow(() -> new IllegalArgumentException("IPPool not found"));

        BandwidthLimits bandwidth = bandwidthRepository.findById(profileDto.getBandwidthLimit())
                .orElseThrow(() -> new IllegalArgumentException("Bandwidth plan not found"));

        PPPoEPlans plans = pppoeProfileRepository.findByName(profileDto.getName());
        if (plans == null) {

            PPPoEPlans plan = PPPoEPlans.builder()
                    .name(profileDto.getName())
                    .ipPool(pool)
                    .planValidity(profileDto.getPlanValidity())
                    .router(router)
                    .bandwidthLimit(bandwidth)
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
                .plan(clients.getPlan().getId())
                .account(clients.getAccount())
                .phone(clients.getPhone())
                .payment(clients.getPayment())
                .balance(clients.getBalance())
                .router(clients.getRouter().getId())
                .build();
    }

    public void deletePppoeProfile(String name, String router) throws MikrotikApiException {
        mikrotikClient.removePppoeProfile(name, router);

        pppoeProfileRepository.deleteProfileByName(name);

    }

    public void updatePppoeProfile(String name, PPPOEProfileDto profileDto) {
        IPPool pool = poolRepository.findById(profileDto.getIpPool())
                .orElseThrow(() -> new IllegalArgumentException("IPPool not found"));

        BandwidthLimits bandwidth = bandwidthRepository.findById(profileDto.getBandwidthLimit())
                .orElseThrow(() -> new IllegalArgumentException("Bandwidth plan not found"));

        PPPoEPlans plan = pppoeProfileRepository.findByName(name);
        if (plan != null) {
            plan.setName(name);
            plan.setPlanValidity(profileDto.getPlanValidity());
            plan.setIpPool(pool);
            plan.setBandwidthLimit(bandwidth);
        }
    }

    public PPPoEClientDto getClientById(Long id) {
        PPPoEClients client = pppoeClientsRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        PPPoEPlans plan = pppoeProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PPPOEProfile not found"));

        return PPPoEClientDto.builder()
                .name(client.getName())
                .plan(plan.getId())
                .account(client.getAccount())
                .phone(client.getPhone())
                .payment(client.getPayment())
                .balance(client.getBalance())
                .router(client.getRouter().getId())
                .build();
    }

    public void addPppoeClient(PPPoEClientDto request) throws MikrotikApiException {
        Routers router = routerRepository.findById(request.getRouter())
                .orElseThrow(() -> new IllegalArgumentException("Router not found"));
        PPPoEPlans plans = pppoeProfileRepository.findById(request.getPlan())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        PPPoEClients client = PPPoEClients.builder()
                .name(request.getName())
                .plan(plans)
                .account(request.getAccount())
                .phone(request.getPhone())
                .payment(request.getPayment())
                .balance(request.getBalance())
                .router(router)
                .build();
        pppoeClientsRepo.save(client);

        mikrotikClient.createPppoeClient(request, router.getRouterName());
    }
}
