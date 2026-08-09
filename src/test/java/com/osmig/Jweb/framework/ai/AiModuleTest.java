package com.osmig.Jweb.framework.ai;

import com.osmig.Jweb.framework.util.Json;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the AI module against a scripted mock of an OpenAI-compatible
 * server (JDK HttpServer — no extra dependencies, no API key).
 */
class AiModuleTest {

    private HttpServer server;
    private final Deque<String> scriptedResponses = new ArrayDeque<>();
    private final List<Map<String, Object>> receivedRequests = new ArrayList<>();
    private AiConfig config;

    @BeforeEach
    void startMockProvider() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            synchronized (receivedRequests) {
                receivedRequests.add(Json.parseMap(body));
            }
            String response = scriptedResponses.isEmpty() ? textReply("(no script)") : scriptedResponses.poll();
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();

        config = new AiConfig()
            .enabled(true)
            .baseUrl("http://localhost:" + server.getAddress().getPort() + "/v1")
            .apiKey("test-key")
            .model("test-model");
        AI.configure(config);
    }

    @AfterEach
    void stopMockProvider() {
        server.stop(0);
        scriptedResponses.clear();
        receivedRequests.clear();
        AI.configure(new AiConfig());
    }

    private static String textReply(String content) {
        return Json.stringify(Map.of("choices", List.of(Map.of(
            "message", Map.of("role", "assistant", "content", content)))));
    }

    private static String toolCallReply(String callId, String toolName, String argsJson) {
        return Json.stringify(Map.of("choices", List.of(Map.of(
            "message", Map.of(
                "role", "assistant",
                "tool_calls", List.of(Map.of(
                    "id", callId,
                    "type", "function",
                    "function", Map.of("name", toolName, "arguments", argsJson))))))));
    }

    // ==================== Tests ====================

    @Test
    void askReturnsModelReply() {
        scriptedResponses.add(textReply("JWeb is a Java web framework."));

        assertEquals("JWeb is a Java web framework.", AI.ask("What is JWeb?"));

        Map<String, Object> request = receivedRequests.get(0);
        assertEquals("test-model", request.get("model"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void chatKeepsHistoryAcrossSends() {
        scriptedResponses.add(textReply("First answer"));
        scriptedResponses.add(textReply("Second answer"));

        Chat chat = AI.chat().system("Be brief");
        chat.send("Question one");
        chat.send("Question two");

        // Second request must contain the full history:
        // system + user1 + assistant1 + user2
        List<Map<String, Object>> messages =
            (List<Map<String, Object>>) receivedRequests.get(1).get("messages");
        assertEquals(4, messages.size());
        assertEquals("system", messages.get(0).get("role"));
        assertEquals("First answer", messages.get(2).get("content"));
        assertEquals("Question two", messages.get(3).get("content"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void agentRunsToolLoopAndFeedsResultsBack() {
        scriptedResponses.add(toolCallReply("call_1", "get_weather", "{\"city\":\"Paris\"}"));
        scriptedResponses.add(textReply("It's raining in Paris — pack an umbrella."));

        AtomicReference<Map<String, Object>> toolArgs = new AtomicReference<>();
        Tool weather = Tool.of("get_weather", "Get the weather for a city")
            .param("city", "The city name")
            .handler(args -> {
                toolArgs.set(args);
                return "rain, 12C";
            });

        String answer = AI.agent()
            .system("You are a travel assistant")
            .tools(weather)
            .run("Should I pack an umbrella for Paris?");

        assertEquals("It's raining in Paris — pack an umbrella.", answer);
        assertEquals("Paris", toolArgs.get().get("city"));

        // First request advertises the tool schema
        List<Map<String, Object>> tools =
            (List<Map<String, Object>>) receivedRequests.get(0).get("tools");
        Map<String, Object> function = (Map<String, Object>) tools.get(0).get("function");
        assertEquals("get_weather", function.get("name"));

        // Second request contains the tool result message
        List<Map<String, Object>> messages =
            (List<Map<String, Object>>) receivedRequests.get(1).get("messages");
        Map<String, Object> toolMessage = messages.get(messages.size() - 1);
        assertEquals("tool", toolMessage.get("role"));
        assertEquals("call_1", toolMessage.get("tool_call_id"));
        assertEquals("rain, 12C", toolMessage.get("content"));
    }

    @Test
    void agentStopsAtMaxSteps() {
        // The model keeps asking for tools forever
        for (int i = 0; i < 5; i++) {
            scriptedResponses.add(toolCallReply("call_" + i, "noop", "{}"));
        }
        Tool noop = Tool.of("noop", "Does nothing").handler(args -> "ok");

        var ex = assertThrows(AiException.class, () ->
            AI.agent().tools(noop).maxSteps(3).run("Loop forever"));
        assertTrue(ex.getMessage().contains("3 steps"));
    }

    @Test
    void disabledConfigThrowsClearError() {
        AI.configure(new AiConfig());   // disabled
        var ex = assertThrows(AiException.class, () -> AI.ask("hi"));
        assertTrue(ex.getMessage().contains("jweb.ai.enabled"));
    }
}
