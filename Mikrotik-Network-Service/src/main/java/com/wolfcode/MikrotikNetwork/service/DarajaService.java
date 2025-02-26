package com.wolfcode.MikrotikNetwork.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wolfcode.MikrotikNetwork.config.B2BMpesaConfig;
import com.wolfcode.MikrotikNetwork.config.MpesaConfig;
import com.wolfcode.MikrotikNetwork.dto.payment.*;
import com.wolfcode.MikrotikNetwork.entity.B2BTransaction;
import com.wolfcode.MikrotikNetwork.entity.PaymentSession;
import com.wolfcode.MikrotikNetwork.multitenancy.TenantContext;
import com.wolfcode.MikrotikNetwork.repository.B2BTransactionRepository;
import com.wolfcode.MikrotikNetwork.repository.PaymentSessionRepository;
import com.wolfcode.MikrotikNetwork.tenants.dto.ShortCodeType;
import com.wolfcode.MikrotikNetwork.tenants.entity.DarajaPaymentDetails;
import com.wolfcode.MikrotikNetwork.tenants.repo.PaymentGatewayRepo;
import com.wolfcode.MikrotikNetwork.utils.HelperUtility;
import com.wolfcode.MikrotikNetwork.utils.MpesaEncryptionUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.wolfcode.MikrotikNetwork.utils.Constants.*;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DarajaService {

    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;
    private final MpesaConfig mpesaConfig;
    private final B2BMpesaConfig b2BMpesaConfig;
    private final PaymentGatewayRepo paymentGatewayRepo;
    private final PaymentSessionRepository paymentSessionRepository;
    private final B2BTransactionRepository b2BTransactionRepository;


    public TokenResponse getAccessToken(String consumerKey, String consumerSecret) {
        String encodedCredentials = HelperUtility.toBase64String(String.format("%s:%s", consumerKey, consumerSecret));

        Request request = new Request.Builder()
                .url(String.format("%s?grant_type=%s", mpesaConfig.getOauthEndpoint(), mpesaConfig.getGrantType()))
                .get()
                .addHeader(AUTHORIZATION_HEADER_STRING, String.format("%s %s", BASIC_AUTH_STRING, encodedCredentials))
                .addHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_HEADER_VALUE)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;

            return objectMapper.readValue(response.body().string(), TokenResponse.class);
        } catch (IOException e) {
            log.error("Could not get access token. -> {}", e.getLocalizedMessage());
            return null;
        }
    }


    public StkPushSyncResponse performStkPushTransaction(PaymentRequest paymentRequest) {
        String phoneNumber = HelperUtility.sanitizePhoneNumber(paymentRequest.getPhoneNumber());
        String transactionTimestamp = HelperUtility.getTransactionTimestamp();

        String tenant = TenantContext.getCurrentTenant();
        DarajaPaymentDetails paymentDetails = paymentGatewayRepo.findByTenant(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        ExternalStkPushRequest externalStkPushRequest;
        TokenResponse accessToken;
        String businessShortCode;
        String passKey;
        String callBackURL;

        if (paymentDetails.getShortCodeType().equals(ShortCodeType.UNVERIFIED)) {
            businessShortCode = mpesaConfig.getStkPushShortCode();
            passKey = mpesaConfig.getStkPassKey();
            callBackURL = mpesaConfig.getStkPushRequestCallbackUrl();

            String stkPushPassword = HelperUtility.getStkPushPassword(businessShortCode, passKey, transactionTimestamp);
            externalStkPushRequest = ExternalStkPushRequest.builder()
                    .businessShortCode(businessShortCode)
                    .password(stkPushPassword)
                    .timestamp(transactionTimestamp)
                    .transactionType(CUSTOMER_PAYBILL_ONLINE)
                    .amount(paymentRequest.getAmount())
                    .partyA(phoneNumber)
                    .partyB(businessShortCode)
                    .phoneNumber(phoneNumber)
                    .callBackURL(callBackURL)
                    .accountReference("JUST-PAY")
                    .transactionDesc(String.format("->>>>> Transaction %s", paymentRequest.getPhoneNumber()))
                    .build();
            accessToken = getAccessToken(mpesaConfig.getConsumerKey(), mpesaConfig.getConsumerSecret());

            PaymentSession paymentSession = new PaymentSession();
            paymentSession.setShortCodeType(ShortCodeType.UNVERIFIED);
            paymentSession.setShortCode(businessShortCode);
            paymentSessionRepository.save(paymentSession);

        } else if (paymentDetails.getShortCodeType().equals(ShortCodeType.VERIFIED)) {
            businessShortCode = paymentDetails.getShortCode();
            passKey = paymentDetails.getStkPassKey();
            String accountReference = paymentDetails.getAccountReference().toUpperCase();

            String stkPushPassword = HelperUtility.getStkPushPassword(businessShortCode, passKey, transactionTimestamp);
            externalStkPushRequest = ExternalStkPushRequest.builder()
                    .businessShortCode(businessShortCode)
                    .password(stkPushPassword)
                    .timestamp(transactionTimestamp)
                    .transactionType(CUSTOMER_PAYBILL_ONLINE)
                    .amount(paymentRequest.getAmount())
                    .partyA(phoneNumber)
                    .partyB(businessShortCode)
                    .phoneNumber(phoneNumber)
                    .callBackURL(mpesaConfig.getStkPushRequestCallbackUrl())
                    .accountReference(accountReference)
                    .transactionDesc(String.format("->>>>> Transaction %s", paymentRequest.getPhoneNumber()))
                    .build();
            accessToken = getAccessToken(paymentDetails.getConsumerKey(), paymentDetails.getConsumerSecret());

            PaymentSession paymentSession = new PaymentSession();
            paymentSession.setShortCode(paymentDetails.getShortCode());
            paymentSession.setShortCodeType(ShortCodeType.VERIFIED);
            paymentSessionRepository.save(paymentSession);
        } else {
            throw new IllegalArgumentException("Invalid shortcode type");
        }

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, Objects.requireNonNull(HelperUtility.toJson(externalStkPushRequest)));
        log.info(HelperUtility.toJson(externalStkPushRequest));
        Request request = new Request.Builder()
                .url(mpesaConfig.getStkPushRequestUrl())
                .method("POST", body)
                .addHeader(AUTHORIZATION_HEADER_STRING, String.format("%s %s", BEARER_AUTH_STRING, accessToken.getAccessToken()))
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;
            return objectMapper.readValue(response.body().string(), StkPushSyncResponse.class);
        } catch (IOException e) {
            log.error("STK push transaction failed ->>>> {}", e.getLocalizedMessage());
            return null;
        }
    }


    public StkQueryResponse checkStkPushStatus(String checkoutRequestId) {
        String transactionTimestamp = HelperUtility.getTransactionTimestamp();

        String tenant = TenantContext.getCurrentTenant();
        DarajaPaymentDetails paymentDetails = paymentGatewayRepo.findByTenant(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        String businessShortCode;
        String passKey;
        TokenResponse accessToken;
        ExternalStkQueryRequest stkQueryRequest;

        if (paymentDetails.getShortCodeType().equals(ShortCodeType.UNVERIFIED)) {
            businessShortCode = mpesaConfig.getStkPushShortCode();
            passKey = mpesaConfig.getStkPassKey();

            String stkPushPassword = HelperUtility.getStkPushPassword(businessShortCode, passKey, transactionTimestamp);
            stkQueryRequest = ExternalStkQueryRequest.builder()
                    .businessShortCode(businessShortCode)
                    .password(stkPushPassword)
                    .timestamp(transactionTimestamp)
                    .checkoutRequestId(checkoutRequestId)
                    .build();
            accessToken = getAccessToken(mpesaConfig.getConsumerKey(), mpesaConfig.getConsumerSecret());

        } else if (paymentDetails.getShortCodeType().equals(ShortCodeType.VERIFIED)) {
            businessShortCode = paymentDetails.getShortCode();
            passKey = paymentDetails.getStkPassKey();

            String stkPushPassword = HelperUtility.getStkPushPassword(businessShortCode, passKey, transactionTimestamp);
            stkQueryRequest = ExternalStkQueryRequest.builder()
                    .businessShortCode(businessShortCode)
                    .password(stkPushPassword)
                    .timestamp(transactionTimestamp)
                    .checkoutRequestId(checkoutRequestId)
                    .build();

            accessToken = getAccessToken(paymentDetails.getConsumerKey(), paymentDetails.getConsumerSecret());
        } else {
            throw new IllegalArgumentException("Invalid shortcode type");
        }

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE,
                Objects.requireNonNull(HelperUtility.toJson(stkQueryRequest)));
        Request request = new Request.Builder()
                .url(mpesaConfig.getStkPushQueryUrl())
                .method("POST", body)
                .addHeader(AUTHORIZATION_HEADER_STRING,
                        String.format("%s %s", BEARER_AUTH_STRING, accessToken.getAccessToken()))
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;
            return objectMapper.readValue(response.body().string(), StkQueryResponse.class);
        } catch (IOException e) {
            log.error("STK query failed ->>>> {}", e.getLocalizedMessage());
            return null;
        }
    }

    public B2BResponse forwardPayment(PaymentSession paymentSession) {
        try {
            //TODO: Encrypt the initiator's password using the certificate
            String encryptedCredential = MpesaEncryptionUtil.encryptSecurityCredential(
                    b2BMpesaConfig.getInitiatorPassword(), b2BMpesaConfig.getCertificatePath());

            String tenantTillNumber = paymentSession.getShortCode();
            B2BRequest b2bRequest = B2BRequest.builder()
                    .Initiator(b2BMpesaConfig.getInitiatorName())
                    .SecurityCredential(encryptedCredential)
                    .CommandID(b2BMpesaConfig.getCommandId())
                    .SenderIdentifierType(b2BMpesaConfig.getSenderIdentifierType())
                    .RecieverIdentifierType(b2BMpesaConfig.getReceiverIdentifierType())
                    .Amount(paymentSession.getAmount())
                    .PartyA(b2BMpesaConfig.getOrgPaybillNumber())
                    .PartyB(tenantTillNumber)
                    .Remarks("Forwarding payment for checkoutRequestID: " + paymentSession.getCheckoutRequestID())
                    .QueueTimeOutURL(b2BMpesaConfig.getB2bTimeoutUrl())
                    .ResultURL(b2BMpesaConfig.getB2bResultUrl())
                    .build();

            String jsonPayload = objectMapper.writeValueAsString(b2bRequest);
            log.info("B2B Request Payload: {}", jsonPayload);

            RequestBody requestBody = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(b2BMpesaConfig.getB2bRequestUrl())
                    .post(requestBody)
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            if (response.body() == null) {
                throw new RuntimeException("Empty response from B2B endpoint");
            }
            String responseString = response.body().string();
            log.info("B2B Response: {}", responseString);
            B2BResponse b2bResponse = objectMapper.readValue(responseString, B2BResponse.class);

            B2BTransaction b2bTransaction = B2BTransaction.builder()
                    .checkoutRequestId(paymentSession.getCheckoutRequestID())
                    .amount(paymentSession.getAmount())
                    .conversationId(b2bResponse.getConversationID())
                    .responseCode(b2bResponse.getResponseCode())
                    .responseDescription(b2bResponse.getResponseDescription())
                    .createdAt(LocalDateTime.now())
                    .build();
            b2BTransactionRepository.save(b2bTransaction);

            return b2bResponse;
        } catch (Exception e) {
            log.error("Error forwarding B2B payment: {}", e.getMessage(), e);
            throw new RuntimeException("Error forwarding B2B payment: " + e.getMessage(), e);
        }
    }
}
