package com.DocMate.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;


@Repository
public class OtpDao {

    private static final Logger logger = LoggerFactory.getLogger(OtpDao.class);
    private static MongoDatabase mongoDatabase;
    private static final int MAX_ATTEMPTS = 3;

    public OtpDao(MongoDatabase db) {
        OtpDao.mongoDatabase = db;
        logger.info("MongoDatabase initialized.");
    }

//    //used
//    public static void storeOtp(String phoneNumber, String encryptedOtp) {
//        try {
//            MongoCollection<Document> collection = mongoDatabase.getCollection("otps");
//            Document document = new Document("phoneNumber", phoneNumber)
//                    .append("otp", encryptedOtp)
//                    .append("timestamp", Instant.now().toEpochMilli())
//                    .append("attempts", MAX_ATTEMPTS);  // Initialize attempt count
//            collection.insertOne(document);
//            logger.info("Stored OTP for phone number: {}", phoneNumber);
//        } catch (Exception e) {
//            logger.error("Error storing OTP for phone number: {}", phoneNumber, e);
//        }
//    }


    public static Document getOtpByPhoneNumber(String phoneNumber) {
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection("otps");
            Document query = new Document("phoneNumber", phoneNumber);
            Document otpDoc = collection.find(query).first();
            
            if (otpDoc != null) {
                logger.info("Found OTP document for phone number: {}", phoneNumber);
                return otpDoc;
            } else {
                logger.warn("No OTP document found for phone number: {}", phoneNumber);
            }
        } catch (Exception e) {
            logger.error("Error retrieving OTP document for phone number: {}", phoneNumber, e);
        }
        return null;
    }
    
    //used
    public static void upsertOtp(String phoneNumber, String otp) {
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection("otps");
            Document existingOtpDoc = collection.find(new Document("phoneNumber", phoneNumber)).first();

            if (existingOtpDoc != null) {
                // Update existing OTP record
                collection.updateOne(
                    new Document("phoneNumber", phoneNumber),
                    new Document("$set", new Document("otp", otp)
                            .append("timestamp", Instant.now().toEpochMilli())
                            .append("attempts", 3)) // Reset attempts
                );
                logger.info("Updated OTP for phone number: {}", phoneNumber);
            } else {
                // Insert new OTP record
                Document newOtpDoc = new Document("phoneNumber", phoneNumber)
                    .append("otp", otp)
                    .append("timestamp", Instant.now().toEpochMilli())
                    .append("attempts", 3);  // Reset attempts for new OTP
                collection.insertOne(newOtpDoc);
                logger.info("Stored OTP for new phone number: {}", phoneNumber);
            }
        } catch (Exception e) {
            logger.error("Error upserting OTP for phone number: {}", phoneNumber, e);
            throw new RuntimeException("Failed to upsert OTP", e);
        }
    }
    
    public static Document getOtpRecord(String phoneNumber) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("otps");
        return collection.find(new Document("phoneNumber", phoneNumber)).first();
    }

    public static void updateAttempts(String phoneNumber, int attempts) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("otps");
        collection.updateOne(new Document("phoneNumber", phoneNumber),
                new Document("$set", new Document("attempts", attempts)));
    }

    public static void decrementAttempts(String phoneNumber) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("otps");
        collection.updateOne(new Document("phoneNumber", phoneNumber),
                new Document("$inc", new Document("attempts", -1)));
    }
    
    public static List<Document> getServicesByUserId(String userId) {
        // Assuming MongoDB is connected and you have a MongoCollection<Document> collection
        MongoCollection<Document> collection = mongoDatabase.getCollection("services");
        return collection.find(eq("userId", userId)).into(new ArrayList<>());
    }
    
}
