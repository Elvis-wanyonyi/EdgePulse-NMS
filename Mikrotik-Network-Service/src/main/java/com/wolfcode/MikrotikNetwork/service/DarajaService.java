package com.wolfcode.MikrotikNetwork.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wolfcode.MikrotikNetwork.config.MpesaConfig;
import com.wolfcode.MikrotikNetwork.dto.payment.*;
import com.wolfcode.MikrotikNetwork.utils.HelperUtility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
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


    public TokenResponse getAccessToken() {

        String encodedCredentials = HelperUtility.toBase64String(String.format("%s:%s", mpesaConfig.getConsumerKey(),
                mpesaConfig.getConsumerSecret()));

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
        String stkPushPassword = HelperUtility.getStkPushPassword(mpesaConfig.getStkPushShortCode(),
                mpesaConfig.getStkPassKey(), transactionTimestamp);

        ExternalStkPushRequest externalStkPushRequest = ExternalStkPushRequest.builder()
                .businessShortCode(mpesaConfig.getStkPushShortCode())
                .password(stkPushPassword)
                .timestamp(transactionTimestamp)
                .transactionType(CUSTOMER_PAYBILL_ONLINE)
                .amount(paymentRequest.getAmount())
                .partyA(phoneNumber)
                .partyB(mpesaConfig.getStkPushShortCode())
                .phoneNumber(phoneNumber)
                .callBackURL(mpesaConfig.getStkPushRequestCallbackUrl())
                .accountReference("JUST-PAY")
                .transactionDesc(String.format("->>>>> Transaction %s", paymentRequest.getPhoneNumber()))
                .build();

        TokenResponse accessToken = getAccessToken();

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE,
                Objects.requireNonNull(HelperUtility.toJson(externalStkPushRequest)));
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
        String stkPushPassword = HelperUtility.getStkPushPassword(mpesaConfig.getStkPushShortCode(),
                mpesaConfig.getStkPassKey(), transactionTimestamp);

        ExternalStkQueryRequest stkQueryRequest = ExternalStkQueryRequest.builder()
                .businessShortCode(mpesaConfig.getStkPushShortCode())
                .password(stkPushPassword)
                .timestamp(transactionTimestamp)
                .checkoutRequestId(checkoutRequestId)
                .build();

        TokenResponse accessToken = getAccessToken();

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE,
                Objects.requireNonNull(HelperUtility.toJson(stkQueryRequest)));

        Request request = new Request.Builder()
                .url(mpesaConfig.getStkPushQueryUrl())
                .method("POST", body)
                .addHeader(AUTHORIZATION_HEADER_STRING, String.format("%s %s", BEARER_AUTH_STRING, accessToken.getAccessToken()))
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
}
