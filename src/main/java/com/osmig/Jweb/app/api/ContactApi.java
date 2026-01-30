package com.osmig.Jweb.app.api;

import com.osmig.Jweb.framework.api.*;
import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.db.mongo.Mongo;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.Map;

/** Contact form API - saves submissions to MongoDB. */
@REST("/api/v1/contact")
public class ContactApi {

    @POST
    public Map<String, Object> submit(@RequestBody Map<String, String> data) {
        String name = data.get("name");
        String email = data.get("email");
        String message = data.get("message");

        if (name == null || email == null || message == null
                || name.isBlank() || email.isBlank() || message.isBlank()) {
            return Map.of("error", "All fields are required");
        }

        Doc contact = Doc.of("contacts")
            .set("name", name.trim())
            .set("email", email.trim())
            .set("message", message.trim())
            .set("createdAt", new Date());
        Mongo.save(contact);

        return Map.of("status", "ok");
    }
}
