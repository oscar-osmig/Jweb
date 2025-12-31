package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.db.mongo.Mongo;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.*;

/** Database operations for TryIt feature. */
public final class TryItDb {
    private TryItDb() {}
    private static final Logger LOG = Logger.getLogger(TryItDb.class.getName());
    static final String COLLECTION = "access_requests";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static boolean initialized = false, available = false;

    public static synchronized boolean ensureConnected() {
        if (initialized) return available;
        initialized = true;
        try {
            // Use environment variables: MONGO_URI and MONGO_DB
            String uri = System.getenv("MONGO_URI");
            String db = System.getenv("MONGO_DB");
            if (uri == null || uri.isBlank()) uri = "mongodb://localhost:27017";
            if (db == null || db.isBlank()) db = "jweb";
            Mongo.connect(uri, db);
            Mongo.getDatabase().listCollectionNames().first();
            available = true;
            LOG.info("MongoDB connected for TryIt API");
        } catch (Exception e) { LOG.log(Level.WARNING, "MongoDB not available: " + e.getMessage()); }
        return available;
    }

    public static Doc findByEmail(String email) { return Mongo.find(COLLECTION).where("email", email).first(); }
    public static Doc findByToken(String token) { return Mongo.find(COLLECTION).where("token", token).first(); }
    public static Doc findById(String id) { return Mongo.findById(COLLECTION, id); }
    public static List<Doc> findByStatus(String status) { return Mongo.find(COLLECTION).where("status", status).toList(); }
    public static List<Doc> findAll() { return Mongo.find(COLLECTION).toList(); }

    public static void saveRequest(String email, String message) {
        Mongo.save(Doc.of(COLLECTION).set("email", email).set("message", message)
            .set("status", "PENDING").set("createdAt", Instant.now().toEpochMilli()));
    }

    public static String approveRequest(String id) {
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        long expiry = Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli();
        Mongo.update(COLLECTION).where("id", id).set("token", token).set("tokenExpiry", expiry)
            .set("status", "APPROVED").set("approvedAt", Instant.now().toEpochMilli()).execute();
        return token;
    }

    public static void rejectRequest(String id) {
        Mongo.update(COLLECTION).where("id", id).set("status", "REJECTED").execute();
    }

    public static void deleteByEmail(String email) {
        Mongo.delete(COLLECTION).where("email", email).execute();
    }

    public static boolean isTokenValid(Doc doc) {
        String token = doc.getString("token");
        Long expiry = doc.getLong("tokenExpiry");
        return token != null && expiry != null && Instant.now().toEpochMilli() < expiry;
    }

    public static Map<String, Object> docToMap(Doc doc) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", doc.getId() != null ? doc.getId() : "");
        m.put("email", doc.getString("email", ""));
        m.put("message", doc.getString("message", ""));
        m.put("status", doc.getString("status", ""));
        m.put("createdAt", doc.getLong("createdAt", 0L));
        m.put("approvedAt", doc.getLong("approvedAt", 0L));
        m.put("token", doc.getString("token") != null ? doc.getString("token") : "");
        m.put("tokenExpiry", doc.getLong("tokenExpiry", 0L));
        return m;
    }
}
