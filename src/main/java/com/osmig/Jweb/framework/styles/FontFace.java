package com.osmig.Jweb.framework.styles;

/**
 * @deprecated Moved to {@link jweb.css.FontFace} — same class, shorter import.
 *             {@code fontFace(family)} returns {@code jweb.css.FontFace}; only the
 *             static factory still resolves through this name.
 */
@Deprecated
public class FontFace extends jweb.css.FontFace {

    protected FontFace(String fontFamily) {
        super(fontFamily);
    }
}
