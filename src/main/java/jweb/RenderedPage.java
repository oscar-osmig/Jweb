package jweb;

import com.osmig.Jweb.framework.events.DomEvent;
import com.osmig.Jweb.framework.events.EventDispatch;
import com.osmig.Jweb.framework.server.CurrentRequest;
import com.osmig.Jweb.framework.state.StateManager;
import com.osmig.Jweb.framework.styles.PageScripts;
import com.osmig.Jweb.framework.styles.PageStyles;
import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;
import com.osmig.Jweb.framework.vdom.VRaw;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.DomUpdateResponse;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.ErrorResponse;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.EventHandledResponse;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.StateData;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.StateUpdateResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A page rendered in a test, with its server-side event loop attached.
 *
 * <p>A browser clicking a {@code button(onClick(e -> count.set(count.get() + 1)))}
 * sends the handler id over the WebSocket; the server runs the handler in the
 * page's render context, re-renders the {@code live(...)} regions that read the
 * changed state, and sends them back as DOM patches. This class does the same
 * thing without the browser: it renders the template in a fresh context, keeps
 * that context alive, finds the handler an element carries, and dispatches
 * through {@code EventDispatch} — the code the WebSocket handler uses — so a
 * test exercises exactly the protocol a page would.</p>
 *
 * <pre>
 * try (RenderedPage page = JWebTest.render(new CounterPage())) {
 *     assertTrue(page.html().contains("Count: 0"));
 *     RenderedPage.Result r = page.click("inc");
 *     assertTrue(r.ok());
 *     assertTrue(page.html().contains("Count: 1"));     // the live region, patched
 * }
 * </pre>
 *
 * <p>{@link #html()} is the page as a browser would now show it: the initial
 * render with every DOM patch applied to the region it targets. Elements are
 * addressed by their {@code id} attribute — a String or a {@link Id} handle.
 * {@link #close()} clears the context, as the server's idle reaper would.
 * A component rather than a page renders as a lambda, since a Template is just
 * {@code render()}: {@code RenderedPage.of(() -> div(...))}.</p>
 */
public final class RenderedPage implements AutoCloseable {

    private final StateManager.StateContext context;
    private final VNode tree;
    private final Map<String, String> regions = new LinkedHashMap<>();
    private final Map<String, Object> states = new LinkedHashMap<>();
    private final java.util.concurrent.BlockingQueue<Result> pushes = new java.util.concurrent.LinkedBlockingQueue<>();
    private final Consumer<List<Object>> sink;
    private boolean closed;

    private RenderedPage(StateManager.StateContext context, VNode tree) {
        this.context = context;
        this.tree = tree;
        // What the page's socket would receive from a server-side push
        // (a PubSub message, a job): collected, for awaitPush()
        this.sink = messages -> pushes.add(absorb(messages));
        com.osmig.Jweb.framework.websocket.ContextSockets.bind(context.getSessionId(), sink);
    }

    // ==================== rendering ====================

    /** Renders a template in a fresh render context (no request). */
    public static RenderedPage of(Template template) {
        return of(template, null);
    }

    /**
     * Renders a template with a request in flight: {@code beforeRender} /
     * {@code afterRender} receive it and {@code Csrf.token()}, {@code Template.session()}
     * and friends read it, as during a page route.
     */
    public static RenderedPage of(Template template, Request request) {
        StateManager.StateContext context = StateManager.createContext();
        if (request != null) CurrentRequest.set(request);
        try {
            template.beforeRender(request);
            PageStyles.collect(template);
            PageScripts.collect(template);
            Element root = template.render();
            VNode tree = root.toVNode();
            template.afterRender(request);
            return new RenderedPage(context, tree);
        } finally {
            StateManager.clearContext();
            if (request != null) CurrentRequest.clear();
        }
    }

    // A component rather than a page: a Template is just render(), so
    //   RenderedPage.of(() -> div(button(id("b"), onClick(e -> ...))))
    // renders it inside the context. (An element built before of() runs would
    // have registered its handlers outside any context — that is why there is
    // no of(Element) overload.)

    // ==================== reading ====================

    // Readers are synchronized like absorb(): a PubSub push lands on another thread.

    /** The page as a browser would show it now: the render with every patch applied. */
    public synchronized String html() {
        return rebuild(tree, regions).toHtml();
    }

    /**
     * The current HTML of one live region (or any patched element) by its
     * {@code data-live} / {@code id}, or {@code null} if no patch has targeted it.
     */
    public synchronized String region(String id) {
        return regions.get(id);
    }

    /** Every state value the server has pushed so far, by state id. */
    public synchronized Map<String, Object> states() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(states));
    }

    /**
     * The page's collected stylesheet ({@code Template.styles()}, generated class
     * rules) as a {@code <style>} tag — what the server would have injected into
     * the head. Draining: a second call returns only CSS collected since.
     */
    public String styleTag() {
        return PageStyles.drainStyleTag(context);
    }

    /** The render context id — what the page's hydration data would carry. */
    public String contextId() {
        return context.getSessionId();
    }

    /** The render context itself, for assertions on registered states and regions. */
    public StateManager.StateContext context() {
        return context;
    }

    // ==================== events ====================

    /** Fires the server {@code click} handler of the element with this id. */
    public Result click(String id) {
        return fire("click", id);
    }

    public Result click(Id id) {
        return click(id.name());
    }

    /** Fires {@code input} with the typed value — {@code onInput(e -> state.set(e.value()))}. */
    public Result input(String id, String value) {
        return fire("input", id, e -> e.value(value));
    }

    public Result input(Id id, String value) {
        return input(id.name(), value);
    }

    /** Fires {@code change} with the chosen value (a select, a text field on blur). */
    public Result change(String id, String value) {
        return fire("change", id, e -> e.value(value));
    }

    public Result change(Id id, String value) {
        return change(id.name(), value);
    }

    /** Fires {@code change} on a checkbox or radio with its checked state. */
    public Result check(String id, boolean checked) {
        return fire("change", id, e -> e.checked(checked).value(checked ? "on" : ""));
    }

    public Result check(Id id, boolean checked) {
        return check(id.name(), checked);
    }

    /** Fires {@code submit} on a form with its field values — {@code e.formData()} in the handler. */
    public Result submit(String id, Map<String, String> formData) {
        return fire("submit", id, e -> e.formData(formData));
    }

    public Result submit(Id id, Map<String, String> formData) {
        return submit(id.name(), formData);
    }

    /** Fires {@code keydown} with the key ({@code "Enter"}, {@code "Escape"}, …) and the keyCode a browser sends with it. */
    public Result keydown(String id, String key) {
        return fire("keydown", id, e -> e.key(key).keyCode(keyCodeOf(key)));
    }

    /** The legacy {@code keyCode} of a key name, as browsers still report it; -1 when there is no fixed one. */
    static int keyCodeOf(String key) {
        if (key == null) return -1;
        switch (key) {
            case "Enter": return 13;
            case "Escape": return 27;
            case "Tab": return 9;
            case "Backspace": return 8;
            case "Delete": return 46;
            case " ": case "Space": return 32;
            case "ArrowLeft": return 37;
            case "ArrowUp": return 38;
            case "ArrowRight": return 39;
            case "ArrowDown": return 40;
            case "Home": return 36;
            case "End": return 35;
            case "PageUp": return 33;
            case "PageDown": return 34;
            default:
                return key.length() == 1 && Character.isLetterOrDigit(key.charAt(0))
                    ? Character.toUpperCase(key.charAt(0)) : -1;
        }
    }

    public Result keydown(Id id, String key) {
        return keydown(id.name(), key);
    }

    /** Fires any event type on the element with this id, with no payload. */
    public Result fire(String eventType, String id) {
        return fire(eventType, id, e -> {});
    }

    public Result fire(String eventType, Id id) {
        return fire(eventType, id.name());
    }

    /**
     * Fires any event type on the element with this id, filling in what the
     * browser would report ({@link EventData}).
     *
     * @throws IllegalArgumentException when no element has that id
     * @throws IllegalStateException    when the element has no server handler for that event type
     */
    public Result fire(String eventType, String id, Consumer<EventData> event) {
        Map<String, String> attributes = attributesOf(id);
        if (attributes == null) {
            throw new IllegalArgumentException("No element with id \"" + id + "\" in the rendered page");
        }
        String handlerId = attributes.get("data-jweb-on" + eventType);
        if (handlerId == null) {
            List<String> handlers = new ArrayList<>();
            for (String name : attributes.keySet()) {
                if (name.startsWith("data-jweb-on")) handlers.add(name.substring("data-jweb-on".length()));
            }
            throw new IllegalStateException("Element \"" + id + "\" has no server handler for \"" + eventType + "\""
                + (handlers.isEmpty() ? " (it has none — an Actions-DSL handler runs in the browser, not here)"
                                      : " — it handles " + handlers));
        }
        EventData data = new EventData(eventType, id);
        event.accept(data);
        List<Object> messages = EventDispatch.dispatch(context.getSessionId(), handlerId, data.build());
        return absorb(messages);
    }

    // ==================== pushes from the server ====================

    /**
     * Waits for the next server-side push to this page — what a
     * {@code PubSub.publish(...)} the page subscribed to sends down its socket —
     * and returns it with its patches applied to {@link #html()}.
     *
     * @throws AssertionError when nothing arrives within the timeout
     */
    public Result awaitPush(java.time.Duration timeout) {
        try {
            Result result = pushes.poll(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (result == null) {
                throw new AssertionError("No push reached the page within " + timeout.toMillis() + " ms");
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted waiting for a push", e);
        }
    }

    /** Waits up to two seconds for the next push. */
    public Result awaitPush() {
        return awaitPush(java.time.Duration.ofSeconds(2));
    }

    /** The pushes received and not yet taken with {@link #awaitPush}. */
    public List<Result> pushes() {
        return List.copyOf(pushes);
    }

    private synchronized Result absorb(List<Object> messages) {
        boolean handled = false;
        boolean defaultPrevented = false;
        String error = null;
        Map<String, Object> changed = new LinkedHashMap<>();
        Map<String, String> patches = new LinkedHashMap<>();
        for (Object message : messages) {
            if (message instanceof EventHandledResponse ack) {
                handled = true;
                defaultPrevented = ack.isPreventDefault();
            } else if (message instanceof ErrorResponse err) {
                error = err.getMessage();
            } else if (message instanceof StateUpdateResponse update) {
                for (StateData s : update.getStates()) {
                    changed.put(s.getId(), s.getValue());
                    states.put(s.getId(), s.getValue());
                }
            } else if (message instanceof DomUpdateResponse update) {
                for (var patch : update.getUpdates()) {
                    patches.put(patch.getId(), patch.getHtml());
                    regions.put(patch.getId(), patch.getHtml());
                }
            }
        }
        return new Result(handled, defaultPrevented, error,
            Collections.unmodifiableMap(changed), Collections.unmodifiableMap(patches),
            Collections.unmodifiableList(messages));
    }

    /** Clears the render context — handlers and regions are gone, as after the idle reaper. */
    @Override
    public void close() {
        if (closed) return;
        closed = true;
        com.osmig.Jweb.framework.websocket.ContextSockets.unbind(context.getSessionId(), sink);
        context.clearContext();
    }

    // ==================== what an event carries ====================

    /** The fields a browser reports with an event; set the ones the handler reads. */
    public static final class EventData {
        private final DomEvent.Builder builder;
        private final Map<String, String> dataset = new LinkedHashMap<>();

        EventData(String type, String targetId) {
            this.builder = DomEvent.builder().type(type).targetId(targetId);
        }

        public EventData value(String value) { builder.value(value); return this; }
        public EventData checked(boolean checked) { builder.checked(checked); return this; }
        public EventData key(String key) { builder.key(key); return this; }
        public EventData keyCode(int keyCode) { builder.keyCode(keyCode); return this; }
        public EventData ctrlKey(boolean on) { builder.ctrlKey(on); return this; }
        public EventData shiftKey(boolean on) { builder.shiftKey(on); return this; }
        public EventData altKey(boolean on) { builder.altKey(on); return this; }
        public EventData metaKey(boolean on) { builder.metaKey(on); return this; }
        public EventData clientX(int x) { builder.clientX(x); return this; }
        public EventData clientY(int y) { builder.clientY(y); return this; }
        public EventData formData(Map<String, String> formData) { builder.formData(formData); return this; }
        /** A {@code data-*} attribute of the target — {@code e.data(name)} in the handler. */
        public EventData data(String name, String value) { dataset.put(name, value); return this; }

        DomEvent build() {
            if (!dataset.isEmpty()) builder.dataset(dataset);
            return builder.build();
        }
    }

    // ==================== what came back ====================

    /**
     * Everything the server sent back for one event.
     *
     * @param handled          the handler ran (an {@code eventHandled} message came back)
     * @param defaultPrevented the handler called {@code e.preventDefault()}
     * @param error            the server's error message, or {@code null}
     * @param states           the states this event changed, by id
     * @param patches          the DOM patches, by target id ({@code data-live} / {@code id})
     * @param messages         the raw protocol messages, in order
     */
    public record Result(boolean handled, boolean defaultPrevented, String error,
                         Map<String, Object> states, Map<String, String> patches,
                         List<Object> messages) {

        /** The handler ran and nothing went wrong. */
        public boolean ok() {
            return handled && error == null;
        }

        /** The new HTML of one patched region, or {@code null}. */
        public String patch(String id) {
            return patches.get(id);
        }
    }

    // ==================== tree helpers ====================

    /**
     * The attributes of the element with this id as the page shows it NOW: a
     * re-rendered live region replaces its first render, so an element it
     * added is found, one it removed is not, and a handler it re-minted is
     * the current one — what a browser would be clicking.
     */
    private synchronized Map<String, String> attributesOf(String id) {
        VElement element = find(rebuild(tree, regions), id);
        if (element != null) return element.getAttributes();
        // inside a patched region, whose latest content is HTML text: read the tag out of it
        for (String html : regions.values()) {
            Map<String, String> attributes = tagAttributes(html, id);
            if (attributes != null) return attributes;
        }
        return null;
    }

    private static final java.util.regex.Pattern TAG = java.util.regex.Pattern.compile("<[a-zA-Z][^>]*>");
    private static final java.util.regex.Pattern ATTRIBUTE =
        java.util.regex.Pattern.compile("([a-zA-Z_:][-a-zA-Z0-9_:.]*)\\s*=\\s*\"([^\"]*)\"");

    /** The attributes of the first tag in the HTML whose id is this one, or null. */
    static Map<String, String> tagAttributes(String html, String id) {
        java.util.regex.Matcher tag = TAG.matcher(html);
        while (tag.find()) {
            String open = tag.group();
            if (!open.contains("id=\"")) continue;
            Map<String, String> attributes = new LinkedHashMap<>();
            java.util.regex.Matcher attribute = ATTRIBUTE.matcher(open);
            while (attribute.find()) attributes.put(attribute.group(1), unescape(attribute.group(2)));
            if (id.equals(attributes.get("id"))) return attributes;
        }
        return null;
    }

    private static String unescape(String value) {
        if (value.indexOf('&') < 0) return value;
        return value.replace("&quot;", "\"").replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
    }

    private static VElement find(VNode node, String id) {
        if (node instanceof VElement element) {
            if (id.equals(element.getAttributes().get("id"))) return element;
            for (VNode child : element.getChildren()) {
                VElement found = find(child, id);
                if (found != null) return found;
            }
        } else if (node instanceof VFragment fragment) {
            for (VNode child : fragment.children()) {
                VElement found = find(child, id);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** The original tree with every patched element replaced by its latest HTML. */
    private static VNode rebuild(VNode node, Map<String, String> regions) {
        if (regions.isEmpty()) return node;
        if (node instanceof VElement element) {
            String live = element.getAttributes().get("data-live");
            if (live != null && regions.containsKey(live)) return new VRaw(regions.get(live));
            String id = element.getAttributes().get("id");
            if (id != null && regions.containsKey(id)) return new VRaw(regions.get(id));
            List<VNode> children = new ArrayList<>(element.getChildren().size());
            boolean changed = false;
            for (VNode child : element.getChildren()) {
                VNode rebuilt = rebuild(child, regions);
                changed |= rebuilt != child;
                children.add(rebuilt);
            }
            return changed ? VElement.of(element.getTag(), element.getAttributes(), children) : element;
        }
        if (node instanceof VFragment fragment) {
            List<VNode> children = new ArrayList<>(fragment.children().size());
            boolean changed = false;
            for (VNode child : fragment.children()) {
                VNode rebuilt = rebuild(child, regions);
                changed |= rebuilt != child;
                children.add(rebuilt);
            }
            return changed ? new VFragment(children) : fragment;
        }
        return node;
    }
}
