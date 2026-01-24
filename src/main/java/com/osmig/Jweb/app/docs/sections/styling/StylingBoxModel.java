package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingBoxModel {
    private StylingBoxModel() {}

    public static Element render() {
        return section(
            h3Title("Box Model"),
            para("Control dimensions, spacing, and borders."),

            codeBlock("""
// Dimensions
.width(px(300))
.height(px(200))
.minWidth(px(200))
.maxWidth(px(800))
.minHeight(px(100))
.maxHeight(px(600))

// Size shorthand
.size(px(100))                  // width & height
.size(px(200), px(100))         // width, height"""),

            h3Title("Margin"),
            para("External spacing around elements."),
            codeBlock("""
// All sides
.margin(rem(1))

// Vertical, horizontal
.margin(rem(1), rem(2))

// Top, horizontal, bottom
.margin(rem(1), rem(2), rem(1))

// Top, right, bottom, left
.margin(rem(1), rem(2), rem(1), rem(2))

// Individual sides
.marginTop(rem(1))
.marginRight(rem(2))
.marginBottom(rem(1))
.marginLeft(rem(2))

// Horizontal/vertical shortcuts
.marginX(rem(2))                // left & right
.marginY(rem(1))                // top & bottom

// Centering
.margin(zero, auto)             // Horizontal center"""),

            h3Title("Padding"),
            para("Internal spacing inside elements."),
            codeBlock("""
// All sides
.padding(rem(1))

// Vertical, horizontal
.padding(rem(1), rem(2))

// Individual sides
.paddingTop(rem(1))
.paddingRight(rem(2))
.paddingBottom(rem(1))
.paddingLeft(rem(2))

// Horizontal/vertical shortcuts
.paddingX(rem(2))               // left & right
.paddingY(rem(1))               // top & bottom"""),

            h3Title("Border"),
            para("Element borders with various styles."),
            codeBlock("""
// Full border
.border(px(1), solid, gray)

// Individual properties
.borderWidth(px(2))
.borderStyle(solid)
.borderColor(blue)

// Individual sides
.borderTop(px(1), solid, black)
.borderBottom(px(2), dashed, gray)
.borderLeft(px(1), solid, black)
.borderRight(px(1), solid, black)

// Border styles
solid, dashed, dotted, double, none

// Border radius
.borderRadius(px(8))
.borderRadius(px(8), px(4), px(8), px(4))  // corners
.borderTopLeftRadius(px(16))
.borderTopRightRadius(px(16))

// Presets
.roundedNone()      // 0
.roundedSm()        // 4px
.roundedMd()        // 6px
.roundedLg()        // 8px
.roundedXl()        // 12px
.roundedFull()      // 9999px (pill/circle)"""),

            h3Title("Box Sizing"),
            para("Control how dimensions are calculated."),
            codeBlock("""
// Include padding/border in width/height
.boxSizing(borderBox)

// Exclude padding/border (default)
.boxSizing(contentBox)

// Best practice: apply to all elements
rule("*").boxSizing(borderBox)"""),

            h3Title("Display"),
            para("Control element rendering."),
            codeBlock("""
.display(block)
.display(inline)
.display(inlineBlock)
.display(flex)
.display(grid)
.display(none)

// Visibility (keeps space)
.visibility(visible)
.visibility(hidden)

// Overflow
.overflow(hidden)
.overflow(auto)
.overflow(scroll)
.overflowX(auto)
.overflowY(scroll)""")
        );
    }
}
