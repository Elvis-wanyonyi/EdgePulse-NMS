package com.wolfcode.MikrotikNetwork.tenants.service;

import com.wolfcode.MikrotikNetwork.dto.payment.MpesaPaymentGateway;
import com.wolfcode.MikrotikNetwork.tenants.dto.ShortCodeType;
import com.wolfcode.MikrotikNetwork.tenants.dto.TenantsResponse;
import com.wolfcode.MikrotikNetwork.tenants.entity.DarajaPaymentDetails;
import com.wolfcode.MikrotikNetwork.tenants.entity.Users;
import com.wolfcode.MikrotikNetwork.tenants.repo.PaymentGatewayRepo;
import com.wolfcode.MikrotikNetwork.tenants.repo.UsersRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final UsersRepository usersRepository;
    private final PaymentGatewayRepo paymentGatewayRepo;



    public List<TenantsResponse> getAllTenants() {
        List<Users> users = usersRepository.findAll();
        return users.stream().map(this::MapToResponse).toList();
    }

    private TenantsResponse MapToResponse(Users user) {
        return TenantsResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public void removeTenant(String name) {
        usersRepository.deleteByName(name);
    }

    public void addMpesaPaymentGatewayDetails(@Valid MpesaPaymentGateway mpesaGateway, String tenant) {
        Users user = usersRepository.findByName(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        DarajaPaymentDetails paymentDetails;
        if (mpesaGateway.getShortCodeType().equals(ShortCodeType.VERIFIED)) {
            paymentDetails = DarajaPaymentDetails.builder()
                    .tenant(user.getName())
                    .consumerKey(mpesaGateway.getConsumerKey())
                    .stkPassKey(mpesaGateway.getStkPassKey())
                    .consumerSecret(mpesaGateway.getConsumerSecret())
                    .stkPassKey(mpesaGateway.getStkPassKey())
                    .shortCodeType(ShortCodeType.VERIFIED)
                    .build();
        } else {
            paymentDetails = DarajaPaymentDetails.builder()
                    .tenant(user.getName())
                    .shortCodeType(mpesaGateway.getShortCodeType())
                    .shortCodeType(ShortCodeType.UNVERIFIED)
                    .build();
        }
        paymentGatewayRepo.save(paymentDetails);
    }

    public void editMpesaPaymentGatewayDetails(@Valid MpesaPaymentGateway mpesaGateway, Long id) {
        
        DarajaPaymentDetails paymentDetails = paymentGatewayRepo.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Could not find payment gateway with id " + id));
        paymentDetails.setConsumerKey(mpesaGateway.getConsumerKey());
        paymentDetails.setStkPassKey(mpesaGateway.getStkPassKey());
        paymentDetails.setConsumerSecret(mpesaGateway.getConsumerSecret());
        paymentDetails.setStkPassKey(mpesaGateway.getStkPassKey());
        paymentGatewayRepo.save(paymentDetails);
    }

    public void deleteMpesaPaymentGatewayDetails(Long id) {
        paymentGatewayRepo.deleteById(id);
    }
}
