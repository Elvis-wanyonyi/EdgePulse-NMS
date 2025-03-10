package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.dto.ServiceType;
import com.wolfcode.MikrotikNetwork.entity.Clients;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ClientsRepository extends JpaRepository<Clients, Long> {


    @Query("SELECT u FROM Clients u WHERE u.expiresOn < :now")
    List<Clients> findOverdueClients(@Param("now") LocalDateTime now);

    Clients findByMpesaRefIgnoreCase(String code);

    Clients findUserByUsername(@NotNull(message = "Enter Voucher") String voucherCode);

    Optional<Clients> findByUsername(String username);

    @Query("SELECT COALESCE(SUM(u.payment), 0) FROM Clients u")
    int sumAllRevenue();

    @Query("SELECT COALESCE(SUM(u.payment), 0) FROM Clients u WHERE u.createdOn BETWEEN :start AND :end")
    int sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(u.payment), 0) FROM Clients u WHERE u.router = :router")
    int sumRevenueByRouter(@Param("router") String router);

    @Query("SELECT EXTRACT(HOUR FROM u.createdOn), COALESCE(SUM(u.payment), 0) FROM Clients u WHERE u.createdOn BETWEEN :start AND :end GROUP BY EXTRACT(HOUR FROM u.createdOn) ORDER BY EXTRACT(HOUR FROM u.createdOn)")
    Map<Integer, Integer> findHourlyRevenueDistribution(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT CAST(u.createdOn AS LocalDate), COALESCE(SUM(u.payment), 0) FROM Clients u WHERE u.createdOn >= :startDate GROUP BY CAST(u.createdOn AS LocalDate) ORDER BY CAST(u.createdOn AS LocalDate) DESC")
    Map<LocalDate, Integer> findDailyRevenueTrend(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT u.plan, COALESCE(SUM(u.payment), 0) FROM Clients u GROUP BY u.plan")
    Map<String, Integer> sumRevenueByPackageType();

    List<Clients> findAllByType(ServiceType serviceType);

    List<Clients> findAllByTypeAndExpiresOnAfter(ServiceType serviceType, LocalDateTime now);

    @Query("SELECT c FROM Clients c WHERE c.username = :username")
    List<Clients> findAllByUsername(@Param("username") String username);

    Clients findByAccount(String billRefNumber);


    @Query("""
            SELECT c FROM Clients c
            WHERE c.createdOn BETWEEN :start AND :end
              AND (:router = 'All routers' OR c.router.routerName = :router)
              AND (:serviceType = 'All Transactions' OR c.type = :serviceType)
              AND (:rechargeMethod = 'All method types' OR c.loginBy = :rechargeMethod)
            """)
    List<Clients> findByPeriodReports(
            @Param("router") String router,
            @Param("serviceType") String serviceType,
            @Param("rechargeMethod") String rechargeMethod,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
                SELECT c FROM Clients c
                WHERE c.createdOn BETWEEN :start AND :end
            """)
    List<Clients> findByCreatedOnBetween(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}
