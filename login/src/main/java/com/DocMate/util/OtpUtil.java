package com.DocMate.util;

import com.DocMate.dao.OtpDao;
import com.DocMate.service.TwilioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;
import java.time.Instant;
import java.util.Random;

public class OtpUtil {

    private static final Logger logger = LoggerFactory.getLogger(OtpUtil.class);

    // Generate OTP
    public static String generateOtp(int length) {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));  // Generate a random digit
        }
        logger.info("Generated OTP: {}", otp);
        return otp.toString();
    }

    // Hash OTP
    public static String hashOtp(String plainOtp) {
        try {
            return HashUtil.hashOtp(plainOtp);
        } catch (Exception e) {
            logger.error("Error hashing OTP", e);
            throw new RuntimeException("Failed to hash OTP", e);
        }
    }

    // Store OTP in database with timestamp
    public static void storeOtp(String phoneNumber, String otp) {
        try {
            OtpDao.upsertOtp(phoneNumber, otp);
            logger.info("Stored OTP for phone number: {}", phoneNumber);
        } catch (Exception e) {
            logger.error("Error storing OTP for phone number: {}", phoneNumber, e);
            throw new RuntimeException("Failed to store OTP", e);
        }
    }

    // Send OTP via Twilio
    public static void sendOtp(String phoneNumber, String otp) {
        try {
            TwilioService twilioService = new TwilioService();
            twilioService.sendOtp(phoneNumber, otp);
            logger.info("Sent OTP to phone number: {}", phoneNumber);
        } catch (Exception e) {
            logger.error("Error sending OTP to phone number: {}", phoneNumber, e);
            throw new RuntimeException("Failed to send OTP", e);
        }
    }

    // Handle OTP generation, hashing, storing, and sending
    public static void otpHandler(String phoneNumber, int otpLength) {
        try {
            String plainOtp = generateOtp(otpLength);
            String hashedOtp = hashOtp(plainOtp);
            sendOtp(phoneNumber, plainOtp);
            storeOtp(phoneNumber, hashedOtp);
        } catch (Exception e) {
            logger.error("Error during OTP handling for phone number: {}", phoneNumber, e);
            throw new RuntimeException("Failed to process OTP", e);
        }
    }

    // Check if OTP is expired
    public static boolean isOtpExpired(long otpTimestamp) {
        long currentTime = Instant.now().toEpochMilli();
        return currentTime - otpTimestamp > 300000; // Expiration time in milliseconds (e.g., 5 minutes)
    }

    // Verify OTP hash
    public static boolean verifyOtpHash(String storedOtp, String providedOtp) {
        String hashedOtp = hashOtp(providedOtp);
        return storedOtp.equals(hashedOtp);
    }

    // Verify OTP
    public static boolean verifyOtp(String phoneNumber, String otp) {
        try {
            Document storedOtpDoc = OtpDao.getOtpByPhoneNumber(phoneNumber);
            if (storedOtpDoc != null) {
                long otpTimestamp = storedOtpDoc.getLong("timestamp");
                int attempts = storedOtpDoc.getInteger("attempts");

                if (isOtpExpired(otpTimestamp)) {
                    otpHandler(phoneNumber, 6);  // Resend OTP if expired
                    return false;
                }

                if (attempts <= 0) {
                    otpHandler(phoneNumber, 6);  // Resend OTP if attempts exhausted
                    return false;
                }

                String storedOtp = storedOtpDoc.getString("otp");
                if (verifyOtpHash(storedOtp, otp)) {
                    OtpDao.updateAttempts(phoneNumber, 0);  // Reset attempts
                    return true;
                } else {
                    OtpDao.decrementAttempts(phoneNumber);  // Reduce attempts
                    logger.warn("Invalid OTP for phone number: {}. Attempts left: {}", phoneNumber, attempts - 1);
                    return false;
                }
            } else {
                logger.warn("No OTP found for phone number: {}", phoneNumber);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error verifying OTP for phone number: {}", phoneNumber, e);
            return false;
        }
    }
    
 // Check if OTP was sent recently
    public static boolean isLocked(Document existingOtpDoc) {
        long currentTime = Instant.now().toEpochMilli();
        long otpTimestamp = existingOtpDoc.getLong("timestamp");

        // Restrict OTP sending within one minute (60000 milliseconds)
        if (currentTime - otpTimestamp < 60000) {
            logger.info("OTP send restricted: already sent recently.");
            return true;
        }
        logger.info("OTP can be sent.");
        return false;
    }
}
