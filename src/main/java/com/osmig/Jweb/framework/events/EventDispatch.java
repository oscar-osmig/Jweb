package com.osmig.Jweb.framework.events;

import com.osmig.Jweb.framework.state.State;
import com.osmig.Jweb.framework.state.StateManager;
import com.osmig.Jweb.framework.util.Log;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.DomPatch;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.DomUpdateResponse;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.ErrorResponse;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.EventHandledResponse;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.StateData;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.StateUpdateResponse;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.ThreePatchResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * One browser event, from handler lookup to the messages the page gets back.
 *
 * <p>This is the server half of the event loop in one place: restore the page's
 * render context, run the handler in that context only, then collect what
 * changed — scene patches queued by {@code Three.patch(...)}, the changed
 * states, the live regions that read them re-rendered as DOM patches — and the
 * handled acknowledgement. {@code JWebSocketHandler} sends the list over the
 * socket; {@link jweb.RenderedPage} reads it in a test, so a test exercises
 * exactly the protocol a browser would.</p>
 */
public final class EventDispatch {

    public static final String CONTEXT_EXPIRED = "context_expired";

    private EventDispatch() {}

    /**
     * Runs the handler for one event and returns everything the page is sent
     * back, in order: {@link ThreePatchResponse}s, a {@link StateUpdateResponse}
     * and a {@link DomUpdateResponse} when states changed, then the
     * {@link EventHandledResponse} — or a single {@link ErrorResponse}.
     *
     * @param contextId the page's render context, or {@code null} for a handler
     *                  minted outside any render (the global registry)
     * @param handlerId the handler the element carries
     * @param event     the event as the browser reported it
     */
    public static List<Object> dispatch(String contextId, String handlerId, DomEvent event) {
        List<Object> out = new ArrayList<>();
        if (handlerId == null) {
            out.add(new ErrorResponse("Missing handler ID"));
            return out;
        }

        StateManager.StateContext context = null;
        if (contextId != null) {
            context = StateManager.getContextById(contextId);
            if (context == null) {
                // The page's context was reaped (or never existed): its
                // handlers are gone with it. Never fall through to the
                // global registry — log and drop.
                Log.warn("Dropping event {} for unknown context {}", handlerId, contextId);
                out.add(new ErrorResponse("This page's session expired — reload to continue", CONTEXT_EXPIRED));
                return out;
            }
            StateManager.setContext(context);
        }

        // A message that names a context resolves the handler in that context
        // ONLY; the global registry serves just the messages that name none
        // (handlers minted outside any render). Three.patch(...) calls made
        // inside it collect on this thread and ride back with the answer.
        jweb.three.ThreePatchQueue.open();
        try {
            boolean executed = contextId != null
                    ? EventRegistry.execute(contextId, handlerId, event)
                    : EventRegistry.execute(handlerId, event);
            if (!executed) {
                Log.warn("Dropping event for handler {} — not registered in context {}", handlerId, contextId);
                out.add(new ErrorResponse("Handler not found: " + handlerId));
                return out;
            }
            threePatches(out);
            if (context == null) {
                context = StateManager.getContext();
            }
            if (context != null) {
                flushStateChanges(context, out);
            }
            out.add(new EventHandledResponse(handlerId, event.isDefaultPrevented()));
            return out;
        } finally {
            StateManager.clearContext();
            jweb.three.ThreePatchQueue.close();
        }
    }

    /**
     * A {@code JWeb.setState(id, value)} call from the client: sets the state in
     * the page's context and returns the resulting messages (derived states and
     * DOM patches included), or one {@link ErrorResponse}.
     */
    public static List<Object> setState(String contextId, String stateId, Object value) {
        List<Object> out = new ArrayList<>();
        StateManager.StateContext context = StateManager.getContextById(contextId);
        if (context == null) {
            out.add(new ErrorResponse("Unknown context: " + contextId));
            return out;
        }
        State<Object> state = context.getState(stateId);
        if (state == null) {
            out.add(new ErrorResponse("Unknown state: " + stateId));
            return out;
        }
        StateManager.setContext(context);
        jweb.three.ThreePatchQueue.open();
        try {
            state.set(value);
            threePatches(out);
            flushStateChanges(context, out);
            return out;
        } finally {
            StateManager.clearContext();
            jweb.three.ThreePatchQueue.close();
        }
    }

    /**
     * After a handler ran: the changed states, the live regions that read them
     * re-rendered, and the change set cleared.
     */
    private static void flushStateChanges(StateManager.StateContext context, List<Object> out) {
        var changedStates = context.getChangedStates();
        if (changedStates.isEmpty()) {
            return;
        }
        List<StateData> stateDataList = new ArrayList<>();
        Set<String> changedIds = new HashSet<>();
        for (State<?> state : changedStates) {
            stateDataList.add(new StateData(state.getId(), state.get()));
            changedIds.add(state.getId());
        }
        out.add(new StateUpdateResponse(stateDataList));
        domUpdates(context, changedIds, out);
        context.clearChangedStates();
    }

    /** Scene patches queued by {@code Three.patch(...)} during the handler — one message each. */
    private static void threePatches(List<Object> out) {
        for (var patch : jweb.three.ThreePatchQueue.drain()) {
            out.add(new ThreePatchResponse(
                    patch.sceneId(), patch.nodeMaps(), patch.cameraMap(),
                    patch.addMaps(), patch.removeNames()));
        }
    }

    /**
     * Re-renders the live regions affected by the changed states as DOM patches
     * (the runtime morphs each into place), with the CSS and script definitions
     * the re-render introduced.
     */
    private static void domUpdates(StateManager.StateContext context, Set<String> changedStateIds, List<Object> out) {
        var components = context.getComponents();
        if (components.isEmpty()) {
            return;
        }
        List<DomPatch> patches = new ArrayList<>();
        for (var entry : components.entrySet()) {
            var component = entry.getValue();
            if (!component.affectedBy(changedStateIds)) continue;
            String newHtml = component.render();
            // A re-render can be the first to need a stylesheet or a
            // generated class rule; the patch carries the CSS it introduced
            // (a <style> set through innerHTML applies).
            String css = com.osmig.Jweb.framework.styles.PageStyles.drainStyleTag(context);
            patches.add(new DomPatch(entry.getKey(), css + newHtml));
        }
        if (patches.isEmpty()) {
            return;
        }
        // Re-renders can mint Actions-DSL handlers the page has never seen
        // (conditional branches); ship their definitions with the patches
        String actionsJs = com.osmig.Jweb.framework.js.ClientActions.drainJs(context);
        // ... and a re-rendered component can bring a scripts() hook with it
        String pageJs = com.osmig.Jweb.framework.styles.PageScripts.drainJs(context);
        if (pageJs != null) actionsJs = actionsJs == null ? pageJs : actionsJs + ";" + pageJs;
        out.add(new DomUpdateResponse(patches, actionsJs));
    }
}
