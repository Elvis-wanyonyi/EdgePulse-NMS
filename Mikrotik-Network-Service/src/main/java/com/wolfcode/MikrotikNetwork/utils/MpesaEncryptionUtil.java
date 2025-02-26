package com.wolfcode.MikrotikNetwork.utils;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class MpesaEncryptionUtil {

    public static String encryptSecurityCredential(String plainText, String certificatePath) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (FileInputStream fis = new FileInputStream(certificatePath)) {
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(fis);
            PublicKey publicKey = certificate.getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }
    }
}
