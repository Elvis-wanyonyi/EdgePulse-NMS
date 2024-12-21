package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.ClientResponse;
import com.wolfcode.MikrotikNetwork.dto.LoginBy;
import com.wolfcode.MikrotikNetwork.dto.Status;
import com.wolfcode.MikrotikNetwork.dto.UserCredentials;
import com.wolfcode.MikrotikNetwork.dto.hotspot.ActiveUsersResponse;
import com.wolfcode.MikrotikNetwork.dto.hotspot.MpesaCodeRequest;
import com.wolfcode.MikrotikNetwork.dto.hotspot.RouterClientResponse;
import com.wolfcode.MikrotikNetwork.dto.network.HotspotPlanDto;
import com.wolfcode.MikrotikNetwork.dto.voucher.*;
import com.wolfcode.MikrotikNetwork.entity.*;
import com.wolfcode.MikrotikNetwork.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MikrotikService {

    private final MikrotikClient mikroTikClient;
    private final HotspotClientsRepository hotspotClientsRepository;
    private final VoucherRepository voucherRepository;
    private final RouterRepository routerRepository;
    private final UserSessionRepository userSessionRepository;
    private final PackageRepository packageRepository;
    private final BandwidthRepository bandwidthRepository;
    public static final String CLIENT_PASSWORD = "12345";
    private final PaymentSessionRepository paymentSessionRepository;


    public void connectUser(String ipAddress, String macAddress, String packageType,
                            String phoneNumber, String mpesaReceiptNumber, String routerName) {

        Optional<HotspotPlans> packagePlans = packageRepository.findByPackageName(packageType);
        if (packagePlans.isPresent()) {
            HotspotPlans packagePlan = packagePlans.get();

            try {
                HotspotClients hotspotClients = HotspotClients.builder()
                        .mpesaReceiptNumber(mpesaReceiptNumber)
                        .phoneNumber(phoneNumber)
                        .router(routerName)
                        .username(macAddress)
                        .ipAddress(ipAddress)
                        .plan(packagePlan.getPackageName())
                        .amount(packagePlan.getPrice())
                        .createdOn(LocalDateTime.now())
                        .expiresOn(LocalDateTime.now().plusHours(packagePlan.getPlanValidity()))
                        .loginBy(LoginBy.PAYMENT)
                        .build();
                hotspotClientsRepository.save(hotspotClients);

                String uptimeLimit = convertHoursToUptimeLimit(packagePlan.getPlanValidity());
                mikroTikClient.createHotspotUser(ipAddress, macAddress,
                        packagePlan.getPackageName(), uptimeLimit, routerName);

                Duration sessionLimit = Duration.ofHours(packagePlan.getPlanValidity());
                LocalDateTime sessionStartTime = LocalDateTime.now();
                LocalDateTime sessionEndTime = sessionStartTime.plus(sessionLimit);
                UserSession userSession = UserSession.builder()
                        .routerName(routerName)
                        .username(macAddress)
                        .sessionStartTime(LocalDateTime.now())
                        .sessionEndTime(sessionEndTime)
                        .build();
                userSessionRepository.save(userSession);

            } catch (MikrotikApiException e) {
                throw new IllegalArgumentException("Failed to connect Client to router");
            }
        } else {
            throw new IllegalArgumentException("Package not found");
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

        Routers router = routerRepository.findByRouterName(paymentSession.getRouterName());
        if (router == null) {
            throw new IllegalArgumentException("Router not found");
        }

        Optional<HotspotPlans> plans = packageRepository.findByPackageName(paymentSession.getPackageType());
        if (plans.isPresent()) {
            HotspotPlans packagePlan = plans.get();

            try {

                String macAddress = paymentSession.getMac();
                String ipAddress = paymentSession.getIp();
                String routerName = router.getRouterName();

                log.info("User Created: username{} password{} ipAddress{} macAddress{}",
                        macAddress, CLIENT_PASSWORD, ipAddress, macAddress);


                mikroTikClient.createHotspotUser(ipAddress, macAddress,
                        packagePlan.getPackageName(), convertHoursToUptimeLimit(packagePlan.getPlanValidity()),
                        routerName);

                Duration sessionLimit = Duration.ofHours(packagePlan.getPlanValidity());
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
        } else {
            throw new IllegalArgumentException("Package not found");
        }
    }




    public void createHotspotVoucher(CreateVouchers voucherRequests) {

        Routers router = routerRepository.findByRouterName(voucherRequests.getRouterName());
        if (router == null) {
            throw new IllegalArgumentException("Router not found");
        }

        Optional<HotspotPlans> plansOptional = packageRepository.findByPackageName(voucherRequests.getPlan());
        if (plansOptional.isPresent()) {
            HotspotPlans plan = plansOptional.get();

            for (int i = 0; i < voucherRequests.getQuantity(); i++) {
                String voucherCode =
                        UUID.randomUUID().toString().substring(voucherRequests.getLength());

                Voucher voucher = Voucher.builder()
                        .voucherCode(voucherCode)
                        .plan(plan)
                        .createdAt(LocalDateTime.now())
                        .status(VoucherStatus.ACTIVE)
                        .redeemedBy(null)
                        .ipAddress(null)
                        .router(router)
                        .build();
                voucherRepository.save(voucher);
            }
        } else {
            throw new IllegalArgumentException("Package not found");
        }
    }

    public Map<String, String> loginWithMpesaCode(MpesaCodeRequest mpesaCodeRequest) {

        HotspotClients hotspotClients = hotspotClientsRepository.findByMpesaReceiptNumberIgnoreCase(mpesaCodeRequest.getCode());
        if (hotspotClients == null) {
            throw new IllegalArgumentException("Code not found contact admin:");
        } else if (!hotspotClients.getUsername().equals(mpesaCodeRequest.getMacAddress())) {
            throw new IllegalArgumentException("MAC not found contact admin:");
        }

        String username = hotspotClients.getUsername();

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

        HotspotClients user = hotspotClientsRepository.findUserByUsername(redeemVoucher.getVoucherCode());
        if (user != null && user.getUsername().equals(redeemVoucher.getMacAddress())) {
            String code = redeemVoucher.getVoucherCode();
            Map<String, String> response = new HashMap<>();
            response.put("username", code);
            response.put("password", code);

            return response;
        }

        Optional<HotspotPlans> plans = packageRepository.findByPackageName(voucher.getPlan().getPackageName());
        if (plans.isEmpty()) {
            throw new IllegalArgumentException("Package not found");
        }
        HotspotPlans hotspotPlans = plans.get();


        voucher.setRedeemedBy(redeemVoucher.getMacAddress());
        voucher.setIpAddress(redeemVoucher.getIpAddress());
        voucher.setStatus(VoucherStatus.USED);
        voucher.setExpiryDate(LocalDateTime.now().plusHours(hotspotPlans.getPlanValidity()));
        voucherRepository.save(voucher);

        String profile = hotspotPlans.getPackageName();
        String uptimeLimit = convertHoursToUptimeLimit(hotspotPlans.getPlanValidity());

        mikroTikClient.redeemVoucher(redeemVoucher.getVoucherCode(), redeemVoucher.getIpAddress(),
                redeemVoucher.getMacAddress(), profile, uptimeLimit, voucher.getRouter().getRouterName());

        UserSession userSession = UserSession.builder()
                .routerName(voucher.getRouter().getRouterName())
                .macAddress(redeemVoucher.getMacAddress())
                .username(voucher.getVoucherCode())
                .sessionStartTime(LocalDateTime.now())
                .sessionEndTime(LocalDateTime.now().plusHours(hotspotPlans.getPlanValidity()))
                .build();
        userSessionRepository.save(userSession);

        HotspotClients hotspotClients = HotspotClients.builder()
                .username(redeemVoucher.getMacAddress())
                .ipAddress(redeemVoucher.getIpAddress())
                .plan(voucher.getPlan().getPackageName())
                .loginBy(LoginBy.VOUCHER)
                .createdOn(LocalDateTime.now())
                .expiresOn(null)
                .amount(hotspotPlans.getPrice())
                .build();
        hotspotClientsRepository.save(hotspotClients);

        String voucherCode = voucher.getVoucherCode();
        Map<String, String> response = new HashMap<>();
        response.put("username", voucherCode);
        response.put("password", voucherCode);

        return response;
    }

    public Page<ClientResponse> getAllClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<HotspotClients> userPage = hotspotClientsRepository.findAll(pageable);

        return userPage.map(this::convertToClientResponse);
    }

    private ClientResponse convertToClientResponse(HotspotClients hotspotClients) {
        return ClientResponse.builder()
                .username(hotspotClients.getUsername())
                .phoneNumber(hotspotClients.getPhoneNumber())
                .mpesaReceiptNumber(hotspotClients.getMpesaReceiptNumber())
                .ipAddress(hotspotClients.getIpAddress())
                .createdOn(hotspotClients.getCreatedOn())
                .expiresOn(hotspotClients.getExpiresOn())
                .plan(hotspotClients.getPlan())
                .amount(hotspotClients.getAmount())
                .loginBy(hotspotClients.getLoginBy())
                .router(hotspotClients.getRouter())
                .build();
    }

    public int getTotalActiveClients(String routerName) throws MikrotikApiException {
        List<ClientResponse> activeClients = mikroTikClient.getTotalActiveClients(routerName);

        return activeClients.size();
    }

    public List<ActiveUsersResponse> getAllActiveClients(String routerName) throws MikrotikApiException {
        return mikroTikClient.getAllActiveClients(routerName);
    }

    public int getTotalConnectedUsers(String routerName) throws MikrotikApiException {
        List<ClientResponse> connectedUsers = mikroTikClient.getTotalConnectedUsers(routerName);
        return connectedUsers.size();
    }

    public List<RouterClientResponse> getConnectedUsers(String routerName) throws MikrotikApiException {
        return mikroTikClient.getConnectedUsers(routerName);
    }

    public Map<String, String> getRouterHealth(String routerName) throws MikrotikApiException {
        return mikroTikClient.getRouterHealth(routerName);
    }

    public CompletableFuture<Map<String, Object>> getRouterTraffic(String routerInterface, String routerName) throws MikrotikApiException {
        return mikroTikClient.getRouterTraffic(routerInterface, routerName);
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
        Optional<HotspotPlans> plans = packageRepository.findByPackageName(updateVoucher.getPackageType());
        if (plans.isEmpty()) {
            throw new IllegalArgumentException("Package not found : " + updateVoucher.getPackageType());
        }
        HotspotPlans plan = plans.get();

        if (voucher != null) {
            voucher.setPlan(plan);
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
        } else {
            throw new IllegalArgumentException("Voucher not found : " + voucherCode);
        }

    }


    public void deleteUser(String routerName, String username) throws MikrotikApiException {
        userSessionRepository.deleteByUsername(username);

        mikroTikClient.deleteUser(routerName, username);
    }

    @Scheduled(fixedRate = 60 * 2000)
    public void removeExpiredUsers() {
        System.out.println("Checking for Expired Users");
        List<UserSession> expiredSessions = userSessionRepository.findExpiredSessions(LocalDateTime.now());

        for (UserSession session : expiredSessions) {
            try {
                mikroTikClient.removeExpiredUser(session.getRouterName(), session.getUsername());

                System.out.println("removed expired user: " + session.getUsername());
                userSessionRepository.delete(session);
            } catch (MikrotikApiException e) {
                System.err.println("Error removing expired user: " + session.getUsername());
            }
        }
    }


    public void createHotspotPlan(@Valid HotspotPlanDto hotspotPlanDto) {
        Routers router = routerRepository.findById(hotspotPlanDto.getRouter())
                .orElseThrow(()-> new IllegalArgumentException("Router not found : " + hotspotPlanDto.getRouter()));

        BandwidthLimits bandwidth = bandwidthRepository.findById(hotspotPlanDto.getBandwidthLimit())
                .orElseThrow(()-> new IllegalArgumentException("Bandwidth not found : "));

            HotspotPlans plans = HotspotPlans.builder()
                    .router(router)
                    .packageName(hotspotPlanDto.getPackageName())
                    .bandwidthLimit(bandwidth)
                    .planValidity(hotspotPlanDto.getPlanValidity())
                    .planDuration(hotspotPlanDto.getPlanDuration())
                    .price(hotspotPlanDto.getPrice())
                    .dataLimit(hotspotPlanDto.getDataLimit())
                    .build();
            packageRepository.save(plans);

    }

    public List<HotspotPlanDto> getHotspotPlans() {
        List<HotspotPlans> hotspotPlans = packageRepository.findAll();
        return Optional.of(hotspotPlans)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::mapToPackagePlanDto)
                .collect(Collectors.toList());
    }

    private HotspotPlanDto mapToPackagePlanDto(HotspotPlans plans) {
        return HotspotPlanDto.builder()
                .packageName(plans.getPackageName())
                .bandwidthLimit(plans.getBandwidthLimit().getId())
                .planValidity(plans.getPlanValidity())
                .planDuration(plans.getPlanDuration())
                .dataLimit(plans.getDataLimit())
                .price(plans.getPrice())
                .router(plans.getRouter().getId())
                .build();
    }

    public void editHotspotPlan(Long id, @Valid HotspotPlanDto hotspotPlanDto) {
        Optional<HotspotPlans> plans = packageRepository.findById(id);

        if (plans.isPresent()) {
            HotspotPlans hotspotPlans = plans.get();
            hotspotPlans.setPackageName(hotspotPlanDto.getPackageName());
            packageRepository.save(hotspotPlans);

        } else {
            throw new IllegalArgumentException("Package not found : " + id);
        }
    }

    public void deleteHotspotPlan(Long id) {
        packageRepository.deleteById(id);

    }

}