package com.wolfcode.MikrotikNetwork.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class HelperUtility {
    public static String toBase64String(String value) {
        byte[] data = value.getBytes(StandardCharsets.ISO_8859_1);
        return Base64.getEncoder().encodeToString(data);
    }

    public static String toJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }


    public static String getTransactionUniqueNumber() {
        RandomStringGenerator stringGenerator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                .build();
        return stringGenerator.generate(10).toUpperCase();
    }

    public static String getStkPushPassword(String shortCode, String passKey, String timestamp) {
        String concatenatedString = String.format("%s%s%s", shortCode, passKey, timestamp);
        return toBase64String(concatenatedString);
    }

    public static String getTransactionTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date());
    }


    public static String sanitizePhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.trim();

        if (phoneNumber.startsWith("0")) {
            phoneNumber = phoneNumber.replaceFirst("0", "254");
        }

        if (phoneNumber.length() != 12 || !phoneNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid phone number format. It should be 12 digits starting with 254.");
        }

        return phoneNumber;
    }
}
