package com.wolfcode.MikrotikNetwork.tenants.controller;

import com.wolfcode.MikrotikNetwork.dto.payment.MpesaPaymentGateway;
import com.wolfcode.MikrotikNetwork.schema.TenantSchema;
import com.wolfcode.MikrotikNetwork.tenants.dto.TenantRequest;
import com.wolfcode.MikrotikNetwork.tenants.dto.TenantsResponse;
import com.wolfcode.MikrotikNetwork.tenants.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class TenantsController {

    private final TenantSchema tenantSchema;
    private final TenantService tenantService;

    public TenantsController(TenantSchema tenantSchema, TenantService tenantService) {
        this.tenantSchema = tenantSchema;
        this.tenantService = tenantService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerTenant(@RequestBody @Valid TenantRequest request) {
        try {
            tenantSchema.registerTenant(request);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error registering tenant", e);
        }
        return ResponseEntity.ok("Tenant registered successfully!");
    }


    @GetMapping("/all-users")
    public ResponseEntity<List<TenantsResponse>> getAllUsers() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @DeleteMapping("/tenant/{name}")
    public ResponseEntity<String> deleteTenant(@PathVariable String name) {
        tenantService.removeTenant(name);
        return ResponseEntity.ok("Tenant deleted successfully!");
    }

    @PostMapping("/add-payment-details/{tenant}")
    public String addMpesaPaymentGatewayDetails(@RequestBody @Valid MpesaPaymentGateway mpesaGateway,
                                                @PathVariable String tenant) {
        tenantService.addMpesaPaymentGatewayDetails(mpesaGateway, tenant);
        return tenant + "Payment details added successfully";
    }

    @PutMapping("/edit-payment-details/{id}")
    public String editMpesaPaymentGatewayDetails(@RequestBody @Valid MpesaPaymentGateway mpesaGateway,
                                                 @PathVariable Long id) {
        tenantService.editMpesaPaymentGatewayDetails(mpesaGateway, id);
        return "Payment details updated successfully";
    }

    @DeleteMapping("/delete-payment-details/{id}")
    public String deleteMpesaPaymentGatewayDetails(@PathVariable Long id){
        tenantService.deleteMpesaPaymentGatewayDetails(id);
        return "Payment details deleted successfully";
    }

}
