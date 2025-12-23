package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingTypography {
    private StylingTypography() {}

    public static Element render() {
        return section(
            h3Title("Typography"),
            para("Font and text styling properties."),
            codeBlock("""
// Font family
.fontFamily("Inter, system-ui, sans-serif")
.fontFamily("'Fira Code', monospace")

// Font size
.fontSize(rem(1.125))
.fontSize(px(18))

// Font weight
.fontWeight(400)     // normal
.fontWeight(600)     // semi-bold
.fontWeight(700)     // bold

// Line height
.lineHeight(1.6)     // unitless multiplier
.lineHeight(px(24))  // explicit value

// Letter spacing
.letterSpacing(px(-0.5))
.letterSpacing(em(0.05))"""),

            h3Title("Text Properties"),
            codeBlock("""
// Text alignment
.textAlign(center)   // left, center, right, justify

// Text decoration
.textDecoration(none)
.textDecoration(underline)

// Text transform
.textTransform(uppercase)
.textTransform(capitalize)

// Text overflow
.overflow(hidden)
.textOverflow(ellipsis)
.whiteSpace(nowrap)

// Word wrapping
.wordBreak(breakWord)
.overflowWrap(breakWord)"""),

            h3Title("Font Presets"),
            codeBlock("""
public final class Typography {
    public static final CSSValue TEXT_XS = rem(0.75);
    public static final CSSValue TEXT_SM = rem(0.875);
    public static final CSSValue TEXT_BASE = rem(1);
    public static final CSSValue TEXT_LG = rem(1.125);
    public static final CSSValue TEXT_XL = rem(1.25);
    public static final CSSValue TEXT_2XL = rem(1.5);
    public static final CSSValue TEXT_3XL = rem(1.875);
}""")
        );
    }
}
