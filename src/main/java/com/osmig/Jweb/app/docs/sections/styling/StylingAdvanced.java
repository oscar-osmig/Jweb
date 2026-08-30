package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
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

// Multiple shadows (string form)
.boxShadow("0 1px 2px rgba(0,0,0,0.05), 0 4px 6px rgba(0,0,0,0.1)")"""),

            h3Title("Transforms & Transitions"),
            codeBlock("""
// Transforms
.transform(translateY(px(-2)))
.transform(scale(1.05))
.transform(rotate(deg(45)))

// Transitions
.transition(all, s(0.2), ease)
.transition(propTransform, s(0.15), easeInOut)
.transitionProperty(propBackgroundColor)
.transitionDuration(s(0.2))
.transitionTimingFunction(easeOut)"""),

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
