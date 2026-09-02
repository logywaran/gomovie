package com.gomovie.payment.provider.mobi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobiChecksumService {

    private final MobiProperties mobiProperties;

    public String generateChecksum(
            String amount,
            String sellerOrderNo,
            String subMid) {

        log.debug(
                "Generating Mobi checksum for sellerOrderNo={}",
                sellerOrderNo
        );

        String input =
                amount + "|" +
                        sellerOrderNo + "|" +
                        subMid;

        try {
            SecureRandom secureRandom = new SecureRandom();

            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);

            IvParameterSpec ivSpec =
                    new IvParameterSpec(iv);

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            KeySpec spec =
                    new PBEKeySpec(
                            mobiProperties.getMid().toCharArray(),
                            mobiProperties.getTid()
                                    .getBytes(StandardCharsets.UTF_8),
                            65536,
                            256
                    );

            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(
                            factory.generateSecret(spec)
                                    .getEncoded(),
                            "AES"
                    );

            Cipher cipher =
                    Cipher.getInstance(
                            "AES/CBC/PKCS5Padding"
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKeySpec,
                    ivSpec
            );

            byte[] cipherText =
                    cipher.doFinal(
                            input.getBytes(StandardCharsets.UTF_8)
                    );

            byte[] encryptedData =
                    new byte[iv.length + cipherText.length];

            System.arraycopy(
                    iv,
                    0,
                    encryptedData,
                    0,
                    iv.length
            );

            System.arraycopy(
                    cipherText,
                    0,
                    encryptedData,
                    iv.length,
                    cipherText.length
            );

            String checksum =
                    Base64.getEncoder()
                            .encodeToString(encryptedData);

            log.debug(
                    "Mobi checksum generated successfully for sellerOrderNo={}",
                    sellerOrderNo
            );

            return checksum;

        } catch (Exception e) {

            log.error(
                    "Failed to generate Mobi checksum for sellerOrderNo={}",
                    sellerOrderNo,
                    e
            );

            throw new IllegalStateException(
                    "Failed to generate Mobi checksum",
                    e
            );
        }
    }
}