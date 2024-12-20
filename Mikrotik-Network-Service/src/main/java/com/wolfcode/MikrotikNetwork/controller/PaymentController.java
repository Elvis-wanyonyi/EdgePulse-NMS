package com.wolfcode.MikrotikNetwork.controller;

import com.wolfcode.MikrotikNetwork.dto.Status;
import com.wolfcode.MikrotikNetwork.dto.UserCredentials;
import com.wolfcode.MikrotikNetwork.dto.hotspot.MpesaCodeRequest;
import com.wolfcode.MikrotikNetwork.dto.payment.*;
import com.wolfcode.MikrotikNetwork.entity.PaymentSession;
import com.wolfcode.MikrotikNetwork.entity.TransactionsInfo;
import com.wolfcode.MikrotikNetwork.repository.PaymentSessionRepository;
import com.wolfcode.MikrotikNetwork.repository.TransactionRepository;
import com.wolfcode.MikrotikNetwork.service.DarajaService;
import com.wolfcode.MikrotikNetwork.service.MikrotikService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@Transactional
@RequiredArgsConstructor
@RequestMapping("/payment")
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final DarajaService darajaService;
    private final AcknowledgeResponse acknowledgeResponse;
    private final TransactionRepository transactionRepository;
    private final PaymentSessionRepository paymentSessionRepository;
    private final MikrotikService mikrotikService;

    @GetMapping("/token")
    public ResponseEntity<TokenResponse> getAccessToken() {
        return ResponseEntity.ok(darajaService.getAccessToken());
    }


    @PostMapping("/stk-push")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StkPushSyncResponse> performStkPushTransaction(@Valid @RequestBody PaymentRequest paymentRequest) {

        String tempRequestId = UUID.randomUUID().toString();
        PaymentSession paymentSession = PaymentSession.builder()
                .tempRequestId(tempRequestId)
                .ip(paymentRequest.getIpAddress())
                .mac(paymentRequest.getMacAddress())
                .packageType(paymentRequest.getPackageType())
                .routerName(paymentRequest.getRouterName())
                .phoneNumber(paymentRequest.getPhoneNumber())
                .amount(paymentRequest.getAmount())
                .status(Status.PENDING)
                .build();
        paymentSessionRepository.save(paymentSession);
        System.out.println(paymentSession);

        try {
            StkPushSyncResponse stkResponse = darajaService.
                    performStkPushTransaction(paymentRequest);

            paymentSession.setCheckoutRequestID(stkResponse.getCheckoutRequestID());
            paymentSessionRepository.save(paymentSession);

            return ResponseEntity.ok(stkResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/stk-query")
    public ResponseEntity<?> checkStkPushStatus(@RequestParam String checkoutRequestId) {
        try {
            StkQueryResponse stkQueryResponse = darajaService.checkStkPushStatus(checkoutRequestId);

            if (stkQueryResponse != null && "0".equals(stkQueryResponse.getResultCode())) {
                log.info("STK Query successful: {}", stkQueryResponse);

                UserCredentials userCredentials = mikrotikService.connectUserWithQuery(checkoutRequestId);

                return ResponseEntity.ok(userCredentials);
            } else {
                log.error("ResultCode != 0 , user did not complete payment.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to verify payment...");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to perform Stk-query");
        }
    }

    @PostMapping("/code")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, String>> loginWithMpesaCode(@RequestBody MpesaCodeRequest mpesaCodeRequest) {
        Map<String, String> userCredentials = mikrotikService.loginWithMpesaCode(mpesaCodeRequest);
        if (userCredentials != null) {
            return ResponseEntity.ok(userCredentials);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @PostMapping("/transaction-status")
    public ResponseEntity<AcknowledgeResponse> acknowledgeStkPushResponse(@RequestBody StkPushAsyncResponse stkPushAsyncResponse) {

        StkCallback stkCallback = stkPushAsyncResponse.getBody().getStkCallback();

        if (stkCallback.getResultCode() == 0) {
            log.info("Payment successful");

            String mpesaReceiptNumber = "N/A";
            String amount = "N/A";
            String phoneNumber = "N/A";

            for (ItemItem item : stkCallback.getCallbackMetadata().getItem()) {
                if ("MpesaReceiptNumber".equals(item.getName())) {
                    mpesaReceiptNumber = item.getValue() != null ? item.getValue() : "N/A";
                } else if ("Amount".equals(item.getName())) {
                    amount = item.getValue() != null ? item.getValue() : "N/A";
                } else if ("PhoneNumber".equals(item.getName())) {
                    phoneNumber = item.getValue() != null ? item.getValue() : "N/A";
                }
            }
            TransactionsInfo transactionsInfo = TransactionsInfo.builder()
                    .code(mpesaReceiptNumber)
                    .phoneNumber(phoneNumber)
                    .amount(amount)
                    .date(LocalDateTime.now())
                    .build();
            transactionRepository.save(transactionsInfo);

            String checkoutRequestID = stkCallback.getCheckoutRequestID();
            PaymentSession paymentSession = paymentSessionRepository.findByCheckoutRequestID(checkoutRequestID);
            paymentSession.setStatus(Status.CONFIRMED);
            paymentSessionRepository.save(paymentSession);

            mikrotikService.connectUser(paymentSession.getIp(), paymentSession.getMac(),
                    paymentSession.getPackageType(), paymentSession.getRouterName(),
                    phoneNumber, mpesaReceiptNumber);

            log.info("Transaction Details -> Phone: {}, Amount: {}, Mpesa Receipt: {}", phoneNumber, amount, mpesaReceiptNumber);

        } else {
            log.warn("Payment failed with result code: {}", stkCallback.getResultCode());
        }
        return ResponseEntity.ok(acknowledgeResponse);
    }


}