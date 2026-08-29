package com.osmig.Jweb.framework.styles;

/**
 * Represents any CSS value (units, colors, keywords, variables).
 * All CSS values can be converted to their string representation.
 *
 * <p>In application code, prefer importing {@link jweb.CSSValue} — the short
 * alias for this interface. Every framework CSS value implements both.</p>
 */
@FunctionalInterface
public interface CSSValue extends jweb.CSSValue {
}
