package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingAdvanced {
    private StylingAdvanced() {}

    public static Element render() {
        return section(
            h3Title("Borders & Shadows"),
            codeBlock("""
// Borders
.border(px(1), solid, hex("#e2e8f0"))
.borderTop(px(2), solid, hex("#6366f1"))
.borderRadius(px(8))
.borderRadius(px(8), px(8), px(0), px(0))  // top corners only

// Box shadow
.boxShadow(px(0), px(2), px(4), rgba(0, 0, 0, 0.1))
.boxShadow(px(0), px(4), px(6), px(-1), rgba(0, 0, 0, 0.1))

// Multiple shadows
.boxShadow(
    shadow(px(0), px(1), px(2), rgba(0,0,0,0.05)),
    shadow(px(0), px(4), px(6), rgba(0,0,0,0.1))
)"""),

            h3Title("Transforms & Transitions"),
            codeBlock("""
// Transforms
.transform(translateY(px(-2)))
.transform(scale(1.05))
.transform(rotate(deg(45)))

// Transitions
.transition("all 0.2s ease")
.transition("transform 0.15s ease-in-out")
.transitionProperty("background-color, border-color")
.transitionDuration("0.2s")
.transitionTimingFunction("ease-out")"""),

            h3Title("Positioning"),
            codeBlock("""
// Position types
.position(relative)
.position(absolute)
.position(fixed)
.position(sticky)

// Offsets
.top(px(0))
.right(px(0))
.bottom(px(0))
.left(px(0))
.inset(px(0))  // all sides

// Z-index
.zIndex(10)
.zIndex(9999)""")
        );
    }
}
