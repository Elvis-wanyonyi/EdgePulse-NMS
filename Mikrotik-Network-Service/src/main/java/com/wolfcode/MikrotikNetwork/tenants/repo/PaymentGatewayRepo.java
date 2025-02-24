package com.wolfcode.MikrotikNetwork.tenants.repo;

import com.wolfcode.MikrotikNetwork.tenants.entity.DarajaPaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentGatewayRepo extends JpaRepository<DarajaPaymentDetails, Long> {
    Optional<DarajaPaymentDetails> findByTenant(String tenant);
}
