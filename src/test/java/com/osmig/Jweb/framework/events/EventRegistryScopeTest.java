package com.osmig.Jweb.framework.events;

import com.osmig.Jweb.framework.state.StateManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Handler ids are capabilities: a message naming a context can only run
 * that context's handlers. The global registry (renders with no context)
 * is never a fallback.
 */
class EventRegistryScopeTest {

    @AfterEach
    void cleanup() {
        StateManager.StateContext context = StateManager.getContext();
        if (context != null) context.clearContext();
        StateManager.clearContext();
        EventRegistry.clearAll();
    }

    @Test
    void scopedLookupDoesNotFallBackToGlobal() {
        // A handler minted outside any render lives in the global registry
        EventHandler global = EventRegistry.register("click", e -> {});
        assertNotNull(EventRegistry.get(global.getId()));

        StateManager.StateContext context = StateManager.createContext();
        assertNull(EventRegistry.get(context.getSessionId(), global.getId()),
            "a context-scoped lookup must not see global handlers");
        assertFalse(EventRegistry.execute(context.getSessionId(), global.getId(), null));
        assertNull(EventRegistry.get("ctx_does_not_exist", global.getId()));
        assertNull(EventRegistry.get(null, global.getId()));
    }

    @Test
    void scopedLookupFindsItsOwnHandlersOnly() {
        StateManager.StateContext a = StateManager.createContext();
        AtomicInteger ran = new AtomicInteger();
        EventHandler inA = EventRegistry.register("click", e -> ran.incrementAndGet());
        StateManager.clearContext();

        StateManager.StateContext b = StateManager.createContext();
        assertNull(EventRegistry.get(b.getSessionId(), inA.getId()), "another page's handler is invisible");
        assertTrue(EventRegistry.execute(a.getSessionId(), inA.getId(), null));
        assertEquals(1, ran.get());

        a.clearContext();
        assertFalse(EventRegistry.execute(a.getSessionId(), inA.getId(), null), "evicted with its context");
    }
}
