package com.DocMate.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

@Service
public class TwilioService {
    private static final Logger logger = LoggerFactory.getLogger(TwilioService.class);

    @Value("${TWILIO_ACCOUNT_SID}")
    private String accountSid;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String authToken;

    @Value("${TWILIO_PHONE_NUMBER}")
    private String twilioPhoneNumber;

    public TwilioService() {
    	logger.info(accountSid+authToken+twilioPhoneNumber+"--------");
        try {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio initialized successfully.");
        } catch (Exception e) {
            logger.error("Error initializing Twilio", e);
        }
    }

    public void sendOtp(String phoneNumber, String otp) {
        try {
            String formattedPhoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+91" + phoneNumber;
            Message.creator(
                new com.twilio.type.PhoneNumber(formattedPhoneNumber),
                new com.twilio.type.PhoneNumber(twilioPhoneNumber),
                "Your OTP is: " + otp
            ).create();
            logger.info("OTP sent successfully to phone number: {}", formattedPhoneNumber);
        } catch (Exception e) {
            logger.error("Error sending OTP to phone number: {}", phoneNumber, e);
        }
    }
}
