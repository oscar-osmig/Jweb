package com.osmig.Jweb.framework.events;

import java.util.function.Consumer;

/**
 * Wraps a Java lambda event handler with a unique ID.
 *
 * <p>When you write:</p>
 * <pre>
 * button(attrs().onClick(e -> count.set(count.get() + 1)), text("Click"))
 * </pre>
 *
 * <p>The lambda is wrapped in an EventHandler with a unique ID.
 * The HTML renders as:</p>
 * <pre>
 * &lt;button onclick="JWeb.call('handler_123', event)"&gt;Click&lt;/button&gt;
 * </pre>
 *
 * <p>When clicked, the client sends a WebSocket message with "handler_123",
 * and the server looks up and executes the original Java lambda.</p>
 */
public class EventHandler {

    private final String id;
    private final String eventType;
    private final Consumer<Event> handler;

    /**
     * Creates a new EventHandler.
     *
     * @param id the unique handler ID
     * @param eventType the DOM event type (click, change, submit, etc.)
     * @param handler the Java lambda to execute
     */
    public EventHandler(String id, String eventType, Consumer<Event> handler) {
        this.id = id;
        this.eventType = eventType;
        this.handler = handler;
    }

    /**
     * Gets the unique handler ID.
     *
     * @return the handler ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the DOM event type this handler responds to.
     *
     * @return the event type (click, change, submit, etc.)
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Executes the handler with the given event.
     *
     * @param event the event to process
     */
    public void handle(Event event) {
        handler.accept(event);
    }

    /**
     * Generates the JavaScript attribute value for this handler.
     *
     * @return JavaScript code to trigger this handler
     */
    public String toJsAttribute() {
        return "JWeb.call('" + id + "', event)";
    }

    /**
     * Generates the HTML attribute name for this event type.
     *
     * @return the attribute name (onclick, onchange, etc.)
     */
    public String getAttributeName() {
        return "on" + eventType;
    }

    @Override
    public String toString() {
        return "EventHandler[" + id + ", " + eventType + "]";
    }
}
