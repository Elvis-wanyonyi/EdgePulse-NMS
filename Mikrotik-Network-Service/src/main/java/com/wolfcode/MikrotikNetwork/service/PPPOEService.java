package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.ClientStatus;
import com.wolfcode.MikrotikNetwork.dto.LoginBy;
import com.wolfcode.MikrotikNetwork.dto.ServiceType;
import com.wolfcode.MikrotikNetwork.dto.Status;
import com.wolfcode.MikrotikNetwork.dto.payment.MpesaC2BRequest;
import com.wolfcode.MikrotikNetwork.dto.payment.PaymentRequest;
import com.wolfcode.MikrotikNetwork.dto.pppoe.CreatePPPoEClient;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPOEProfileDto;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoEClientsResponse;
import com.wolfcode.MikrotikNetwork.dto.pppoe.PPPoESubscription;
import com.wolfcode.MikrotikNetwork.entity.*;
import com.wolfcode.MikrotikNetwork.repository.*;
import lombok.RequiredArgsConstructor;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PPPOEService {

    private final MikrotikClient mikrotikClient;
    private final PlansRepository plansRepository;
    private final ClientsRepository clientsRepository;
    private final RouterRepository routerRepository;
    private final IPPoolRepository poolRepository;
    private final BandwidthRepository bandwidthRepository;
    private final UserSessionRepository userSessionRepository;
    private final PPPoETransactionRepository pppoeTransactionRepository;


    public void createPppoeProfile(PPPOEProfileDto profileDto) throws MikrotikApiException {
        Routers router = routerRepository.findById(profileDto.getRouter())
                .orElseThrow(() -> new IllegalArgumentException("Router not found"));

        IPPool pool = poolRepository.findById(profileDto.getIpPool())
                .orElseThrow(() -> new IllegalArgumentException("IPPool not found"));

        BandwidthLimits bandwidth = bandwidthRepository.findById(profileDto.getBandwidthLimit())
                .orElseThrow(() -> new IllegalArgumentException("Bandwidth plan not found"));

        Optional<Plans> plans = plansRepository.findByPlanName(profileDto.getName());
        if (plans.isEmpty()) {
            Plans plan = Plans.builder()
                    .planName(profileDto.getName())
                    .ipPool(pool)
                    .planValidity(profileDto.getPlanValidity())
                    .router(router)
                    .bandwidthLimit(bandwidth)
                    .build();
            plansRepository.save(plan);

            mikrotikClient.createPppoeProfile(plan);
        } else {
            throw new IllegalArgumentException("Plan already exists");
        }
    }

    public List<PPPoEClientsResponse> getAllPppoeClients() {
        List<Clients> clientsList = clientsRepository.findAll();
        return clientsList.stream().map(this::mapToResponse).toList();
    }

    private PPPoEClientsResponse mapToResponse(Clients clients) {
        return PPPoEClientsResponse.builder()
                .fullName(clients.getFullName())
                .plan(clients.getPlan().getPlanName())
                .account(clients.getAccount())
                .username(clients.getUsername())
                .password(clients.getPassword())
                .phone(clients.getPhone())
                .email(clients.getEmail())
                .address(clients.getAddress())
                .router(clients.getRouter().getRouterName())
                .loginBy(clients.getLoginBy().toString())
                .status(clients.getStatus().toString())
                .type(clients.getType().toString())
                .createdOn(clients.getCreatedOn())
                .expiresOn(clients.getExpiresOn())
                .build();
    }

    public void deletePppoeProfile(Long id) throws MikrotikApiException {
        Plans pppoePlan = plansRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        mikrotikClient.removePppoeProfile(pppoePlan);

        plansRepository.deleteById(pppoePlan.getId());

    }

    public void updatePppoeProfile(Long id, PPPOEProfileDto profileDto) {
        IPPool pool = poolRepository.findById(profileDto.getIpPool())
                .orElseThrow(() -> new IllegalArgumentException("IPPool not found"));

        BandwidthLimits bandwidth = bandwidthRepository.findById(profileDto.getBandwidthLimit())
                .orElseThrow(() -> new IllegalArgumentException("Bandwidth plan not found"));

        Plans plan = plansRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        plan.setPlanName(profileDto.getName());
        plan.setPlanValidity(profileDto.getPlanValidity());
        plan.setIpPool(pool);
        plan.setBandwidthLimit(bandwidth);

    }

    public void addPppoeClient(CreatePPPoEClient request) throws MikrotikApiException {
        Routers router = routerRepository.findById(request.getRouter().getId())
                .orElseThrow(() -> new IllegalArgumentException("Router not found"));
        Plans plans = plansRepository.findById(request.getPlan().getId())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        Clients client = Clients.builder()
                .fullName(request.getFullName())
                .plan(plans)
                .account(request.getAccount())
                .username(request.getUsername())
                .password(request.getPassword())
                .type(ServiceType.PPPOE)
                .payment(plans.getPrice())
                .balance(0)
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .router(router)
                .address(request.getAddress())
                .loginBy(LoginBy.ADMIN)
                .activePeriod(request.getActivePeriod())
                .createdOn(LocalDateTime.now())
                .expiresOn(LocalDateTime.now().plusDays(plans.getPlanValidity() + 1))
                .build();
        clientsRepository.save(client);

        mikrotikClient.createPppoeClient(client);

        UserSession userSession = UserSession.builder()
                .username(client.getUsername())
                .routerName(router.getRouterName())
                .type(ServiceType.PPPOE)
                .sessionStartTime(LocalDateTime.now())
                .sessionEndTime(client.getExpiresOn())
                .build();
        userSessionRepository.save(userSession);

    }

    public void editClientAccount(Long id, CreatePPPoEClient clientDto) throws MikrotikApiException {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        Plans plans = plansRepository.findById(clientDto.getPlan().getId())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        client.setAccount(clientDto.getAccount());
        client.setPhone(clientDto.getPhone());
        client.setEmail(clientDto.getEmail());
        client.setAddress(clientDto.getAddress());
        client.setPlan(plans);
        client.setUsername(clientDto.getUsername());
        client.setPassword(clientDto.getPassword());
        client.setActivePeriod(clientDto.getActivePeriod());
        client.setExpiresOn(clientDto.getExpiresOn());

        mikrotikClient.editPppoeClient(client);
        clientsRepository.save(client);

    }

    public void deactivateClient(Long id) throws MikrotikApiException {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        mikrotikClient.deactivateClient(client);
    }

    public void removeClient(Long id) throws MikrotikApiException {
        var client = clientsRepository.findById(id);
        mikrotikClient.deleteClient(client);

        clientsRepository.deleteById(id);
    }

    public void rechargeClientAccount(Long id) throws MikrotikApiException {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        client.setPayment(client.getPlan().getPrice());
        client.setBalance(0);
        clientsRepository.save(client);

        mikrotikClient.reWritePppoeAccount(client);
    }

    public void reWriteAccount(Long id) throws MikrotikApiException {
        Clients clients = clientsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        clients.setCreatedOn(LocalDateTime.now());
        clients.setExpiresOn(LocalDateTime.now().plusDays(clients.getPlan().getPlanValidity() + 1));
        mikrotikClient.reWritePppoeAccount(clients);
    }

    public void clientSubscription(Long clientId, PPPoESubscription subscription) {
        Clients client = clientsRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        //handle payments online via stk push, cover installments
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPhoneNumber(subscription.getPhoneNumber());
        paymentRequest.setAmount(subscription.getAmount());

        //darajaService.performStkPushTransaction(paymentRequest);
    }

    public PPPoEClientsResponse getPppoeClientById(Long id) {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        return PPPoEClientsResponse.builder()
                .fullName(client.getFullName())
                .plan(client.getPlan().getPlanName())
                .account(client.getAccount())
                .username(client.getUsername())
                .password(client.getPassword())
                .phone(client.getPhone())
                .email(client.getEmail())
                .address(client.getAddress())
                .router(client.getRouter().getRouterName())
                .loginBy(client.getLoginBy().toString())
                .status(client.getStatus().toString())
                .type(client.getType().toString())
                .createdOn(client.getCreatedOn())
                .expiresOn(client.getExpiresOn())
                .build();
    }

    public List<PPPoEClientsResponse> getActivePppoeClients() {
        return mikrotikClient.getAllActivePppoeClients()
                .stream()
                .map(clientData -> {
                    String username = clientData.get("name");
                    String routerName = clientData.get("router");

                    Clients client = clientsRepository.findByUsername(username).orElse(null);
                    PPPoEClientsResponse response = new PPPoEClientsResponse();
                    response.setUsername(username);
                    response.setRouter(routerName);

                    if (client != null) {
                        response.setFullName(client.getFullName());
                        response.setAccount(client.getAccount());
                        response.setUsername(client.getUsername());
                        response.setPassword(client.getPassword());
                        response.setPhone(client.getPhone());
                        response.setType(client.getType().toString());
                        response.setCreatedOn(client.getCreatedOn());
                        response.setExpiresOn(client.getExpiresOn());
                        response.setLoginBy(client.getLoginBy().toString());
                        response.setPlan(client.getPlan().getPlanName());
                        response.setStatus(ClientStatus.ACTIVE.toString());
                        response.setRouter(routerName);
                    }
                    return response;
                })
                .toList();
    }

    public int getTotalActivePppoeClients() {
        return mikrotikClient.getTotalActivePppoeClients();
    }

    public void updatePppoeClientPayment(MpesaC2BRequest request) throws MikrotikApiException {

        Clients client = clientsRepository.findByAccount(request.getBillRefNumber());
        if (client != null) {
            PPPoETransaction transaction = PPPoETransaction.builder()
                    .mpesaRef(request.getCommandID())
                    .account(client.getUsername())
                    .amount(request.getAmount())
                    .phoneNumber(request.getMsisdn())
                    .date(LocalDateTime.now())
                    .router(client.getRouter().getRouterName())
                    .status(Status.CONFIRMED)
                    .build();
            pppoeTransactionRepository.save(transaction);

            client.setPayment(Integer.valueOf(request.getAmount()));
            client.setMpesaRef(request.getBillRefNumber());
            client.setExpiresOn(LocalDateTime.now().plusDays(client.getPlan().getPlanValidity() + 1));
            clientsRepository.save(client);

            mikrotikClient.reWritePppoeAccount(client);
        } else {
            throw new IllegalArgumentException("Client not found");
        }
    }
}
