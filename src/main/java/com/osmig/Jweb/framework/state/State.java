package com.osmig.Jweb.framework.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Core reactive state holder - similar to React's useState.
 *
 * <p>Usage:</p>
 * <pre>
 * private final State&lt;Integer&gt; count = useState(0);
 *
 * // Read value
 * count.get()  // returns 0
 *
 * // Set value
 * count.set(5)
 *
 * // Update based on current value
 * count.update(c -> c + 1)
 * </pre>
 *
 * @param <T> The type of the state value
 */
public class State<T> {

    private T value;
    private final String id;
    private final List<Consumer<T>> subscribers = new ArrayList<>();
    private boolean dirty = false;

    /**
     * Creates a new State with the given initial value.
     * Use {@link StateHooks#useState(Object)} instead of calling this directly.
     */
    State(String id, T initialValue) {
        this.id = id;
        this.value = initialValue;
    }

    /**
     * Gets the current state value.
     *
     * @return the current value
     */
    public T get() {
        return value;
    }

    /**
     * Sets a new state value.
     * If the value is different from the current value, subscribers are notified.
     *
     * @param newValue the new value to set
     */
    public void set(T newValue) {
        if (!Objects.equals(value, newValue)) {
            T oldValue = value;
            value = newValue;
            dirty = true;
            notifySubscribers();
            StateManager.onStateChange(this, oldValue, newValue);
        }
    }

    /**
     * Updates the state based on the current value.
     *
     * <p>Example:</p>
     * <pre>
     * count.update(c -> c + 1);  // Increment
     * list.update(l -> { l.add(item); return l; });  // Add to list
     * </pre>
     *
     * @param updater function that takes the current value and returns the new value
     */
    public void update(UnaryOperator<T> updater) {
        set(updater.apply(value));
    }

    /**
     * Gets the unique identifier for this state.
     * Used for serialization and WebSocket synchronization.
     *
     * @return the state ID
     */
    public String getId() {
        return id;
    }

    /**
     * Checks if this state has been modified since last render.
     *
     * @return true if the state has changed
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Clears the dirty flag. Called after rendering.
     */
    void clearDirty() {
        dirty = false;
    }

    /**
     * Subscribes to state changes.
     *
     * @param subscriber callback invoked when state changes
     * @return this State for chaining
     */
    public State<T> subscribe(Consumer<T> subscriber) {
        subscribers.add(subscriber);
        return this;
    }

    /**
     * Removes a subscriber.
     *
     * @param subscriber the subscriber to remove
     * @return this State for chaining
     */
    public State<T> unsubscribe(Consumer<T> subscriber) {
        subscribers.remove(subscriber);
        return this;
    }

    /**
     * Notifies all subscribers of the current value.
     */
    private void notifySubscribers() {
        for (Consumer<T> subscriber : subscribers) {
            subscriber.accept(value);
        }
    }

    /**
     * Returns a JSON-serializable representation of this state.
     *
     * @return JSON string of the state
     */
    public String toJson() {
        if (value == null) {
            return "{\"id\":\"" + id + "\",\"value\":null}";
        } else if (value instanceof String) {
            return "{\"id\":\"" + id + "\",\"value\":\"" + escapeJson((String) value) + "\"}";
        } else if (value instanceof Number || value instanceof Boolean) {
            return "{\"id\":\"" + id + "\",\"value\":" + value + "}";
        } else {
            // For complex objects, use toString() - can be enhanced with Jackson later
            return "{\"id\":\"" + id + "\",\"value\":\"" + escapeJson(value.toString()) + "\"}";
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String toString() {
        return "State[" + id + "=" + value + "]";
    }
}
