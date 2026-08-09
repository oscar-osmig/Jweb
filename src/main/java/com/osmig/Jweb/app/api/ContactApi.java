package com.osmig.Jweb.app.api;

import com.osmig.Jweb.framework.api.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/** Contact form API - saves submissions via MessageStore (Mongo or in-memory). */
@REST("/api/v1/contact")
public class ContactApi {

    private final MessageStore messageStore;

    public ContactApi(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    @POST
    public Map<String, Object> submit(@RequestBody Map<String, String> data) {
        String name = data.get("name");
        String email = data.get("email");
        String message = data.get("message");

        if (name == null || email == null || message == null
                || name.isBlank() || email.isBlank() || message.isBlank()) {
            return Map.of("error", "All fields are required");
        }

        messageStore.save(name.trim(), email.trim(), message.trim());

        return Map.of("status", "ok");
    }
}
