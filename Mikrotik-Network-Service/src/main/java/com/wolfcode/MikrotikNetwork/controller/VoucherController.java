package com.wolfcode.MikrotikNetwork.controller;

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
@RequestMapping("/voucher")
public class VoucherController {

    private final MikrotikService mikrotikService;


    @PostMapping("/add")
    public String createHotspotVoucher(@RequestBody CreateVouchers createVouchers) {
        mikrotikService.createHotspotVoucher(createVouchers);
        return "success";
    }

    @PostMapping
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

    @DeleteMapping("/{voucherCode}")
    public String deleteVoucher(@PathVariable String voucherCode) {
        mikrotikService.deleteVoucher(voucherCode);
        return "success";
    }

    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(mikrotikService.getAllVouchers());
    }
}
