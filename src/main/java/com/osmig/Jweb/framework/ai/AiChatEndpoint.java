package com.osmig.Jweb.framework.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ready-made chat endpoint backing {@link AiChatWidget}. Active when
 * {@code jweb.ai.enabled=true}.
 *
 * <p>POST /jweb/ai/chat {"sessionId": "...", "message": "..."} →
 * {"reply": "..."} — conversation history is kept per session id and
 * evicted after 30 minutes of inactivity.</p>
 */
@RestController
@ConditionalOnProperty(name = "jweb.ai.enabled", havingValue = "true")
public class AiChatEndpoint {

    private static final long SESSION_TTL_MS = 30 * 60 * 1000;
    private static final int MAX_SESSIONS = 10_000;
    private static final int MAX_MESSAGE_CHARS = 8_000;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private record Session(Chat chat, long[] lastUsed) {}

    @PostMapping("/jweb/ai/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> body) {
        String sessionId = body.get("sessionId");
        String message = body.get("message");
        if (sessionId == null || sessionId.isBlank() || message == null || message.isBlank()) {
            return Map.of("error", "sessionId and message are required");
        }
        if (message.length() > MAX_MESSAGE_CHARS) {
            return Map.of("error", "Message too long");
        }
        if (!AI.isConfigured()) {
            return Map.of("error", "AI is not configured (jweb.ai.*)");
        }

        evictStale();

        Session session = sessions.computeIfAbsent(sessionId,
            id -> new Session(AI.chat().system(systemPrompt()), new long[]{System.currentTimeMillis()}));
        session.lastUsed()[0] = System.currentTimeMillis();

        try {
            String reply = session.chat().send(message);
            return Map.of("reply", reply);
        } catch (AiException e) {
            return Map.of("error", e.getMessage());
        }
    }

    /** Override point: the system prompt for widget conversations. */
    protected String systemPrompt() {
        return "You are a helpful assistant embedded in this website. Be concise.";
    }

    private void evictStale() {
        long now = System.currentTimeMillis();
        if (sessions.size() > MAX_SESSIONS) {
            sessions.clear();
            return;
        }
        sessions.values().removeIf(s -> now - s.lastUsed()[0] > SESSION_TTL_MS);
    }
}
