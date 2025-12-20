package com.osmig.Jweb.framework.websocket;

import com.osmig.Jweb.framework.events.DomEvent;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.state.State;
import com.osmig.Jweb.framework.state.StateManager;
import com.osmig.Jweb.framework.util.Json;
import com.osmig.Jweb.framework.util.Log;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        Log.debug("WebSocket connected: {}", session.getId());

        // Send acknowledgment
        sendMessage(session, new ConnectedResponse(session.getId()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Log.trace("Received: {}", payload);

        try {
            // Parse the base message to determine type
            Base baseMessage = Json.parse(payload, Base.class);
            String type = baseMessage.getType();

            if (type == null) {
                sendMessage(session, new ErrorResponse("Missing message type"));
                return;
            }

            switch (type) {
                case "event" -> handleEventMessage(session, Json.parse(payload, EventMessage.class));
                case "init" -> handleInitMessage(session, Json.parse(payload, InitMessage.class));
                case "ping" -> sendMessage(session, new PongResponse());
                default -> sendMessage(session, new ErrorResponse("Unknown message type: " + type));
            }
        } catch (Json.JsonException e) {
            Log.warn("JSON parse error: {}", e.getMessage());
            sendMessage(session, new ErrorResponse("Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            Log.error("Error handling message: {}", e.getMessage(), e);
            sendMessage(session, new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Handles event messages from the client.
     */
    private void handleEventMessage(WebSocketSession session, EventMessage msg) throws IOException {
        String handlerId = msg.getHandler();
        if (handlerId == null) {
            sendMessage(session, new ErrorResponse("Missing handler ID"));
            return;
        }

        // Extract context ID and restore context for this thread
        String contextId = msg.getContextId();
        StateManager.StateContext context = null;
        if (contextId != null) {
            context = StateManager.getContextById(contextId);
            if (context != null) {
                StateManager.setContext(context);
            }
        }

        // Build event from message
        DomEvent event = DomEvent.builder()
                .type(msg.getEventType())
                .targetId(msg.getTargetId())
                .value(msg.getValue())
                .key(msg.getKey())
                .keyCode(msg.getKeyCode() != null ? msg.getKeyCode() : -1)
                .ctrlKey(Boolean.TRUE.equals(msg.getCtrlKey()))
                .shiftKey(Boolean.TRUE.equals(msg.getShiftKey()))
                .altKey(Boolean.TRUE.equals(msg.getAltKey()))
                .metaKey(Boolean.TRUE.equals(msg.getMetaKey()))
                .clientX(msg.getClientX() != null ? msg.getClientX() : -1)
                .clientY(msg.getClientY() != null ? msg.getClientY() : -1)
                .checked(Boolean.TRUE.equals(msg.getChecked()))
                .formData(msg.getFormData())
                .dataset(msg.getDataset())
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
                    List<StateData> stateDataList = new ArrayList<>();
                    for (State<?> state : changedStates) {
                        stateDataList.add(new StateData(state.getId(), state.get()));
                    }
                    sendMessage(session, new StateUpdateResponse(stateDataList));

                    // Re-render components and send DOM updates
                    sendDomUpdates(session, context);

                    context.clearChangedStates();
                }
            }

            // Send success response
            sendMessage(session, new EventHandledResponse(handlerId, event.isDefaultPrevented()));
        } else {
            sendMessage(session, new ErrorResponse("Handler not found: " + handlerId));
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

        List<DomPatch> patches = new ArrayList<>();
        for (var entry : components.entrySet()) {
            String componentId = entry.getKey();
            var component = entry.getValue();
            String newHtml = component.render();
            patches.add(new DomPatch(componentId, newHtml));
        }

        sendMessage(session, new DomUpdateResponse(patches));
    }

    /**
     * Handles initialization messages from the client.
     */
    private void handleInitMessage(WebSocketSession session, InitMessage msg) throws IOException {
        String contextId = msg.getContextId();
        if (contextId != null) {
            sessionContextMap.put(session.getId(), contextId);
        }

        // Send current state if available
        StateManager.StateContext context = StateManager.getContext();
        List<StateData> stateDataList = new ArrayList<>();

        if (context != null) {
            for (State<?> state : context.getStates().values()) {
                stateDataList.add(new StateData(state.getId(), state.get()));
            }
        }

        sendMessage(session, new InitStateResponse(stateDataList));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        sessionContextMap.remove(session.getId());
        Log.debug("WebSocket disconnected: {}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Log.error("WebSocket error: {}", exception.getMessage(), exception);
    }

    /**
     * Sends a message object to a specific session.
     * The object will be serialized to JSON.
     */
    public void sendMessage(WebSocketSession session, Object message) throws IOException {
        if (session.isOpen()) {
            String json = Json.stringify(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    /**
     * Sends a raw JSON string to a specific session.
     */
    public void sendRawMessage(WebSocketSession session, String json) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(json));
        }
    }

    /**
     * Broadcasts a message to all connected sessions.
     */
    public void broadcast(Object message) {
        String json = Json.stringify(message);
        sessions.values().forEach(session -> {
            try {
                sendRawMessage(session, json);
            } catch (IOException e) {
                Log.warn("Broadcast error: {}", e.getMessage());
            }
        });
    }

    /**
     * Sends a state update to all sessions.
     */
    public void broadcastStateUpdate(State<?> state) {
        List<StateData> states = List.of(new StateData(state.getId(), state.get()));
        broadcast(new StateUpdateResponse(states));
    }

    /**
     * Gets the number of active connections.
     */
    public int getConnectionCount() {
        return sessions.size();
    }
}
