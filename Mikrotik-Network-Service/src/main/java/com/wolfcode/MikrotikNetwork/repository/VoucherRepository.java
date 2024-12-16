package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.Voucher;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Voucher findByVoucherCodeIgnoreCase(String voucherCode);


    @Query("SELECT u FROM Voucher u WHERE u.expiryDate < :now")
    List<Voucher> findExpiredVouchers(@Param("now") LocalDateTime now);
}
