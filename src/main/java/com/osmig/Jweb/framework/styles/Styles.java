package com.osmig.Jweb.framework.styles;

/**
 * Static factory for creating Style builders.
 * Import this class statically for clean usage.
 *
 * Usage:
 *   import static com.osmig.Jweb.framework.styles.Styles.*;
 *   import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 *   import static com.osmig.Jweb.framework.styles.CSSColors.*;
 *   import static com.osmig.Jweb.framework.styles.CSS.*;
 *
 *   div(attrs().style(
 *       style()
 *           .display(flex)
 *           .padding(rem(1))
 *           .background(hex("#f5f5f5"))
 *           .border(px(1), solid, gray)
 *   ))
 *
 * @deprecated Replaced by {@code jweb.css.Styles} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class Styles {

    protected Styles() {}

    /**
     * Creates a new Style builder.
     */
    public static jweb.Style<?> style() {
        return new Style<>();
    }
}
