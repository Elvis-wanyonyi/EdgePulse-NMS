package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.RevenueReportRequest;
import com.wolfcode.MikrotikNetwork.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;


    @GetMapping("/today")
    public ResponseEntity<Integer> getTodayRevenue() {
        int todayRevenue = revenueService.calculateTodayRevenue();
        return ResponseEntity.ok(todayRevenue);
    }

    @GetMapping("/month")
    public ResponseEntity<Integer> getThisMonthRevenue() {
        int monthRevenue = revenueService.calculateThisMonthRevenue();
        return ResponseEntity.ok(monthRevenue);
    }

    @PostMapping("/reports")
    public ResponseEntity<Integer> getRevenueReport(@RequestBody RevenueReportRequest request) {
        int reportRevenue = revenueService.calculateRevenueReport(request);
        return ResponseEntity.ok(reportRevenue);
    }

    @GetMapping("/by-router")
    public ResponseEntity<Integer> getRevenueByRouter(@RequestParam("router") String router) {
        int routerRevenue = revenueService.calculateRevenueByRouter(router);
        return ResponseEntity.ok(routerRevenue);
    }

    @GetMapping("/plan-reports")
    public ResponseEntity<Map<String, Integer>> getRevenueByPackage() {
        Map<String, Integer> revenueByPackage = revenueService.calculateRevenueByPackage();
        return ResponseEntity.ok(revenueByPackage);
    }

}
