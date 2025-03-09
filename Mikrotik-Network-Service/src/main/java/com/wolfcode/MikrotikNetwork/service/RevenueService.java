package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.dto.RevenueReportRequest;
import com.wolfcode.MikrotikNetwork.repository.ClientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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


    public int calculateRevenueReport(RevenueReportRequest request) {
        return 0;
    }
}
