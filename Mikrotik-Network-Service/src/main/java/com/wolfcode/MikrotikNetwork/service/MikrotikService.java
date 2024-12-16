package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.*;
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


    public void connectUser(String ipAddress, String macAddress, String packageType,
                            String phoneNumber, String mpesaReceiptNumber, String amount, String routerName, String checkoutRequestID) {

        Optional<PackagePlans> packagePlans = packageRepository.findByPackageName(packageType);
        if (packagePlans.isPresent()) {
            PackagePlans packagePlan = packagePlans.get();

            try {
                Clients clients = Clients.builder()
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
                hotspotClientsRepository.save(clients);

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


    public UserCredentials connectUserWithQuery(String ipAddress, String macAddress, String packageType,
                                                String phoneNumber, String amount, String routerName, String checkoutRequestID, String transactionRefNo) {

        Optional<PackagePlans> plans = packageRepository.findByPackageName(packageType);
        if (plans.isPresent()) {
            PackagePlans packagePlan = plans.get();

            try {
                String username = generateUsername(macAddress, phoneNumber);
                String password = generatePassword();

                log.info("User Created: username{} password{} ipAddress{} macAddress{}",
                        username, password, ipAddress, macAddress);

                int intAmount = Integer.parseInt(amount);
                Clients clients = Clients.builder()
                        .mpesaReceiptNumber(transactionRefNo)
                        .phoneNumber(phoneNumber)
                        .router(routerName)
                        .username(macAddress)
                        .ipAddress(ipAddress)
                        .plan(packagePlan.getPackageName())
                        .amount(intAmount)
                        .createdOn(LocalDateTime.now())
                        .build();
                hotspotClientsRepository.save(clients);

                mikroTikClient.createHotspotUser(ipAddress, macAddress,
                        packagePlan.getPackageName(), convertHoursToUptimeLimit(packagePlan.getPlanValidity()),
                        routerName);

                Duration sessionLimit = Duration.ofHours(packagePlan.getPlanValidity());
                LocalDateTime sessionStartTime = LocalDateTime.now();
                LocalDateTime sessionEndTime = sessionStartTime.plus(sessionLimit);
                UserSession userSession = UserSession.builder()
                        .routerName(routerName)
                        .username(username)
                        .sessionStartTime(LocalDateTime.now())
                        .sessionEndTime(sessionEndTime)
                        .build();
                userSessionRepository.save(userSession);

                return UserCredentials.builder()
                        .username(username)
                        .password(password)
                        .build();
            } catch (MikrotikApiException e) {
                throw new IllegalArgumentException("Failed to connect Client to router");
            }
        } else {
            throw new IllegalArgumentException("Package not found");
        }
    }


    public String generateUsername(String macAddress, String phoneNumber) {
        String mac = macAddress.substring(0, 4);
        return phoneNumber + mac;
    }

    public String generatePassword() {
        return UUID.randomUUID().toString().substring(0, 4);
    }


    public void createHotspotVoucher(CreateVouchers voucherRequests) {

        for (int i = 0; i < voucherRequests.getQuantity(); i++) {
            String voucherCode =
                    UUID.randomUUID().toString().substring(voucherRequests.getLength());

            Voucher voucher = Voucher.builder()
                    .voucherCode(voucherCode)
                    .packageType(voucherRequests.getPackageType())
                    .createdAt(LocalDateTime.now())
                    .status(VoucherStatus.ACTIVE)
                    .redeemedBy(null)
                    .ipAddress(null)
                    .routerName(voucherRequests.getRouterName())
                    .build();
            voucherRepository.save(voucher);
        }
    }

    public Map<String, String> loginWithMpesaCode(MpesaCodeRequest mpesaCodeRequest) {

        Clients clients = hotspotClientsRepository.findByMpesaReceiptNumberIgnoreCase(mpesaCodeRequest.getCode());
        if (clients == null) {
            throw new IllegalArgumentException("Code not found contact admin:");
        } else if (!clients.getUsername().equals(mpesaCodeRequest.getMacAddress())) {
            throw new IllegalArgumentException("MAC not found contact admin:");
        }

        String username = clients.getUsername();

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

        Clients user = hotspotClientsRepository.findUserByUsername(redeemVoucher.getVoucherCode());
        if (user != null && user.getUsername().equals(redeemVoucher.getMacAddress())) {
            String code = redeemVoucher.getVoucherCode();
            Map<String, String> response = new HashMap<>();
            response.put("username", code);
            response.put("password", code);

            return response;
        }

        Optional<PackagePlans> plans = packageRepository.findByPackageName(voucher.getPackageType());
        if (plans.isEmpty()) {
            throw new IllegalArgumentException("Package not found");
        }
        PackagePlans packagePlans = plans.get();


        voucher.setRedeemedBy(redeemVoucher.getMacAddress());
        voucher.setIpAddress(redeemVoucher.getIpAddress());
        voucher.setStatus(VoucherStatus.USED);
        voucher.setExpiryDate(LocalDateTime.now().plusHours(packagePlans.getPlanValidity()));
        voucherRepository.save(voucher);

        String profile = packagePlans.getPackageName();
        String uptimeLimit = convertHoursToUptimeLimit(packagePlans.getPlanValidity());

        mikroTikClient.redeemVoucher(redeemVoucher.getVoucherCode(), redeemVoucher.getIpAddress(),
                redeemVoucher.getMacAddress(), profile, uptimeLimit, voucher.getRouterName());

        UserSession userSession = UserSession.builder()
                .routerName(voucher.getRouterName())
                .macAddress(redeemVoucher.getMacAddress())
                .username(voucher.getVoucherCode())
                .sessionStartTime(LocalDateTime.now())
                .sessionEndTime(LocalDateTime.now().plusHours(packagePlans.getPlanValidity()))
                .build();
        userSessionRepository.save(userSession);

        Clients clients = Clients.builder()
                .username(redeemVoucher.getMacAddress())
                .ipAddress(redeemVoucher.getIpAddress())
                .plan(voucher.getPackageType())
                .loginBy(LoginBy.VOUCHER)
                .createdOn(LocalDateTime.now())
                .expiresOn(null)
                .amount(packagePlans.getPrice())
                .build();
        hotspotClientsRepository.save(clients);

        String voucherCode = voucher.getVoucherCode();
        Map<String, String> response = new HashMap<>();
        response.put("username", voucherCode);
        response.put("password", voucherCode);

        return response;
    }

    public Page<ClientResponse> getAllClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Clients> userPage = hotspotClientsRepository.findAll(pageable);

        return userPage.map(this::convertToClientResponse);
    }

    private ClientResponse convertToClientResponse(Clients clients) {
        return ClientResponse.builder()
                .username(clients.getUsername())
                .phoneNumber(clients.getPhoneNumber())
                .mpesaReceiptNumber(clients.getMpesaReceiptNumber())
                .ipAddress(clients.getIpAddress())
                .createdOn(clients.getCreatedOn())
                .expiresOn(clients.getExpiresOn())
                .plan(clients.getPlan())
                .amount(clients.getAmount())
                .loginBy(clients.getLoginBy())
                .router(clients.getRouter())
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

        if (voucher != null) {
            voucher.setPackageType(updateVoucher.getPackageType());
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

    public void addRouter(RouterRequest routerRequest) {
        Routers router = Routers.builder()
                .routerName(routerRequest.getRouterName())
                .routerIPAddress(routerRequest.getRouterIPAddress())
                .username(routerRequest.getUsername())
                .password(routerRequest.getPassword())
                .dnsName(routerRequest.getDnsName())
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
            router.setDnsName(routerRequest.getDnsName());
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


    public void createHotspotPlan(@Valid PackagePlanDto packagePlanDto) {
        Optional<BandwidthLimits> bandwidth = bandwidthRepository.findByName(packagePlanDto.getBandwidthLimit());
        if (bandwidth.isPresent()) {
            BandwidthLimits bandwidthLimits = bandwidth.get();

        PackagePlans plans = PackagePlans.builder()
                .routerName(packagePlanDto.getRouterName())
                .packageName(packagePlanDto.getPackageName())
                .bandwidthLimit(bandwidthLimits.getDownload()+ "/" + bandwidthLimits.getUpload())
                .planValidity(packagePlanDto.getPlanValidity())
                .planDuration(packagePlanDto.getPlanDuration())
                .price(packagePlanDto.getPrice())
                .dataLimit(packagePlanDto.getDataLimit())
                .build();
        packageRepository.save(plans);

    }
}

    public List<PackagePlanDto> getHotspotPlans() {
        List<PackagePlans> packagePlans = packageRepository.findAll();
        return Optional.of(packagePlans)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::mapToPackagePlanDto)
                .collect(Collectors.toList());
    }

    private PackagePlanDto mapToPackagePlanDto(PackagePlans plans) {
        return PackagePlanDto.builder()
                .packageName(plans.getPackageName())
                .bandwidthLimit(plans.getBandwidthLimit())
                .planValidity(plans.getPlanValidity())
                .planDuration(plans.getPlanDuration())
                .dataLimit(plans.getDataLimit())
                .price(plans.getPrice())
                .routerName(plans.getRouterName())
                .build();
    }

    public void editHotspotPlan(Long id, @Valid PackagePlanDto packagePlanDto) {
        Optional<PackagePlans> plans = packageRepository.findById(id);

        if (plans.isPresent()) {
            PackagePlans packagePlans = plans.get();
            packagePlans.setPackageName(packagePlanDto.getPackageName());
            packageRepository.save(packagePlans);

        } else {
            throw new IllegalArgumentException("Package not found : " + id);
        }
    }

    public void deleteHotspotPlan(Long id) {
        packageRepository.deleteById(id);

    }


    public void addBandwidthPlan(BandwidthDto request) {
        BandwidthLimits bandwidthLimits = BandwidthLimits.builder()
                .name(request.getName())
                .upload(request.getUpload())
                .download(request.getDownload())
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
                .upload(limits.getUpload())
                .download(limits.getDownload())
                .build();
    }

    public void editBandwidthPlan(Long id, BandwidthDto request) {
        bandwidthRepository.findById(id).ifPresent(bandwidth -> {
            bandwidth.setUpload(request.getUpload());
            bandwidth.setDownload(request.getDownload());
            bandwidth.setName(request.getName());
            bandwidthRepository.save(bandwidth);
        });
    }

    public void deleteBandwidthPlan(Long id) {
        bandwidthRepository.deleteById(id);
    }
}