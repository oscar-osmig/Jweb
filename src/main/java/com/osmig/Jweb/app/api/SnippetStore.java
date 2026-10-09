package com.osmig.Jweb.app.api;

import jweb.Doc;
import jweb.Mongo;
import jweb.api.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

/**
 * Stores community snippets and their review state. A snippet is submitted
 * as {@code pending}, becomes {@code approved} when an admin publishes it,
 * and is deleted when rejected or unpublished. Uses MongoDB when connected;
 * otherwise an in-memory store so the submit → review → publish flow still
 * works in local development (in-memory snippets are lost on restart).
 *
 * <p>A Mongo call that fails — the database is configured but unreachable —
 * logs a warning and falls back to memory too, so the public gallery
 * degrades to "no snippets yet" rather than answering 500.</p>
 */
@Component
public class SnippetStore {

    public static final String COLLECTION = "snippets";
    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";

    private static final int MEMORY_LIMIT = 500;
    private static final System.Logger LOG = System.getLogger(SnippetStore.class.getName());

    // Newest first
    private final ConcurrentLinkedDeque<Doc> memory = new ConcurrentLinkedDeque<>();

    /** Saves a submission as pending review and returns it with its id set. */
    public Doc submit(String title, String author, String code) {
        Doc snippet = Doc.of(COLLECTION)
            .set("title", title)
            .set("author", author)
            .set("code", code)
            .set("status", PENDING)
            .set("createdAt", new Date());

        return withMongo(() -> Mongo.save(snippet), () -> {
            snippet.id(Mongo.newId());
            memory.addFirst(snippet);
            while (memory.size() > MEMORY_LIMIT) {
                memory.pollLast();
            }
            return snippet;
        });
    }

    /** Snippets awaiting review, newest first. */
    public List<Doc> pending() {
        return byStatus(PENDING);
    }

    /** Published snippets, newest first. */
    public List<Doc> approved() {
        return byStatus(APPROVED);
    }

    private List<Doc> byStatus(String status) {
        return withMongo(
            () -> Mongo.find(COLLECTION).where("status", status).orderByDesc("_id").toList(),
            () -> memory.stream().filter(d -> status.equals(d.getString("status"))).toList());
    }

    /** A snippet by id; empty for an unknown or malformed id. */
    public Optional<Doc> byId(String id) {
        if (!Mongo.isValidId(id)) return Optional.empty();
        return withMongo(
            () -> Mongo.findByIdOptional(COLLECTION, id),
            () -> memory.stream().filter(d -> id.equals(d.getId())).findFirst());
    }

    /** Publishes a snippet. False when there is no such snippet. */
    public boolean approve(String id) {
        if (byId(id).isEmpty()) return false;
        Date now = new Date();
        return withMongo(() -> {
            Mongo.update(COLLECTION).where("id", id)
                .set("status", APPROVED)
                .set("approvedAt", now)
                .execute();
            return true;
        }, () -> {
            byId(id).ifPresent(d -> d.set("status", APPROVED).set("approvedAt", now));
            return true;
        });
    }

    /** Removes a snippet — a rejection or an unpublish. False when there is no such snippet. */
    public boolean delete(String id) {
        if (!Mongo.isValidId(id)) return false;
        return withMongo(
            () -> Mongo.deleteById(COLLECTION, id),
            () -> memory.removeIf(d -> id.equals(d.getId())));
    }

    /** Mongo when connected and answering; the in-memory store otherwise. */
    private static <T> T withMongo(Supplier<T> query, Supplier<T> fallback) {
        if (!Mongo.isConnected()) return fallback.get();
        try {
            return query.get();
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.WARNING,
                "MongoDB unavailable for snippets, using the in-memory store: " + e.getMessage());
            return fallback.get();
        }
    }
}
