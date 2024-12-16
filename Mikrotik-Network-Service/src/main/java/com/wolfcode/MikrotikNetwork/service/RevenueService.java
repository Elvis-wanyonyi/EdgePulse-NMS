package com.wolfcode.MikrotikNetwork.service;

import com.wolfcode.MikrotikNetwork.repository.HotspotClientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final HotspotClientsRepository hotspotClientsRepository;


    public int calculateAllRevenue() {
        return hotspotClientsRepository.sumAllRevenue();
    }

    public int calculateTodayRevenue() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        return hotspotClientsRepository.sumRevenueBetween(todayStart, todayEnd);
    }

    public int calculateThisMonthRevenue() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);
        return hotspotClientsRepository.sumRevenueBetween(monthStart, monthEnd);
    }

    public int calculateCustomRevenue(LocalDateTime start, LocalDateTime end) {
        return hotspotClientsRepository.sumRevenueBetween(start, end);
    }

    public int calculateRevenueByRouter(String router) {
        return hotspotClientsRepository.sumRevenueByRouter(router);
    }

    public Map<String, Integer> calculateRevenueByPackage() {
        return hotspotClientsRepository.sumRevenueByPackageType();
    }


    public Map<LocalDate, Integer> getDailyRevenueTrend(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return hotspotClientsRepository.findDailyRevenueTrend(startDate);
    }

    public Map<Integer, Integer> getHourlyRevenueDistribution(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return hotspotClientsRepository.findHourlyRevenueDistribution(start, end);
    }


}
