package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.PeriodReportsRequest;
import com.wolfcode.MikrotikNetwork.dto.PeriodReportsResponse;
import com.wolfcode.MikrotikNetwork.entity.Clients;
import com.wolfcode.MikrotikNetwork.repository.ClientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final ClientsRepository clientsRepository;


    public int calculateTodayRevenue() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        return clientsRepository.sumRevenueBetween(todayStart, todayEnd);
    }

    public int calculateThisMonthRevenue() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);
        return clientsRepository.sumRevenueBetween(monthStart, monthEnd);
    }

    public int calculateRevenueByRouter(String router) {
        return clientsRepository.sumRevenueByRouter(router);
    }

    public Map<String, Integer> calculateRevenueByPackage() {
        return clientsRepository.sumRevenueByPackageType();
    }


    public List<PeriodReportsResponse> getPeriodReports(PeriodReportsRequest request) {
        LocalDateTime start = request.getStartDate().atStartOfDay();
        LocalDateTime end = request.getEndDate().atTime(LocalTime.MAX);

        List<Clients> clients = clientsRepository.findByPeriodReports(
                request.getRouter(),
                request.getServiceType(),
                request.getRechargeMethod(),
                start,
                end
        );

        return clients.stream().map(this::mapToPeriodResponse).toList();
    }

    private PeriodReportsResponse mapToPeriodResponse(Clients client) {
        return PeriodReportsResponse.builder()
                .username(client.getUsername())
                .serviceType(client.getType().name())
                .createdOn(client.getCreatedOn())
                .expiresOn(client.getExpiresOn())
                .loginBy(client.getLoginBy().name())
                .planName(client.getPlan().getPlanName())
                .planPrice(String.valueOf(client.getPlan().getPrice()))
                .router(client.getRouter().getRouterName())
                .build();
    }

    public List<PeriodReportsResponse> getDailyReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<Clients> clients = clientsRepository.findByCreatedOnBetween(startOfDay, endOfDay);
        return clients.stream().map(this::mapToPeriodResponse).toList();
    }
}
