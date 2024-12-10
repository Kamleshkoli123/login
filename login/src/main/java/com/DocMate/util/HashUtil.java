package com.DocMate.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashUtil {

    private static final Logger logger = LoggerFactory.getLogger(HashUtil.class);

    // Hash OTP using SHA-256
    public static String hashOtp(String plainOtp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(plainOtp.getBytes(StandardCharsets.UTF_8));
            
            logger.info("otp hash : {} {}", plainOtp, Base64.getEncoder().encodeToString(hashedBytes));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error hashing OTP", e);
            throw new RuntimeException("Error hashing OTP", e);
        }
    }

    // Verify if hashed OTP matches stored OTP
    public static boolean verifyOtpHash(String storedOtp, String inputOtp) {
        String hashedInputOtp = HashUtil.hashOtp(inputOtp);
        return storedOtp.equals(hashedInputOtp);
    }
}
