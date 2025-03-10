package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.ClientResponse;
import com.wolfcode.MikrotikNetwork.dto.hotspot.ActiveUsersResponse;
import com.wolfcode.MikrotikNetwork.dto.hotspot.ClientDetailsResponse;
import com.wolfcode.MikrotikNetwork.dto.hotspot.ClientLogs;
import com.wolfcode.MikrotikNetwork.dto.hotspot.Invoices;
import com.wolfcode.MikrotikNetwork.dto.network.PlanDto;
import com.wolfcode.MikrotikNetwork.dto.voucher.CreateVouchers;
import com.wolfcode.MikrotikNetwork.dto.voucher.RedeemVoucher;
import com.wolfcode.MikrotikNetwork.dto.voucher.UpdateVoucher;
import com.wolfcode.MikrotikNetwork.dto.voucher.VoucherResponse;
import com.wolfcode.MikrotikNetwork.entity.Voucher;
import com.wolfcode.MikrotikNetwork.service.MikrotikService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotspot")
public class HotspotController {

    private final MikrotikService mikrotikService;


    @PostMapping("/add-voucher")
    public String createHotspotVoucher(@RequestBody CreateVouchers createVouchers) {
        mikrotikService.createHotspotVoucher(createVouchers);
        return "success";
    }

    @PostMapping("/voucher")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, String>> redeemVoucher(@Valid @RequestBody RedeemVoucher redeemVoucher) throws MikrotikApiException {
        Map<String, String> userCredentials = mikrotikService.redeemVoucher(redeemVoucher);
        if (userCredentials != null) {
            return ResponseEntity.ok(userCredentials);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @PatchMapping("/edit-voucher/{voucherCode}")
    public ResponseEntity<VoucherResponse> editVoucher(@PathVariable String voucherCode,
                                                       @RequestBody UpdateVoucher updateVoucher) {
        VoucherResponse voucherResponse = mikrotikService.editVoucher(voucherCode, updateVoucher);
        return ResponseEntity.ok(voucherResponse);
    }

    @DeleteMapping("/voucher/{voucherCode}")
    public String deleteVoucher(@PathVariable String voucherCode) {
        mikrotikService.deleteVoucher(voucherCode);
        return "success";
    }

    @GetMapping("/all-vouchers")
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(mikrotikService.getAllVouchers());
    }

    @PostMapping("/add-plan")
    public ResponseEntity<String> createHotspotPackage(@Valid @RequestBody PlanDto planDto) {
        mikrotikService.createHotspotPlan(planDto);
        return ResponseEntity.ok("Successfully created hotspot plan");
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanDto>> getHotspotPackages() {
        return ResponseEntity.ok(mikrotikService.getHotspotPlans());
    }

    @PutMapping("/plan/edit/{id}")
    public ResponseEntity<String> editHotspotPlan(@PathVariable Long id,
                                                  @Valid @RequestBody PlanDto planDto) {
        mikrotikService.editHotspotPlan(id, planDto);
        return ResponseEntity.ok("Successfully updated hotspot plan");
    }

    @DeleteMapping("/plan/{id}")
    public ResponseEntity<String> deleteHotspotPlan(@PathVariable Long id) {
        mikrotikService.deleteHotspotPlan(id);
        return ResponseEntity.ok("Successfully deleted hotspot plan");
    }

    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id) throws MikrotikApiException {
        mikrotikService.deleteHotspotClient(id);
        return "success";
    }

    @GetMapping("/clients")
    public List<ClientResponse> getAllClients() {
        return mikrotikService.getAllClients();
    }

    @GetMapping("/totalActive-users")
    public int getTotalActiveClients() throws MikrotikApiException {
        return mikrotikService.getTotalActiveClients();
    }

    @GetMapping("/active-clients")
    public List<ActiveUsersResponse> getAllActiveClients() throws MikrotikApiException {
        return mikrotikService.getAllActiveClients();
    }

    @GetMapping("/total-connected-clients")
    public int getTotalOfflineClients() throws MikrotikApiException {
        return mikrotikService.getTotalConnectedUsers();
    }

    @GetMapping("/connected")
    public List<ClientResponse> getConnectedUsers() {
        return mikrotikService.getConnectedUsers();
    }

    @GetMapping("/client/{username}")
    public ClientDetailsResponse getClientByUsername(@PathVariable String username) throws MikrotikApiException {
        return mikrotikService.getClientByUsername(username);
    }

    @GetMapping("/logs/{username}")
    public ResponseEntity<List<ClientLogs>> getClientLogs(@PathVariable("username") String username) throws MikrotikApiException {
        List<ClientLogs> logs = mikrotikService.getClientLogs(username);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/invoices/{username}")
    public ResponseEntity<List<Invoices>> getClientInvoices(@PathVariable("username") String username) {
        try {
            List<Invoices> invoices = mikrotikService.getClientInvoices(username);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
