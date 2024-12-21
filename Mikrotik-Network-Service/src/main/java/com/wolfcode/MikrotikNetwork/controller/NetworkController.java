package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.network.BandwidthDto;
import com.wolfcode.MikrotikNetwork.dto.network.IPPoolDto;
import com.wolfcode.MikrotikNetwork.dto.network.RouterRequest;
import com.wolfcode.MikrotikNetwork.entity.Routers;
import com.wolfcode.MikrotikNetwork.service.NetworkService;
import lombok.RequiredArgsConstructor;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/network")
public class NetworkController {

    private final NetworkService networkService;


    @PostMapping("/add-router")
    public String addRouter(@RequestBody RouterRequest routerRequest) {
        networkService.addRouter(routerRequest);
        return "success";
    }

    @GetMapping("/all-routers")
    private List<Routers> getAllRouters() {
        return networkService.getAllRouters();
    }

    @DeleteMapping("/delete/{routerName}")
    public String deleteRouter(@PathVariable String routerName) {
        networkService.deleteRouter(routerName);
        return "success";
    }

    @PutMapping("/{routerName}")
    public String updateRouter(@PathVariable String routerName, @RequestBody RouterRequest routerRequest) {
        networkService.updateRouter(routerName, routerRequest);
        return "success";
    }

    @PostMapping("/ip-pool")
    public ResponseEntity<String> createIPPool(@RequestBody IPPoolDto ipPoolDto) throws MikrotikApiException {
        networkService.createIPPool(ipPoolDto);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/ip-pool")
    public List<IPPoolDto> getIPPools() {
        return networkService.getIPPools();
    }

    @DeleteMapping("/ip-pool/{router}/{name}")
    public String deleteIPPool(@PathVariable String name, @PathVariable String router) throws MikrotikApiException {
        networkService.deleteIPPool(name, router);
        return "Success!";
    }


    @PostMapping("/bandwidth")
    public String addBandwidthPlan(@RequestBody BandwidthDto request) {
        networkService.addBandwidthPlan(request);
        return "Success !";
    }

    @GetMapping("/bandwidth")
    public List<BandwidthDto> getBandwidthPlan() {
        return networkService.getBandwidthPlans();
    }

    @PutMapping("/bandwidth/{id}")
    public String editBandwidthPlan(@PathVariable Long id, @RequestBody BandwidthDto request) {
        networkService.editBandwidthPlan(id, request);
        return "Success !";
    }

    @DeleteMapping("/bandwidth/{id}")
    public String deleteBandwidthPlan(@PathVariable Long id) {
        networkService.deleteBandwidthPlan(id);
        return "Success !";
    }

}
