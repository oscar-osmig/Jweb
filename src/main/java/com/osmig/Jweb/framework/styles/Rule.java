package com.osmig.Jweb.framework.styles;

/**
 * @deprecated Moved to {@link jweb.css.Rule} — a record, so this name cannot be
 *             a subtype: only the {@code of(...)} factory remains here and it
 *             returns {@code jweb.css.Rule}. Declare variables as {@code jweb.css.Rule}.
 */
@Deprecated
public final class Rule {

    private Rule() {}

    public static jweb.css.Rule of(String selector, jweb.Style<?> style) {
        return jweb.css.Rule.of(selector, style);
    }
}
