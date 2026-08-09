package com.osmig.Jweb.app.api;

import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.db.mongo.Mongo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Stores contact-form submissions. Uses MongoDB when connected; otherwise
 * falls back to an in-memory store so the contact → admin flow still works
 * (e.g. local development without a database). In-memory messages are lost
 * on restart.
 */
@Component
public class MessageStore {

    private static final int MEMORY_LIMIT = 1000;

    // Newest first
    private final ConcurrentLinkedDeque<Doc> memory = new ConcurrentLinkedDeque<>();

    /** Saves a submission to Mongo when available, memory otherwise. */
    public void save(String name, String email, String message) {
        Doc contact = Doc.of("contacts")
            .set("name", name)
            .set("email", email)
            .set("message", message)
            .set("createdAt", new Date());

        if (Mongo.isConnected()) {
            Mongo.save(contact);
            return;
        }

        memory.addFirst(contact);
        while (memory.size() > MEMORY_LIMIT) {
            memory.pollLast();
        }
    }

    /** Returns all submissions, newest first. */
    public List<Doc> findAll() {
        if (Mongo.isConnected()) {
            return Mongo.find("contacts")
                .orderByDesc("_id")
                .toList();
        }
        return Collections.unmodifiableList(new ArrayList<>(memory));
    }
}
