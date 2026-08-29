package com.osmig.Jweb.framework.styles;

/**
 * Base fluent builder for CSS styles.
 *
 * @deprecated The Style DSL moved to {@link jweb.Style} for shorter imports:
 * {@code import jweb.Style;}. This class remains as a compatibility alias —
 * existing code keeps working unchanged.
 */
@Deprecated
public class Style<T extends Style<T>> extends jweb.Style<T> implements CSSValue {

    public Style() {}
}
