package com.osmig.Jweb.framework.websocket;

import com.osmig.Jweb.framework.events.DomEvent;
import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.state.State;
import com.osmig.Jweb.framework.state.StateManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for JWeb.
 *
 * <p>Handles messages from the browser:</p>
 * <ul>
 *   <li>Event execution requests (click, change, submit, etc.)</li>
 *   <li>State queries</li>
 * </ul>
 *
 * <p>Sends messages to the browser:</p>
 * <ul>
 *   <li>State updates</li>
 *   <li>DOM patches</li>
 * </ul>
 */
@Component
public class JWebSocketHandler extends TextWebSocketHandler {

    // Session ID -> WebSocket session
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // WebSocket session ID -> State context session ID
    private final Map<String, String> sessionContextMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("[JWeb] WebSocket connected: " + session.getId());

        // Send acknowledgment
        sendMessage(session, "{\"type\":\"connected\",\"sessionId\":\"" + session.getId() + "\"}");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("[JWeb] Received: " + payload);

        try {
            // Simple JSON parsing (can be replaced with Jackson for production)
            if (payload.contains("\"type\":\"event\"")) {
                handleEventMessage(session, payload);
            } else if (payload.contains("\"type\":\"init\"")) {
                handleInitMessage(session, payload);
            } else if (payload.contains("\"type\":\"ping\"")) {
                sendMessage(session, "{\"type\":\"pong\"}");
            }
        } catch (Exception e) {
            System.err.println("[JWeb] Error handling message: " + e.getMessage());
            e.printStackTrace();
            sendMessage(session, "{\"type\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Handles event messages from the client.
     * Format: {"type":"event","handler":"h_123","value":"...","targetId":"...","contextId":"..."}
     */
    private void handleEventMessage(WebSocketSession session, String payload) throws IOException {
        // Extract handler ID
        String handlerId = extractJsonValue(payload, "handler");
        if (handlerId == null) {
            sendMessage(session, "{\"type\":\"error\",\"message\":\"Missing handler ID\"}");
            return;
        }

        // Extract context ID and restore context for this thread
        String contextId = extractJsonValue(payload, "contextId");
        StateManager.StateContext context = null;
        if (contextId != null) {
            context = StateManager.getContextById(contextId);
            if (context != null) {
                StateManager.setContext(context);
            }
        }

        // Build event from payload
        DomEvent event = DomEvent.builder()
                .type(extractJsonValue(payload, "eventType"))
                .targetId(extractJsonValue(payload, "targetId"))
                .value(extractJsonValue(payload, "value"))
                .key(extractJsonValue(payload, "key"))
                .keyCode(extractJsonInt(payload, "keyCode", -1))
                .ctrlKey(extractJsonBool(payload, "ctrlKey"))
                .shiftKey(extractJsonBool(payload, "shiftKey"))
                .altKey(extractJsonBool(payload, "altKey"))
                .metaKey(extractJsonBool(payload, "metaKey"))
                .clientX(extractJsonInt(payload, "clientX", -1))
                .clientY(extractJsonInt(payload, "clientY", -1))
                .checked(extractJsonBool(payload, "checked"))
                .build();

        // Execute the handler
        boolean executed = EventRegistry.execute(handlerId, event);

        if (executed) {
            // Check for state changes
            if (context == null) {
                context = StateManager.getContext();
            }

            if (context != null) {
                var changedStates = context.getChangedStates();
                if (!changedStates.isEmpty()) {
                    // Send state updates
                    StringBuilder sb = new StringBuilder("{\"type\":\"stateUpdate\",\"states\":[");
                    boolean first = true;
                    for (State<?> state : changedStates) {
                        if (!first) sb.append(",");
                        sb.append(state.toJson());
                        first = false;
                    }
                    sb.append("]}");
                    sendMessage(session, sb.toString());

                    // Re-render components and send DOM updates
                    sendDomUpdates(session, context);

                    context.clearChangedStates();
                }
            }

            // Send success response
            sendMessage(session, "{\"type\":\"eventHandled\",\"handler\":\"" + handlerId +
                    "\",\"preventDefault\":" + event.isDefaultPrevented() + "}");
        } else {
            sendMessage(session, "{\"type\":\"error\",\"message\":\"Handler not found: " + handlerId + "\"}");
        }

        // Clear thread-local context
        StateManager.clearContext();
    }

    /**
     * Re-renders components and sends DOM updates to the client.
     */
    private void sendDomUpdates(WebSocketSession session, StateManager.StateContext context) throws IOException {
        var components = context.getComponents();
        if (components.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder("{\"type\":\"domUpdate\",\"updates\":[");
        boolean first = true;

        for (var entry : components.entrySet()) {
            String componentId = entry.getKey();
            var component = entry.getValue();

            String newHtml = component.render();

            if (!first) sb.append(",");
            sb.append("{\"id\":\"").append(componentId).append("\",");
            sb.append("\"html\":\"").append(escapeJson(newHtml)).append("\"}");
            first = false;
        }

        sb.append("]}");
        sendMessage(session, sb.toString());
    }

    /**
     * Handles initialization messages from the client.
     * Format: {"type":"init","contextId":"..."}
     */
    private void handleInitMessage(WebSocketSession session, String payload) throws IOException {
        String contextId = extractJsonValue(payload, "contextId");
        if (contextId != null) {
            sessionContextMap.put(session.getId(), contextId);
        }

        // Send current state if available
        StateManager.StateContext context = StateManager.getContext();
        if (context != null) {
            sendMessage(session, "{\"type\":\"initState\",\"states\":" + context.toJson() + "}");
        } else {
            sendMessage(session, "{\"type\":\"initState\",\"states\":[]}");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        sessionContextMap.remove(session.getId());
        System.out.println("[JWeb] WebSocket disconnected: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("[JWeb] WebSocket error: " + exception.getMessage());
    }

    /**
     * Sends a message to a specific session.
     */
    public void sendMessage(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    /**
     * Broadcasts a message to all connected sessions.
     */
    public void broadcast(String message) {
        sessions.values().forEach(session -> {
            try {
                sendMessage(session, message);
            } catch (IOException e) {
                System.err.println("[JWeb] Broadcast error: " + e.getMessage());
            }
        });
    }

    /**
     * Sends a state update to all sessions.
     */
    public void broadcastStateUpdate(State<?> state) {
        String message = "{\"type\":\"stateUpdate\",\"states\":[" + state.toJson() + "]}";
        broadcast(message);
    }

    // Simple JSON parsing helpers (for basic use - can be replaced with Jackson)

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private int extractJsonInt(String json, String key, int defaultValue) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return defaultValue;
        start += search.length();
        // Skip whitespace
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (end == start) return defaultValue;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean extractJsonBool(String json, String key) {
        String searchTrue = "\"" + key + "\":true";
        return json.contains(searchTrue);
    }

    private String escapeJson(String s) {
        if (s == null) return "null";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Gets the number of active connections.
     */
    public int getConnectionCount() {
        return sessions.size();
    }
}
