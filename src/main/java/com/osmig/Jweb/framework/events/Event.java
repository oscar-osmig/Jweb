package com.osmig.Jweb.framework.events;

import java.util.Map;

/**
 * Represents a browser event sent to the server.
 *
 * <p>This interface provides access to event data when handling
 * onClick, onChange, onSubmit, and other DOM events.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * button(attrs().onClick(e -> {
 *     System.out.println("Clicked element: " + e.targetId());
 * }), text("Click me"))
 *
 * input(attrs().onChange(e -> {
 *     name.set(e.value());  // Get input value
 * }))
 *
 * form(attrs().onSubmit(e -> {
 *     e.preventDefault();   // Prevent form submission
 *     submitData();
 * }), ...)
 * </pre>
 */
public interface Event {

    /**
     * Prevents the default browser action for this event.
     * For forms, prevents submission. For links, prevents navigation.
     */
    void preventDefault();

    /**
     * Stops the event from propagating to parent elements.
     */
    void stopPropagation();

    /**
     * Gets the value of the target element (for input, select, textarea).
     *
     * @return the element value, or empty string if not applicable
     */
    String value();

    /**
     * Gets the ID of the element that triggered the event.
     *
     * @return the element ID
     */
    String targetId();

    /**
     * Gets the type of event (click, change, submit, etc.).
     *
     * @return the event type
     */
    String type();

    /**
     * Gets the key that was pressed (for keyboard events).
     *
     * @return the key name, or null if not a keyboard event
     */
    String key();

    /**
     * Gets the key code (for keyboard events).
     *
     * @return the key code, or -1 if not a keyboard event
     */
    int keyCode();

    /**
     * Checks if the Ctrl key was held during the event.
     *
     * @return true if Ctrl was pressed
     */
    boolean ctrlKey();

    /**
     * Checks if the Shift key was held during the event.
     *
     * @return true if Shift was pressed
     */
    boolean shiftKey();

    /**
     * Checks if the Alt key was held during the event.
     *
     * @return true if Alt was pressed
     */
    boolean altKey();

    /**
     * Checks if the Meta key (Cmd on Mac) was held during the event.
     *
     * @return true if Meta was pressed
     */
    boolean metaKey();

    /**
     * Gets the X coordinate of the mouse/touch (for pointer events).
     *
     * @return the X coordinate, or -1 if not a pointer event
     */
    int clientX();

    /**
     * Gets the Y coordinate of the mouse/touch (for pointer events).
     *
     * @return the Y coordinate, or -1 if not a pointer event
     */
    int clientY();

    /**
     * Gets the checked state (for checkbox/radio inputs).
     *
     * @return true if checked
     */
    boolean checked();

    /**
     * Gets form data (for submit events).
     *
     * @return map of field names to values
     */
    Map<String, String> formData();

    /**
     * Gets a custom data attribute from the target element.
     *
     * @param name the data attribute name (without "data-" prefix)
     * @return the attribute value, or null if not present
     */
    String data(String name);

    /**
     * Gets all custom data attributes from the target element.
     *
     * @return map of data attribute names to values
     */
    Map<String, String> dataset();
}
