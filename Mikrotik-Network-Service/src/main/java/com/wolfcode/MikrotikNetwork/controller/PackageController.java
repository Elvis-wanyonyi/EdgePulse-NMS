package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.network.PackagePlanDto;
import com.wolfcode.MikrotikNetwork.service.MikrotikService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans/")
public class PackageController {


    private final MikrotikService mikrotikService;

    @PostMapping
    public ResponseEntity<String> createHotspotPackage(@Valid @RequestBody PackagePlanDto packagePlanDto) {
        mikrotikService.createHotspotPlan(packagePlanDto);
        return ResponseEntity.ok("Successfully created hotspot plan");
    }

    @GetMapping
    public ResponseEntity<List<PackagePlanDto>> getHotspotPackages() {
        return ResponseEntity.ok(mikrotikService.getHotspotPlans());
    }

    @PutMapping("edit/{id}")
    public ResponseEntity<String> editHotspotPlan(@PathVariable Long id,
                                                  @Valid @RequestBody PackagePlanDto packagePlanDto) {
        mikrotikService.editHotspotPlan(id, packagePlanDto);
        return ResponseEntity.ok("Successfully updated hotspot plan");
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteHotspotPlan(@PathVariable Long id) {
        mikrotikService.deleteHotspotPlan(id);
        return ResponseEntity.ok("Successfully deleted hotspot plan");
    }
}

