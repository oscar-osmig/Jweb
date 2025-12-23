package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingUnits {
    private StylingUnits() {}

    public static Element render() {
        return section(
            h3Title("CSS Units"),
            para("Type-safe CSS units prevent invalid values."),
            codeBlock("""
// Absolute units
px(16)           // 16px - pixels
pt(12)           // 12pt - points

// Relative units
rem(1.5)         // 1.5rem - relative to root font size
em(2)            // 2em - relative to parent font size
percent(50)      // 50% - percentage

// Viewport units
vh(100)          // 100vh - viewport height
vw(50)           // 50vw - viewport width
vmin(10)         // 10vmin - smaller of vw/vh
vmax(10)         // 10vmax - larger of vw/vh

// Flex/Grid units
fr(1)            // 1fr - fractional unit
auto             // auto"""),

            h3Title("Using Units in Properties"),
            codeBlock("""
.width(px(300))
.height(vh(100))
.padding(rem(1), rem(2))           // top/bottom, left/right
.margin(px(10), px(20), px(10), px(20))  // top, right, bottom, left
.gap(rem(1))
.fontSize(rem(1.125))
.lineHeight(1.6)                   // unitless for line-height
.borderRadius(px(8))
.maxWidth(percent(100))""")
        );
    }
}
