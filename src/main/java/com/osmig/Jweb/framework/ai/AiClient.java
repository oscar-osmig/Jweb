package com.osmig.Jweb.framework.ai;

import com.osmig.Jweb.framework.http.Fetch;
import com.osmig.Jweb.framework.http.FetchResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Low-level chat-completions client speaking the OpenAI wire format
 * (works with OpenAI, Ollama, Groq, LM Studio, vLLM, ...).
 *
 * <p>Most apps should use {@link AI}, {@link Chat} or {@link Agent} instead.</p>
 */
final class AiClient {

    private AiClient() {}

    /**
     * Sends a chat completion request and returns the assistant message
     * (content and any tool calls).
     */
    @SuppressWarnings("unchecked")
    static AssistantReply complete(AiConfig config, List<Map<String, Object>> messages,
                                   List<Tool> tools, String modelOverride, Double temperatureOverride) {
        if (!config.isEnabled()) {
            throw new AiException("AI is disabled — set jweb.ai.enabled: true and configure jweb.ai.base-url");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", modelOverride != null ? modelOverride : config.getModel());
        payload.put("messages", messages);
        payload.put("temperature", temperatureOverride != null ? temperatureOverride : config.getTemperature());
        if (tools != null && !tools.isEmpty()) {
            List<Map<String, Object>> toolDefs = new ArrayList<>();
            for (Tool tool : tools) {
                toolDefs.add(tool.toApiDefinition());
            }
            payload.put("tools", toolDefs);
        }

        Fetch request = Fetch.post(config.getBaseUrl() + "/chat/completions")
            .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
            .json(payload);
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            request.bearer(config.getApiKey());
        }

        FetchResult result;
        try {
            result = request.send();
        } catch (Exception e) {
            throw new AiException("AI request failed: " + e.getMessage(), e);
        }
        if (!result.isOk()) {
            throw new AiException("AI provider returned " + result.status() + ": " + truncate(result.body()));
        }

        Map<String, Object> body = result.asMap();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new AiException("AI provider returned no choices: " + truncate(result.body()));
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = message.get("content") instanceof String s ? s : null;
        List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) message.get("tool_calls");

        return new AssistantReply(content, toolCalls, message);
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }

    /** The assistant's turn: text content and/or requested tool calls. */
    record AssistantReply(String content, List<Map<String, Object>> toolCalls,
                          Map<String, Object> rawMessage) {

        boolean wantsTools() {
            return toolCalls != null && !toolCalls.isEmpty();
        }
    }
}
