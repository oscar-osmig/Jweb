package com.osmig.Jweb.framework;

import com.osmig.Jweb.framework.events.DomEvent;
import com.osmig.Jweb.framework.events.EventHandler;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.hydration.HydrationData;
import com.osmig.Jweb.framework.state.State;
import com.osmig.Jweb.framework.state.StateManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the server-driven reactive round-trip end to end at the framework
 * level: a render registers a handler bound to a state, the render thread is
 * detached, and a later event (as it would arrive over /jweb/event) resolves
 * the context by its id, executes the handler, and observes the state change.
 *
 * <p>These tests lock in the fixes for the reactive pipeline: the context
 * surviving past render (so its id remains resolvable), handlers being scoped
 * to their session (so they are isolated per client), and the hydration data
 * being escaped so it cannot break out of its script tag.</p>
 */
class ReactivePipelineTest {

    @AfterEach
    void tearDown() {
        StateManager.detachContext();
    }

    private static DomEvent clickEvent() {
        return DomEvent.builder().type("click").build();
    }

    @Test
    void eventAfterRenderExecutesHandlerAndSeesStateChange() {
        // --- render phase ---
        StateManager.StateContext context = StateManager.createContext();
        String contextId = context.getSessionId();

        State<Integer> count = StateManager.createState(0);
        EventHandler handler = EventRegistry.register("click", e -> count.set(count.get() + 1));
        String handlerId = handler.getId();

        // End of render: the pooled thread is detached, context stays alive.
        StateManager.detachContext();
        assertNull(StateManager.getContext(), "thread-local context must be cleared after render");

        // --- event phase (simulating a later /jweb/event request) ---
        StateManager.StateContext restored = StateManager.getContextById(contextId);
        assertNotNull(restored, "context id handed to the client must still resolve after render");

        StateManager.setContext(restored);
        boolean executed = EventRegistry.execute(contextId, handlerId, clickEvent());
        assertTrue(executed, "handler registered during render must be executable during the event");

        assertEquals(1, count.get(), "handler must have mutated the state");
        List<State<?>> changed = restored.getChangedStates();
        assertEquals(1, changed.size(), "the changed state must be reported back to the client");
        assertEquals(count.getId(), changed.get(0).getId());
    }

    @Test
    void handlersAreIsolatedBetweenSessions() {
        StateManager.StateContext ctxA = StateManager.createContext();
        String idA = ctxA.getSessionId();
        State<Integer> a = StateManager.createState(0);
        String handlerA = EventRegistry.register("click", e -> a.set(99)).getId();
        StateManager.detachContext();

        StateManager.StateContext ctxB = StateManager.createContext();
        String idB = ctxB.getSessionId();
        StateManager.detachContext();

        assertNotEquals(idA, idB);

        // Session B must not be able to invoke session A's handler.
        boolean crossExecuted = EventRegistry.execute(idB, handlerA, clickEvent());
        assertFalse(crossExecuted, "one session must not execute another session's handler");
        assertEquals(0, a.get(), "cross-session execution must not have mutated state");

        // The owning session still can.
        assertTrue(EventRegistry.execute(idA, handlerA, clickEvent()));
        assertEquals(99, a.get());
    }

    @Test
    void sessionIdsAreNotGuessable() {
        StateManager.StateContext ctx = StateManager.createContext();
        String id = ctx.getSessionId();
        StateManager.detachContext();

        assertTrue(id.startsWith("ctx_"));
        // A UUID-based id is far longer than the old "ctx_<millis>_<threadId>".
        assertTrue(id.length() > 20, "session id should be an unguessable token");
        assertFalse(id.matches("ctx_\\d+_\\d+"), "session id must not be a predictable timestamp/thread pair");
    }

    @Test
    void hydrationDataEscapesScriptBreakout() {
        HydrationData data = HydrationData.builder()
                .contextId("ctx_test")
                .stateJson("[{\"id\":\"s1\",\"value\":\"</script><script>alert(1)</script>\"}]")
                .build();

        String tag = data.toScriptTag();
        assertFalse(tag.contains("</script><script>"),
                "a state value must not be able to close the hydration script tag");
        assertTrue(tag.contains("\\u003c"), "unsafe '<' should be unicode-escaped");
    }
}
