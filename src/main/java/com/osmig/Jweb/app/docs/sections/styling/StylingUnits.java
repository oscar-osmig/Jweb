package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingUnits {
    private StylingUnits() {}

    public static Element render() {
        return section(
            h3Title("CSS Units"),
            para("Type-safe unit functions prevent invalid CSS values."),

            codeBlock("""
// Absolute units
px(10)              // 10px - pixels
px(15.5)            // 15.5px - decimal pixels

// Relative units (recommended)
rem(1)              // 1rem - relative to root font-size
rem(1.5)            // 1.5rem
em(1.2)             // 1.2em - relative to parent font-size
percent(50)         // 50%
percent(33.33)      // 33.33%

// Viewport units
vh(100)             // 100vh - viewport height
vw(50)              // 50vw - viewport width
vmin(10)            // 10vmin - smaller of vw/vh
vmax(20)            // 20vmax - larger of vw/vh

// Dynamic viewport (mobile-safe)
dvh(100)            // 100dvh - accounts for mobile browser chrome
dvw(100)            // 100dvw

// Grid units
fr(1)               // 1fr - fraction of available space
fr(2)               // 2fr - 2 fractions

// Time (for animations)
ms(300)             // 300ms - milliseconds
s(0.3)              // 0.3s - seconds

// Angles (for transforms)
deg(45)             // 45deg - degrees
turn(0.5)           // 0.5turn - half rotation"""),

            h3Title("CSS Functions"),
            para("Mathematical functions for dynamic values."),
            codeBlock("""
// calc() - calculations
.width(calc("100% - 200px"))
.height(calc("100vh - 60px"))

// min() - smallest value
.width(min(px(400), percent(100)))

// max() - largest value
.width(max(px(300), percent(50)))

// clamp() - value within range
.fontSize(clamp(rem(1), vw(4), rem(2)))
// Font scales between 1rem and 2rem based on viewport

// Example: responsive container
.maxWidth(min(px(1200), calc("100% - 2rem")))
.padding(clamp(rem(1), vw(5), rem(3)))"""),

            h3Title("Common Constants"),
            para("Pre-defined values for common CSS keywords."),
            codeBlock("""
zero                // 0
auto                // auto
inherit             // inherit
initial             // initial
unset               // unset
none                // none

// Usage
.margin(zero)
.width(auto)
.display(none)""")
        );
    }
}
