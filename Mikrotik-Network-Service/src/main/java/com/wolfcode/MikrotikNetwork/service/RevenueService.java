package com.wolfcode.MikrotikNetwork.service;

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


    public int calculateAllRevenue() {
        return clientsRepository.sumAllRevenue();
    }

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

    public int calculateCustomRevenue(LocalDateTime start, LocalDateTime end) {
        return clientsRepository.sumRevenueBetween(start, end);
    }

    public int calculateRevenueByRouter(String router) {
        return clientsRepository.sumRevenueByRouter(router);
    }

    public Map<String, Integer> calculateRevenueByPackage() {
        return clientsRepository.sumRevenueByPackageType();
    }

    public Map<Integer, Integer> getHourlyRevenueDistribution(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return clientsRepository.findHourlyRevenueDistribution(start, end);
    }

}
