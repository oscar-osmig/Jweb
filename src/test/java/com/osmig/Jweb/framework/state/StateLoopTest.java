package com.osmig.Jweb.framework.state;

import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.events.EventHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateLoopTest {

    @AfterEach
    void cleanup() {
        StateManager.StateContext context = StateManager.getContext();
        if (context != null) {
            context.clearContext();
        }
        StateManager.clearContext();
        EventRegistry.clearAll();
    }

    @Test
    void contextSurvivesThreadDetach() {
        StateManager.StateContext context = StateManager.createContext();
        String id = context.getSessionId();

        // Simulate end-of-render: detach from thread WITHOUT clearing registry
        StateManager.clearContext();

        // A later WebSocket event must still find the context
        assertSame(context, StateManager.getContextById(id));
    }

    @Test
    void contextIdsAreUnguessable() {
        StateManager.StateContext a = StateManager.createContext();
        StateManager.clearContext();
        StateManager.StateContext b = StateManager.createContext();

        assertNotEquals(a.getSessionId(), b.getSessionId());
        // UUID-based: no timestamp/thread-id structure
        assertTrue(a.getSessionId().matches("ctx_[0-9a-f-]{36}"));
    }

    @Test
    void statesRegisterWithActiveContext() {
        StateManager.StateContext context = StateManager.createContext();
        State<Integer> count = StateHooks.useState(0);

        assertSame(count, context.getState(count.getId()));

        count.set(5);
        assertTrue(context.getChangedStates().contains(count));
    }

    @Test
    void handlersAreScopedToContextAndEvictedWithIt() {
        StateManager.StateContext context = StateManager.createContext();
        EventHandler handler = EventRegistry.register("click", e -> {});

        // Scoped lookup works, global does not (handler is context-scoped)
        assertNotNull(EventRegistry.get(context.getSessionId(), handler.getId()));
        assertNull(EventRegistry.get(handler.getId()));

        context.clearContext();
        assertNull(EventRegistry.get(context.getSessionId(), handler.getId()));
    }

    @Test
    void handlerIdsAreUnguessable() {
        EventHandler a = EventRegistry.register("click", e -> {});
        EventHandler b = EventRegistry.register("click", e -> {});

        assertNotEquals(a.getId(), b.getId());
        assertTrue(a.getId().matches("h_\\d+_[0-9a-f-]+"), "expected random suffix: " + a.getId());
    }

    @Test
    void useComponentRegistersForDomPatching() {
        StateManager.StateContext context = StateManager.createContext();
        State<Integer> count = StateHooks.useState(1);

        var element = StateHooks.useComponent("counter", () ->
            () -> new com.osmig.Jweb.framework.vdom.VText("Count: " + count.get()));

        assertEquals(1, context.getComponents().size());
        assertTrue(element.toHtml().contains("id=\"counter\""));

        count.set(2);
        String patched = context.getComponent("counter").render();
        assertTrue(patched.contains("Count: 2"));
    }
}
