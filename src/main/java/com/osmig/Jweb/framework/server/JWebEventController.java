package com.osmig.Jweb.framework.server;

import com.osmig.Jweb.framework.events.DomEvent;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.state.State;
import com.osmig.Jweb.framework.state.StateManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP-based event controller for JWeb interactivity.
 *
 * <p>Handles event execution via POST requests instead of WebSocket.
 * This is simpler and more reliable than WebSocket for basic interactivity.</p>
 *
 * <p>Flow:</p>
 * <ol>
 *   <li>Client clicks button with onclick="JWeb.call('h_1', event)"</li>
 *   <li>JavaScript sends POST to /jweb/event with handler ID and event data</li>
 *   <li>Server executes handler, state changes</li>
 *   <li>Server returns JSON with updated state values</li>
 *   <li>Client updates DOM elements bound to those states</li>
 * </ol>
 */
@RestController
@RequestMapping("/jweb")
public class JWebEventController {

    /**
     * Handles event execution requests.
     *
     * @param body JSON with handler, contextId, value, etc.
     * @return JSON with updated states
     */
    @PostMapping(value = "/event",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleEvent(@RequestBody Map<String, Object> body) {

        String handlerId = (String) body.get("handler");
        String contextId = (String) body.get("contextId");
        String value = (String) body.getOrDefault("value", "");
        String eventType = (String) body.getOrDefault("eventType", "click");

        if (handlerId == null) {
            return ResponseEntity.badRequest()
                .body("{\"error\":\"Missing handler ID\"}");
        }

        // Restore context if available
        StateManager.StateContext context = null;
        if (contextId != null) {
            context = StateManager.getContextById(contextId);
            if (context != null) {
                StateManager.setContext(context);
            }
        }

        // Build event object
        DomEvent event = DomEvent.builder()
            .type(eventType)
            .value(value)
            .targetId((String) body.getOrDefault("targetId", ""))
            .checked(Boolean.TRUE.equals(body.get("checked")))
            .build();

        // Execute handler
        boolean executed = EventRegistry.execute(handlerId, event);

        if (!executed) {
            StateManager.clearContext();
            return ResponseEntity.ok()
                .body("{\"success\":false,\"error\":\"Handler not found: " + handlerId + "\"}");
        }

        // Get updated states
        if (context == null) {
            context = StateManager.getContext();
        }

        StringBuilder response = new StringBuilder("{\"success\":true,\"states\":[");

        if (context != null) {
            var changedStates = context.getChangedStates();
            boolean first = true;
            for (State<?> state : changedStates) {
                if (!first) response.append(",");
                response.append(state.toJson());
                first = false;
            }
            context.clearChangedStates();
        }

        response.append("]}");

        StateManager.clearContext();

        return ResponseEntity.ok().body(response.toString());
    }

    /**
     * Simple ping endpoint to check if JWeb is working.
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("{\"status\":\"ok\"}");
    }
}
