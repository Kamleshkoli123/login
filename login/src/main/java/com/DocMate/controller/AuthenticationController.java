package com.DocMate.controller;

import com.DocMate.service.OtpService;
import com.DocMate.util.OtpUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import com.DocMate.util.LoginValidationUtil;
import com.DocMate.service.JwtService;
import com.DocMate.dao.OtpDao;
import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private OtpService otpService;

    @PostMapping("/sendOtp")
    public ResponseEntity<String> sendOtp(@RequestParam("phoneNumber") String phoneNumber) {
        try {
            logger.info("Received phone number: {}", phoneNumber);

            if (!LoginValidationUtil.isValidContact(phoneNumber)) {
                logger.error("Invalid contact received");
                return ResponseEntity.status(400).body("Invalid contact number.");
            }

            Document existingOtpDoc = OtpDao.getOtpByPhoneNumber(phoneNumber);
            if (existingOtpDoc != null) {
            	logger.info("contact allready exist.");
            	if(OtpUtil.isLocked(existingOtpDoc)) {
                    logger.warn("OTP already sent within the last minute for phone number: {}", phoneNumber);
            		return ResponseEntity.status(429).body("OTP already sent, please wait for 1 minute.");
            	}
            	
            }

            otpService.sendOtp(phoneNumber, 6);
            return ResponseEntity.ok("OTP sent successfully.");
        } catch (Exception e) {
            logger.error("Error in sendOtp", e);
            return ResponseEntity.status(500).body("Error sending OTP.");
        }
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<String> verifyOtp(@RequestParam("phoneNumber") String phoneNumber,
                                            @RequestParam("otp") String inputOtp,
                                            HttpServletResponse response) {
        try {
            logger.info("Received phone number and OTP for verification.");

            if (!LoginValidationUtil.isValidContact(phoneNumber)) {
                logger.error("Invalid contact received");
                return ResponseEntity.status(400).body("Invalid contact number.");
            }

            if (!LoginValidationUtil.isValidOTP(inputOtp)) {
                logger.error("Invalid OTP received");
                return ResponseEntity.status(400).body("Invalid OTP.");
            }

            boolean otpVerified = otpService.verifyOtp(phoneNumber, inputOtp);
            if (otpVerified) {
                String jwtToken = jwtService.generateJwtToken(phoneNumber);

                // Add JWT token as a cookie
                Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(true); // Set true if using HTTPS
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(24 * 60 * 60); // Cookie valid for 1 day
                response.addCookie(jwtCookie);

                logger.info("OTP verified for phone number: {}. JWT Token set as cookie.", phoneNumber);
                return ResponseEntity.ok("OTP verified and token set as cookie.");
            } else {
                logger.warn("Invalid OTP for phone number: {}", phoneNumber);
                return ResponseEntity.status(400).body("Invalid OTP or maximum attempts reached.");
            }
        } catch (Exception e) {
            logger.error("Error in verifyOtp", e);
            return ResponseEntity.status(500).body("OTP verification failed.");
        }
    }


    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(200).body("Test success");
    }
}
