package com.DocMate.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginValidationUtil {

    private static final Logger logger = LoggerFactory.getLogger(LoginValidationUtil.class);

    // Static method to validate 6-digit OTP
    public static boolean isValidOTP(String otp) {
        logger.info("Validating OTP: {}", otp);

        if (otp == null) {
            logger.error("OTP validation failed: OTP is null");
            return false;
        }

        if (otp.length() != 4) {
            logger.error("OTP validation failed: Invalid length for OTP: {}", otp);
            return false;
        }

        if (!otp.matches("\\d{4}")) {
            logger.error("OTP validation failed: OTP contains invalid characters: {}", otp);
            return false;
        }

        logger.info("OTP syntax validation successful for OTP: {}", otp);
        return true;
    }

    // Static method to validate 10-digit Indian contact number
    public static boolean isValidContact(String contact) {
        logger.info("Validating contact: {}", contact);

        if (contact == null) {
            logger.error("Contact validation failed: Contact is null");
            return false;
        }

        if (contact.length() != 10) {
            logger.error("Contact validation failed: Invalid length for contact: {}", contact);
            return false;
        }

        if (!contact.matches("[6-9]\\d{9}")) {
            logger.error("Contact validation failed: Contact contains invalid characters or format: {}", contact);
            return false;
        }

        logger.info("Contact syntax validation successful for contact: {}", contact);
        return true;

    }
    
}
