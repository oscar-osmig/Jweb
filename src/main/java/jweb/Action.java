package jweb;

/**
 * Represents a buildable JavaScript action.
 *
 * <p>Actions can be used in two ways:</p>
 * <ol>
 *   <li>With ScriptBuilder for &lt;script&gt; blocks: {@code script().add(onClick("btn").does(show("panel")))}</li>
 *   <li>Inline with event attributes: {@code attrs().onClick(show("panel"))}</li>
 * </ol>
 */
@FunctionalInterface
public interface Action {
    /**
     * Builds the JavaScript code for this action.
     * @return the JavaScript code string
     */
    String build();

    /**
     * Returns the action as an inline JavaScript string for use in event attributes.
     * Can be used directly with onclick, onsubmit, etc.
     *
     * <p>Example:</p>
     * <pre>
     * // Using with attrs()
     * button(attrs().set("onclick", show("panel").inline()), "Show Panel")
     *
     * // Or use the convenience method
     * button(attrs().onClick(show("panel")), "Show Panel")
     * </pre>
     *
     * @return the JavaScript code string suitable for inline event handlers
     */
    default String inline() {
        return build();
    }
}
