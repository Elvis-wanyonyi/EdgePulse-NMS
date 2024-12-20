package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.PaymentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentSessionRepository extends JpaRepository<PaymentSession, Long> {

    PaymentSession findByCheckoutRequestID(String checkoutRequestID);
}
