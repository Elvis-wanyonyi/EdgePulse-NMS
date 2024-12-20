package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.network.BandwidthDto;
import com.wolfcode.MikrotikNetwork.service.MikrotikService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bandwidth")
public class BandwidthController {

    private final MikrotikService mikrotikService;

    public BandwidthController(MikrotikService mikrotikService) {
        this.mikrotikService = mikrotikService;
    }

    @PostMapping
    public String addBandwidthPlan(@RequestBody BandwidthDto request){
        mikrotikService.addBandwidthPlan(request);
        return "Success !";
    }

    @GetMapping
    public List<BandwidthDto> getBandwidthPlan(){
        return mikrotikService.getBandwidthPlans();
    }

    @PutMapping("/edit/{id}")
    public String editBandwidthPlan(@PathVariable Long id, @RequestBody BandwidthDto request){
        mikrotikService.editBandwidthPlan(id,request);
        return "Success !";
    }

    @DeleteMapping("{id}")
    public String deleteBandwidthPlan(@PathVariable Long id){
        mikrotikService.deleteBandwidthPlan(id);
        return "Success !";
    }
}
