package com.osmig.Jweb.app.tryit;

import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.db.mongo.Mongo;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Repository for AccessRequest documents in MongoDB.
 * Uses the JWeb framework's MongoDB utilities (Mongo, Doc, MongoQuery).
 */
@Repository
public class AccessRequestRepository {

    private static final Logger LOG = Logger.getLogger(AccessRequestRepository.class.getName());
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "jweb";
    private static final String COLLECTION_NAME = "access_requests";

    private boolean initialized = false;
    private boolean available = true;

    /**
     * Lazy initialization - connects to MongoDB on first use.
     */
    private synchronized void ensureInitialized() {
        if (initialized) return;
        initialized = true;

        try {
            Mongo.connect(CONNECTION_STRING, DATABASE_NAME);
            // Test connection by accessing the database
            Mongo.getDatabase().listCollectionNames().first();
            LOG.info("Connected to MongoDB at " + CONNECTION_STRING);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "MongoDB not available: " + e.getMessage() +
                ". Try It feature will not work until MongoDB is running.");
            available = false;
        }
    }

    private void checkAvailable() {
        ensureInitialized();
        if (!available) {
            throw new IllegalStateException("MongoDB is not available. Please ensure MongoDB is running at " + CONNECTION_STRING);
        }
    }

    public AccessRequest save(AccessRequest request) {
        checkAvailable();

        Doc doc = toDoc(request);
        Mongo.save(doc);

        // Get the generated ID if this was a new insert
        if (request.getId() == null) {
            request.setId(doc.getId());
        }

        return request;
    }

    public Optional<AccessRequest> findById(String id) {
        checkAvailable();
        try {
            Doc doc = Mongo.findById(COLLECTION_NAME, id);
            return Optional.ofNullable(doc).map(this::fromDoc);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Optional<AccessRequest> findByEmail(String email) {
        checkAvailable();
        Doc doc = Mongo.find(COLLECTION_NAME)
            .where("email", email)
            .first();
        return Optional.ofNullable(doc).map(this::fromDoc);
    }

    public Optional<AccessRequest> findByToken(String token) {
        checkAvailable();
        Doc doc = Mongo.find(COLLECTION_NAME)
            .where("token", token)
            .first();
        return Optional.ofNullable(doc).map(this::fromDoc);
    }

    public List<AccessRequest> findByStatus(AccessRequest.Status status) {
        checkAvailable();
        return Mongo.find(COLLECTION_NAME)
            .where("status", status.name())
            .toList()
            .stream()
            .map(this::fromDoc)
            .collect(Collectors.toList());
    }

    public List<AccessRequest> findAll() {
        checkAvailable();
        return Mongo.find(COLLECTION_NAME)
            .toList()
            .stream()
            .map(this::fromDoc)
            .collect(Collectors.toList());
    }

    public void updateStatus(String id, AccessRequest.Status status) {
        checkAvailable();
        Mongo.update(COLLECTION_NAME)
            .where("id", id)
            .set("status", status.name())
            .execute();
    }

    public void setToken(String id, String token, Instant expiry) {
        checkAvailable();
        Mongo.update(COLLECTION_NAME)
            .where("id", id)
            .set("token", token)
            .set("tokenExpiry", expiry.toEpochMilli())
            .set("status", AccessRequest.Status.APPROVED.name())
            .set("approvedAt", Instant.now().toEpochMilli())
            .execute();
    }

    public void delete(String id) {
        checkAvailable();
        Mongo.deleteById(COLLECTION_NAME, id);
    }

    private Doc toDoc(AccessRequest request) {
        Doc doc = Doc.of(COLLECTION_NAME);

        if (request.getId() != null) {
            doc.id(request.getId());
        }

        doc.set("email", request.getEmail())
           .set("message", request.getMessage())
           .set("status", request.getStatus().name())
           .set("token", request.getToken())
           .set("tokenExpiry", request.getTokenExpiry() != null ? request.getTokenExpiry().toEpochMilli() : null)
           .set("createdAt", request.getCreatedAt().toEpochMilli())
           .set("approvedAt", request.getApprovedAt() != null ? request.getApprovedAt().toEpochMilli() : null);

        return doc;
    }

    private AccessRequest fromDoc(Doc doc) {
        AccessRequest request = new AccessRequest();
        request.setId(doc.getId());
        request.setEmail(doc.getString("email"));
        request.setMessage(doc.getString("message"));
        request.setStatus(AccessRequest.Status.valueOf(doc.getString("status")));
        request.setToken(doc.getString("token"));

        Long tokenExpiry = doc.getLong("tokenExpiry");
        if (tokenExpiry != null) {
            request.setTokenExpiry(Instant.ofEpochMilli(tokenExpiry));
        }

        Long createdAt = doc.getLong("createdAt");
        if (createdAt != null) {
            request.setCreatedAt(Instant.ofEpochMilli(createdAt));
        }

        Long approvedAt = doc.getLong("approvedAt");
        if (approvedAt != null) {
            request.setApprovedAt(Instant.ofEpochMilli(approvedAt));
        }

        return request;
    }
}
