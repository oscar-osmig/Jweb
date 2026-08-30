package com.osmig.Jweb.framework.styles;

/**
 * One CSS rule — a selector plus the style to apply to it.
 *
 * <p>This is the single rule shape every at-rule builder accepts, so the same
 * literal works inside {@code media()}, {@code supports()} and
 * {@code container()}:</p>
 *
 * <pre>{@code
 * import static com.osmig.Jweb.framework.styles.Rule.of;
 *
 * media().minWidth(px(768)).rules(
 *     of(".container", style().maxWidth(px(720))),
 *     of(".sidebar",   style().display("block"))
 * );
 *
 * supports("display", "grid").rules(
 *     of(".grid", style().display("grid"))
 * );
 * }</pre>
 *
 * <p>Note there is deliberately no top-level {@code rule(...)} factory here:
 * {@code CSS.rule(String)} already owns that name under a wildcard import.
 * Use {@link #of(String, jweb.Style)} instead.</p>
 *
 * @param selector the CSS selector (e.g. {@code ".card"}, {@code "h1"})
 * @param style the style to apply
 */
public record Rule(String selector, jweb.Style<?> style) {

    /**
     * Creates a rule.
     *
     * @param selector the CSS selector
     * @param style the style to apply
     * @return a new Rule
     */
    public static Rule of(String selector, jweb.Style<?> style) {
        return new Rule(selector, style);
    }
}
