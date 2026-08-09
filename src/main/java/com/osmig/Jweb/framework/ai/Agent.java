package com.osmig.Jweb.framework.ai;

import com.osmig.Jweb.framework.util.Json;
import com.osmig.Jweb.framework.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * An agent loop: the model reasons, calls your {@link Tool}s, sees their
 * results, and repeats until it produces a final answer (or hits
 * {@link #maxSteps}).
 *
 * <pre>
 * String result = AI.agent()
 *     .system("You are a support agent for JWeb")
 *     .tools(searchDocs, createTicket)
 *     .maxSteps(8)
 *     .onStep((step, info) -> Log.info("agent step {}: {}", step, info))
 *     .run("My page returns 405 on POST — what's wrong?");
 * </pre>
 */
public class Agent {

    private final AiConfig config;
    private final List<Map<String, Object>> messages = new ArrayList<>();
    private final List<Tool> tools = new ArrayList<>();
    private String model;
    private Double temperature;
    private int maxSteps = 10;
    private BiConsumer<Integer, String> onStep;

    Agent(AiConfig config) {
        this.config = config;
    }

    /** Sets the system prompt. */
    public Agent system(String prompt) {
        messages.add(Chat.message("system", prompt));
        return this;
    }

    /** Registers the tools the model may call. */
    public Agent tools(Tool... tools) {
        for (Tool t : tools) {
            this.tools.add(t);
        }
        return this;
    }

    /** Overrides the configured model for this agent. */
    public Agent model(String model) {
        this.model = model;
        return this;
    }

    /** Overrides the configured temperature for this agent. */
    public Agent temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    /** Maximum reason/act iterations before giving up (default 10). */
    public Agent maxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
        return this;
    }

    /** Observer called each step with (stepNumber, description) — for logging/UI. */
    public Agent onStep(BiConsumer<Integer, String> observer) {
        this.onStep = observer;
        return this;
    }

    /**
     * Runs the loop for one task and returns the model's final answer.
     * The agent keeps its history, so {@code run} can be called again
     * for follow-ups.
     */
    @SuppressWarnings("unchecked")
    public String run(String task) {
        messages.add(Chat.message("user", task));

        for (int step = 1; step <= maxSteps; step++) {
            AiClient.AssistantReply reply = AiClient.complete(config, messages, tools, model, temperature);

            if (!reply.wantsTools()) {
                String content = reply.content() != null ? reply.content() : "";
                messages.add(Chat.message("assistant", content));
                notifyStep(step, "final answer");
                return content;
            }

            // Record the assistant turn (with its tool_calls) then execute each tool
            messages.add(reply.rawMessage());
            for (Map<String, Object> call : reply.toolCalls()) {
                Map<String, Object> function = (Map<String, Object>) call.get("function");
                String toolName = (String) function.get("name");
                String argsJson = (String) function.get("arguments");
                notifyStep(step, "calling " + toolName + " " + argsJson);

                String resultJson = executeTool(toolName, argsJson);

                Map<String, Object> toolMessage = new LinkedHashMap<>();
                toolMessage.put("role", "tool");
                toolMessage.put("tool_call_id", call.get("id"));
                toolMessage.put("content", resultJson);
                messages.add(toolMessage);
            }
        }

        throw new AiException("Agent did not finish within " + maxSteps + " steps");
    }

    private String executeTool(String toolName, String argsJson) {
        Tool tool = tools.stream()
            .filter(t -> t.name().equals(toolName))
            .findFirst()
            .orElse(null);
        if (tool == null) {
            return "Error: unknown tool '" + toolName + "'";
        }
        try {
            Map<String, Object> args = argsJson == null || argsJson.isBlank()
                ? Map.of()
                : Json.parseMap(argsJson);
            Object result = tool.invoke(args);
            return result instanceof String s ? s : Json.stringify(result);
        } catch (Exception e) {
            Log.warn("Tool '{}' failed: {}", toolName, e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private void notifyStep(int step, String info) {
        if (onStep != null) {
            onStep.accept(step, info);
        }
    }
}
