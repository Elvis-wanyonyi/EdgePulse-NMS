package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.*;
import com.wolfcode.MikrotikNetwork.dto.hotspot.*;
import com.wolfcode.MikrotikNetwork.dto.network.PlanDto;
import com.wolfcode.MikrotikNetwork.dto.voucher.*;
import com.wolfcode.MikrotikNetwork.entity.*;
import com.wolfcode.MikrotikNetwork.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MikrotikService {

    private final MikrotikClient mikroTikClient;
    private final ClientsRepository clientsRepository;
    private final VoucherRepository voucherRepository;
    private final RouterRepository routerRepository;
    private final UserSessionRepository userSessionRepository;
    private final BandwidthRepository bandwidthRepository;
    public static final String CLIENT_PASSWORD = "12345";
    private final PaymentSessionRepository paymentSessionRepository;
    private final PlansRepository plansRepository;
    private final SimpMessagingTemplate messagingTemplate;


    public void connectUser(String ipAddress, String macAddress, String packageType,
                            String phoneNumber, String mpesaReceiptNumber, String routerName) {

        Plans hotspotPlans = plansRepository.findByPlanName(packageType)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        Routers router = routerRepository.findByRouterName(routerName)
                .orElseThrow(() -> new IllegalArgumentException("Router not found"));

        try {
            Clients hotspotClients = Clients.builder()
                    .account(macAddress)
                    .mpesaRef(mpesaReceiptNumber)
                    .phone(phoneNumber)
                    .router(router)
                    .username(macAddress)
                    .plan(hotspotPlans)
                    .payment(hotspotPlans.getPrice())
                    .createdOn(LocalDateTime.now())
                    .expiresOn(LocalDateTime.now().plusHours(hotspotPlans.getPlanValidity()))
                    .loginBy(LoginBy.SYSTEM)
                    .build();
            clientsRepository.save(hotspotClients);

            String uptimeLimit =
                    convertHoursToUptimeLimit(Math.toIntExact(hotspotPlans.getPlanValidity()));

            mikroTikClient.createHotspotUser(ipAddress, macAddress,
                    hotspotPlans.getPlanName(), uptimeLimit, router);

            Duration sessionLimit = Duration.ofHours(hotspotPlans.getPlanValidity());
            LocalDateTime sessionEndTime = LocalDateTime.now().plus(sessionLimit);
            UserSession userSession = UserSession.builder()
                    .routerName(routerName)
                    .username(macAddress)
                    .type(ServiceType.HOTSPOT)
                    .sessionStartTime(LocalDateTime.now())
                    .sessionEndTime(sessionEndTime)
                    .build();
            userSessionRepository.save(userSession);

        } catch (MikrotikApiException e) {
            throw new IllegalArgumentException("Failed to connect Client to router");
        }
    }

    private String convertHoursToUptimeLimit(Integer planValidity) {
        return String.format("%02d:00:00", planValidity);
    }


    public UserCredentials connectUserWithQuery(String checkoutRequestId) {
        PaymentSession paymentSession = paymentSessionRepository.findByCheckoutRequestID(checkoutRequestId);
        if (paymentSession == null) {
            throw new IllegalArgumentException("Payment session not found");
        }

        paymentSession.setStatus(Status.CONFIRMED);
        paymentSessionRepository.save(paymentSession);

        Routers router = routerRepository.findByRouterName(paymentSession.getRouterName())
                .orElseThrow(() -> new IllegalArgumentException("Router not found"));

        Plans plans = plansRepository.findByPlanName(paymentSession.getPackageType())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));


        try {

            String macAddress = paymentSession.getMac();
            String ipAddress = paymentSession.getIp();
            String routerName = router.getRouterName();

            log.info("User Created: username{} password{} ipAddress{} macAddress{}",
                    macAddress, CLIENT_PASSWORD, ipAddress, macAddress);

            mikroTikClient.createHotspotUser(ipAddress, macAddress,
                    plans.getPlanName(), convertHoursToUptimeLimit(Math.toIntExact(plans.getPlanValidity())),
                    router);

            Duration sessionLimit = Duration.ofHours(plans.getPlanValidity());
            LocalDateTime sessionStartTime = LocalDateTime.now();
            LocalDateTime sessionEndTime = sessionStartTime.plus(sessionLimit);
            UserSession userSession = UserSession.builder()
                    .routerName(routerName)
                    .username(macAddress)
                    .sessionStartTime(LocalDateTime.now())
                    .sessionEndTime(sessionEndTime)
                    .build();
            userSessionRepository.save(userSession);

            return UserCredentials.builder()
                    .username(macAddress)
                    .password(CLIENT_PASSWORD)
                    .build();
        } catch (MikrotikApiException e) {
            throw new IllegalArgumentException("Failed to connect Client to router");
        }
    }

    public void createHotspotVoucher(CreateVouchers voucherRequests) {

        Routers router = routerRepository.findByRouterName(voucherRequests.getRouterName())
                .orElseThrow(() -> new IllegalArgumentException("Router not found"));

        Plans plans = plansRepository.findByPlanName(voucherRequests.getPlan())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        for (int i = 0; i < voucherRequests.getQuantity(); i++) {
            String voucherCode =
                    UUID.randomUUID().toString().substring(voucherRequests.getLength());

            Voucher voucher = Voucher.builder()
                    .voucherCode(voucherCode)
                    .plan(plans)
                    .createdAt(LocalDateTime.now())
                    .status(VoucherStatus.ACTIVE)
                    .redeemedBy(null)
                    .ipAddress(null)
                    .router(router)
                    .build();
            voucherRepository.save(voucher);
        }
    }

    public Map<String, String> loginWithMpesaCode(MpesaCodeRequest mpesaCodeRequest) {

        Clients client = clientsRepository.findByMpesaRefIgnoreCase(mpesaCodeRequest.getCode());
        if (client == null) {
            throw new IllegalArgumentException("Code not found contact admin:");
        } else if (!client.getUsername().equals(mpesaCodeRequest.getMacAddress())) {
            throw new IllegalArgumentException("MAC not found contact admin:");
        }
        String username = client.getUsername();
        Map<String, String> response = new HashMap<>();
        response.put("username", username);
        response.put("password", null);
        return response;
    }

    public Map<String, String> redeemVoucher(@Valid RedeemVoucher redeemVoucher) throws MikrotikApiException {
        Voucher voucher = voucherRepository.findByVoucherCodeIgnoreCase(redeemVoucher.getVoucherCode());
        if (voucher == null || voucher.getStatus() == VoucherStatus.EXPIRED) {
            throw new IllegalArgumentException("Voucher not found or expired, contact admin: ");
        }

        Clients user = clientsRepository.findUserByUsername(redeemVoucher.getVoucherCode());
        if (user != null && user.getUsername().equals(redeemVoucher.getMacAddress())) {
            String code = redeemVoucher.getVoucherCode();
            Map<String, String> response = new HashMap<>();
            response.put("username", code);
            response.put("password", code);

            return response;
        }

        Plans plans = plansRepository.findByPlanName(voucher.getPlan().getPlanName())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        voucher.setRedeemedBy(redeemVoucher.getMacAddress());
        voucher.setIpAddress(redeemVoucher.getIpAddress());
        voucher.setStatus(VoucherStatus.USED);
        voucher.setExpiryDate(LocalDateTime.now().plusHours(plans.getPlanValidity()));
        voucherRepository.save(voucher);

        String profile = plans.getPlanName();
        String uptimeLimit = convertHoursToUptimeLimit(Math.toIntExact(plans.getPlanValidity()));

        mikroTikClient.redeemVoucher(redeemVoucher.getVoucherCode(), redeemVoucher.getIpAddress(),
                redeemVoucher.getMacAddress(), profile, uptimeLimit, voucher.getRouter().getRouterName());

        UserSession userSession = UserSession.builder()
                .routerName(voucher.getRouter().getRouterName())
                .username(voucher.getVoucherCode())
                .sessionStartTime(LocalDateTime.now())
                .sessionEndTime(LocalDateTime.now().plusHours(plans.getPlanValidity()))
                .build();
        userSessionRepository.save(userSession);

        Clients hotspotClients = Clients.builder()
                .account(redeemVoucher.getMacAddress())
                .username(redeemVoucher.getMacAddress())
                .plan(voucher.getPlan())
                .type(ServiceType.HOTSPOT)
                .loginBy(LoginBy.VOUCHER)
                .balance(0)
                .createdOn(LocalDateTime.now())
                .expiresOn(null)
                .payment(plans.getPrice())
                .build();
        clientsRepository.save(hotspotClients);

        String voucherCode = voucher.getVoucherCode();
        Map<String, String> response = new HashMap<>();
        response.put("username", voucherCode);
        response.put("password", voucherCode);

        return response;
    }

    public List<ClientResponse> getAllClients() {
        List<Clients> clients = clientsRepository.findAllByType(ServiceType.HOTSPOT);

        return clients.stream().map(this::convertToClientResponse).collect(Collectors.toList());
    }

    private ClientResponse convertToClientResponse(Clients hotspotClients) {
        return ClientResponse.builder()
                .username(hotspotClients.getUsername())
                .phone(hotspotClients.getPhone())
                .createdOn(hotspotClients.getCreatedOn())
                .expiresOn(hotspotClients.getExpiresOn())
                .plan(hotspotClients.getPlan().getPlanName())
                .payment(hotspotClients.getPlan().getPrice())
                .balance(0)
                .loginBy(hotspotClients.getLoginBy().toString())
                .type(hotspotClients.getType().toString())
                .router(hotspotClients.getRouter().getRouterName())
                .build();
    }

    public int getTotalActiveClients() throws MikrotikApiException {
        List<ClientResponse> activeClients = mikroTikClient.getTotalActiveClients();

        return activeClients.size();
    }

    public List<ActiveUsersResponse> getAllActiveClients() throws MikrotikApiException {
        return mikroTikClient.getAllActiveClients();
    }

    public int getTotalConnectedUsers() throws MikrotikApiException {
        return mikroTikClient.getTotalConnectedUsers();
    }

    public List<ClientResponse> getConnectedUsers() {
        List<Clients> clients = clientsRepository.findAllByTypeAndExpiresOnAfter(ServiceType.HOTSPOT, LocalDateTime.now());

        return clients.stream().map(this::mapToActiveClients).toList();
    }

    private ClientResponse mapToActiveClients(Clients clients) {
        return ClientResponse.builder()
                .username(clients.getUsername())
                .phone(clients.getPhone())
                .mpesaRef(clients.getMpesaRef())
                .plan(clients.getPlan().getPlanName())
                .createdOn(clients.getCreatedOn())
                .expiresOn(clients.getExpiresOn())
                .type(ServiceType.HOTSPOT.toString())
                .loginBy(clients.getLoginBy().toString())
                .status(clients.getStatus().toString())
                .router(clients.getRouter().getRouterName())
                .build();
    }

    public Map<String, String> getRouterHealth(String routerName) throws MikrotikApiException {
        return mikroTikClient.getRouterHealth(routerName);
    }

    @Async
    public void monitorRouterTraffic(String routerName) throws MikrotikApiException {
        Routers router = routerRepository.findByRouterName(routerName)
                .orElseThrow(() -> new IllegalArgumentException("Router not found"));

        mikroTikClient.connectRouter(router.getRouterName());

        while (!Thread.currentThread().isInterrupted()) {
            CompletableFuture<Map<String, Object>> trafficData = mikroTikClient.monitorRouterTraffic(router.getRouterInterface());

            trafficData.thenAccept(data -> messagingTemplate.convertAndSend("/topic/traffic", data));

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public Map<String, String> getRouterSystemAlerts(String routerName) throws MikrotikApiException {
        return mikroTikClient.getRouterSystemAlerts(routerName);
    }

    public Map<String, Object> viewRouterLogs(String routerName) throws MikrotikApiException {
        return mikroTikClient.viewRouterLogs(routerName);
    }

    public void changeRouterSystemSettings(String action, String routerName) throws MikrotikApiException {
        mikroTikClient.changeRouterSystemSettings(action, routerName);
    }


    public void deleteVoucher(String voucherCode) {
        Voucher voucher = voucherRepository.findByVoucherCodeIgnoreCase(voucherCode);
        if (voucher != null) {
            voucherRepository.delete(voucher);
        } else {
            throw new IllegalArgumentException("Voucher not found : " + voucherCode);
        }
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public VoucherResponse editVoucher(String voucherCode, UpdateVoucher updateVoucher) {
        Voucher voucher = voucherRepository.findByVoucherCodeIgnoreCase(voucherCode);
        Plans plans = plansRepository.findByPlanName(updateVoucher.getPackageType())
                .orElseThrow(() -> new IllegalArgumentException("plan does not exist"));

        voucher.setPlan(plans);
        voucher.setStatus(updateVoucher.getStatus());
        voucher.setRedeemedBy(updateVoucher.getRedeemedBy());
        voucherRepository.save(voucher);

        return VoucherResponse.builder()
                .voucherCode(voucherCode)
                .redeemedBy(voucher.getRedeemedBy())
                .packageType(voucher.getRedeemedBy())
                .status(voucher.getStatus())
                .createdAt(voucher.getCreatedAt())
                .build();
    }

    public void deleteHotspotClient(Long id) throws MikrotikApiException {
        UserSession user = userSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hotspot client not found : " + id));

        mikroTikClient.deleteUser(user);
    }

    @Scheduled(cron = "0 */3 * * * *")
    public void removeExpiredUsers() {
        System.out.println("Checking for Expired Users");
        List<UserSession> expiredSessions = userSessionRepository.findExpiredSessions(LocalDateTime.now());

        for (UserSession session : expiredSessions) {
            try {
                if ("hotspot".equalsIgnoreCase(session.getType().toString())) {
                    mikroTikClient.removeExpiredUser(session);
                    System.out.println("Removed expired Hotspot user: " + session.getUsername());
                } else if ("pppoe".equalsIgnoreCase(session.getType().toString())) {
                    mikroTikClient.disconnectOverdueClients(session);
                    System.out.println("Disconnected expired PPPoE user: " + session.getUsername());
                }

                userSessionRepository.delete(session);
            } catch (MikrotikApiException e) {
                System.err.println("Error handling expired user: " + session.getUsername());
            }
        }
    }

    public void createHotspotPlan(@Valid PlanDto planDto) {
        Routers router = routerRepository.findById(planDto.getRouter())
                .orElseThrow(() -> new IllegalArgumentException("Router not found : " + planDto.getRouter()));

        BandwidthLimits bandwidth = bandwidthRepository.findById(planDto.getBandwidthLimit())
                .orElseThrow(() -> new IllegalArgumentException("Bandwidth not found : "));

        Plans plans = Plans.builder()
                .router(router)
                .planName(planDto.getPackageName())
                .bandwidthLimit(bandwidth)
                .planValidity(planDto.getPlanValidity())
                .price(planDto.getPrice())
                .build();
        plansRepository.save(plans);
    }

    public List<PlanDto> getHotspotPlans() {
        List<Plans> hotspotPlans = plansRepository.findAllByServiceType(ServiceType.HOTSPOT);
        return Optional.of(hotspotPlans)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::mapToPackagePlanDto)
                .collect(Collectors.toList());
    }

    private PlanDto mapToPackagePlanDto(Plans plans) {
        return PlanDto.builder()
                .packageName(plans.getPlanName())
                .bandwidthLimit(plans.getBandwidthLimit().getId())
                .planValidity(plans.getPlanValidity())
                .price(plans.getPrice())
                .router(plans.getRouter().getId())
                .build();
    }

    public void editHotspotPlan(Long id, @Valid PlanDto planDto) {
        Plans plans = plansRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found : " + id));

        plans.setPlanName(planDto.getPackageName());
        plans.setPlanName(planDto.getPackageName());
        plans.setPlanValidity(planDto.getPlanValidity());
        plans.setPrice(planDto.getPrice());
        plansRepository.save(plans);
    }

    public void deleteHotspotPlan(Long id) {
        plansRepository.deleteById(id);
    }

    public Map<String, Object> getRouterResources(String routerName) throws MikrotikApiException {
        Routers router = routerRepository.findByRouterName(routerName)
                .orElseThrow(() -> new IllegalArgumentException("Router not found : " + routerName));
        return mikroTikClient.getRouterResources(router.getRouterName());
    }

    public Map<String, Object> getRouterIpAddresses(String routerName) throws MikrotikApiException {
        Routers router = routerRepository.findByRouterName(routerName)
                .orElseThrow(() -> new IllegalArgumentException("Router not found : " + routerName));
        return mikroTikClient.getRouterIpAddresses(router.getRouterName());
    }

    public ClientDetailsResponse getClientByUsername(String username) {
        Clients client = clientsRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Client not found : " + username));

        return ClientDetailsResponse.builder()
                .username(client.getUsername())
                .phone(client.getPhone())
                .createdOn(client.getCreatedOn())
                .expiresOn(client.getExpiresOn())
                .mpesaRef(client.getMpesaRef())
                .plan(client.getPlan().getPlanName())
                .router(client.getRouter().getRouterName())
                .type(client.getType().toString())
                .loginBy(client.getLoginBy().toString())
                .status(client.getStatus().toString())
                .build();
    }

    public List<ClientLogs> getClientLogs(String username) throws MikrotikApiException {
        Clients client = clientsRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Client not found : " + username));

        return mikroTikClient.getHotspotClientLogs(client);
    }

    public List<Invoices> getClientInvoices(String username) {
        List<Clients> clients = clientsRepository.findAllByUsername(username);

        return clients.stream().map(client -> new Invoices(
                generateInvoiceNumber(client),
                client.getPayment(),
                client.getPlan().getPlanName(),
                client.getCreatedOn(),
                client.getExpiresOn(),
                client.getLoginBy()
        )).toList();
    }

    private String generateInvoiceNumber(Clients client) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return String.format("INV-%s", client.getCreatedOn().format(formatter));
    }

}