package com.osmig.Jweb.framework.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A conversation with the model. History is kept between {@link #send}
 * calls, so follow-up questions have context.
 *
 * <pre>
 * Chat chat = AI.chat()
 *     .system("You are a concise assistant")
 *     .model("gpt-4o")          // optional per-chat override
 *     .temperature(0.2);        // optional per-chat override
 *
 * String a = chat.send("What is JWeb?");
 * String b = chat.send("Show me an example");   // remembers the topic
 * </pre>
 */
public class Chat {

    private final AiConfig config;
    private final List<Map<String, Object>> messages = new ArrayList<>();
    private String model;
    private Double temperature;

    Chat(AiConfig config) {
        this.config = config;
    }

    /** Sets the system prompt (call before the first send). */
    public Chat system(String prompt) {
        messages.add(message("system", prompt));
        return this;
    }

    /** Overrides the configured model for this chat. */
    public Chat model(String model) {
        this.model = model;
        return this;
    }

    /** Overrides the configured temperature for this chat. */
    public Chat temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * Sends a user message and returns the assistant's reply.
     * Both are appended to the conversation history.
     */
    public String send(String userMessage) {
        messages.add(message("user", userMessage));
        AiClient.AssistantReply reply = AiClient.complete(config, messages, null, model, temperature);
        String content = reply.content() != null ? reply.content() : "";
        messages.add(message("assistant", content));
        return content;
    }

    /** The conversation history so far (role/content maps, oldest first). */
    public List<Map<String, Object>> history() {
        return Collections.unmodifiableList(messages);
    }

    /** Number of messages in the history (including the system prompt). */
    public int size() {
        return messages.size();
    }

    static Map<String, Object> message(String role, String content) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }
}
