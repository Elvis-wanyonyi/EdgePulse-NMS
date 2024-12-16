package com.wolfcode.MikrotikNetwork.tenants.controller;

import com.wolfcode.MikrotikNetwork.schema.TenantSchema;
import com.wolfcode.MikrotikNetwork.tenants.dto.TenantRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class TenantsController {

    private final TenantSchema tenantSchema;

    public TenantsController(TenantSchema tenantSchema) {
        this.tenantSchema = tenantSchema;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerTenant(@RequestBody TenantRequest request) {
        tenantSchema.registerTenant(request);
        return ResponseEntity.ok("Tenant registered successfully!");
    }


}
