package com.DocMate.service;

import com.DocMate.dao.OtpDao;
import com.DocMate.util.OtpUtil;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    @Autowired
    private TwilioService twilioService;

    @Value("${otp.expiration}")
    private long otpExpirationMillis;

    // Generate OTP and send it
    public void sendOtp(String phoneNumber, int otpLength) {
        try {
            String plainOtp = OtpUtil.generateOtp(otpLength);
            String hashedOtp = OtpUtil.hashOtp(plainOtp);

            OtpUtil.storeOtp(phoneNumber, hashedOtp);
            twilioService.sendOtp(phoneNumber, plainOtp);

            logger.info("OTP generated, hashed, stored, and sent for phone number: {}", phoneNumber);
        } catch (Exception e) {
            logger.error("Error in sendOtp", e);
            throw new RuntimeException("Failed to send OTP", e);
        }
    }

 // Verify if OTP is correct and handle attempts or expiration
    public boolean verifyOtp(String phoneNumber, String inputOtp) {
        try {
            Document storedOtpDoc = OtpDao.getOtpByPhoneNumber(phoneNumber);

            if (storedOtpDoc == null) {
                logger.warn("No OTP record found for phone number: {}", phoneNumber);
                return false;
            }

            int attempts = storedOtpDoc.getInteger("attempts", 0); // Default to 0 if field is missing
            if (attempts <= 0) {
                logger.warn("No remaining attempts for phone number: {}", phoneNumber);
                return false;
            }

            long otpTimestamp = storedOtpDoc.getLong("timestamp");

            // Check if OTP is expired
            if (OtpUtil.isOtpExpired(otpTimestamp)) {
                logger.warn("OTP expired. Sending a new one for phone number: {}", phoneNumber);
                return false;
            }

            // Verify OTP
            String storedOtp = storedOtpDoc.getString("otp");
            if (OtpUtil.verifyOtpHash(storedOtp, inputOtp)) {
                OtpDao.updateAttempts(phoneNumber, 0);  // Reset attempts on success
                logger.info("OTP verified for phone number: {}", phoneNumber);
                return true;
            } else {
                OtpDao.decrementAttempts(phoneNumber);  // Decrement attempts on failure
                logger.warn("Incorrect OTP for phone number: {}. Attempts left: {}", phoneNumber, attempts - 1);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error verifying OTP", e);
            return false;
        }
    }

}
