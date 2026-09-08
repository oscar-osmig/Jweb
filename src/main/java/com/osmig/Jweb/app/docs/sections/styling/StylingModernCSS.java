package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingModernCSS {
    private StylingModernCSS() {}

    public static Element render() {
        return section(
            h3Title("Modern CSS Features"),
            para("Anchor positioning, scroll snap, text wrapping, subgrid, masking, and logical properties — each as typed builders."),

            h3Title("Anchor Positioning"),
            para("Position elements relative to an anchor element without JavaScript."),
            codeBlock("""
import static jweb.Css.*;

// Define an anchor
rule(".trigger").apply(anchorName("--tooltip-anchor"))

// Position relative to anchor using position-area
rule(".tooltip")
    .apply(positionAnchor("--tooltip-anchor"))
    .apply(positionArea("top"))
    .position(absolute)

// Using anchor() function for fine-grained control
rule(".popup")
    .top(anchor("--btn", "bottom"))
    .left(anchor("--btn", "left"))

// Fallback positioning (try top, then bottom)
rule(".dropdown")
    .apply(positionAnchor("--menu"))
    .apply(positionTryFallbacks("flip-block", "flip-inline"))

// Convenience: position above/below/left/right
rule(".tip")
    .apply(positionAnchor("--target"))
    .apply(positionAbove())    // shorthand for positionArea("top")
rule(".sub")
    .apply(positionAnchor("--target"))
    .apply(positionBelow())    // shorthand for positionArea("bottom")"""),

            h3Title("Scroll Snap"),
            para("Control scroll behavior to snap to specific elements."),
            codeBlock("""
import static jweb.Css.*;

// Horizontal carousel
rule(".carousel")
    .apply(snapTypeX("mandatory"))    // snap on X axis
    .apply(snapPadding("0 20px"))     // padding at snap edges
    .overflowX(auto)

rule(".carousel-item")
    .apply(snapAlignCenter())         // snap to center
    .apply(snapStopAlways())          // always stop at each item

// Vertical page snap
rule(".page-container")
    .apply(snapTypeY("proximity"))    // snap when close
    .overflowY(scroll)
    .height(vh(100))

rule(".page-section")
    .apply(snapAlignStart())
    .height(vh(100))

// Overscroll behavior
rule(".panel")
    .apply(overscrollBehaviorY("contain"))  // prevent parent scroll"""),

            h3Title("Text Wrapping"),
            para("Modern text wrapping and truncation controls."),
            codeBlock("""
import static jweb.Css.*;

// Balanced line lengths (great for headings)
rule("h1, h2").apply(textWrapBalance())

// Better orphan/widow handling (for body text)
rule("p").apply(textWrapPretty())

// Truncate to N lines with ellipsis
rule(".preview").apply(lineClamp(3))
// Shows max 3 lines, then "..."

// Prevent wrapping
rule(".nowrap").apply(textWrapNowrap())

// Word breaking
rule(".long-urls").apply(wordBreakAll())        // break anywhere
rule(".cjk").apply(wordBreakKeepAll())          // keep CJK together
rule(".overflow").apply(overflowWrapBreakWord()) // break on overflow

// Hyphenation
rule(".article").apply(hyphensAuto())

// Text overflow
rule(".ellipsis").apply(textOverflowEllipsis())"""),

            h3Title("Subgrid"),
            para("Inherit parent grid tracks in nested grids."),
            codeBlock("""
import static jweb.Css.*;

// Parent grid
rule(".card-grid")
    .display(grid)
    .gridTemplateColumns("repeat(3, 1fr)")
    .gap(rem(1))

// Child inherits parent columns
rule(".card")
    .display(grid)
    .apply(subgridColumns())    // grid-template-columns: subgrid

// Inherit both axes
rule(".full-child")
    .display(grid)
    .apply(subgridBoth())       // columns and rows from parent

// Named line references
rule(".named-child")
    .display(grid)
    .apply(subgridColumnsNamed("[start] [end]"))

// Grid placement helpers
rule(".span-all")
    .apply(gridColumnFull())    // grid-column: 1 / -1"""),

            h3Title("Masking and Clipping"),
            para("Apply masks and clip paths for creative shapes."),
            codeBlock("""
import static jweb.Css.*;

// Mask with image
rule(".masked").apply(maskImage("url(mask.svg)"))
               .apply(maskMode("alpha"))

// Clip to circle
rule(".avatar").apply(clipCircle("50%"))

// Clip to ellipse
rule(".oval").apply(clipEllipse("50%", "40%"))

// Clip to polygon
rule(".arrow").apply(clipPolygon(
    "50% 0%", "100% 100%", "0% 100%"))

// Preset shapes
rule(".diamond").apply(clipDiamond())
rule(".pentagon").apply(clipPentagon())
rule(".hexagon").apply(clipHexagon())
rule(".star").apply(clipStar())
rule(".tri-up").apply(clipTriangleUp())
rule(".tri-down").apply(clipTriangleDown())

// SVG path clipping
rule(".custom").apply(clipSvgPath("M0,0 L100,0 L50,100 Z"))"""),

            h3Title("Logical Properties"),
            para("Direction-aware properties that adapt to RTL/LTR and writing modes."),
            codeBlock("""
import static jweb.Css.*;

// Sizing (replaces width/height)
rule(".box")
    .apply(inlineSize("300px"))       // width in LTR
    .apply(blockSize("200px"))        // height in LTR
    .apply(maxInlineSize("100%"))

// Margin (replaces margin-left/right/top/bottom)
rule(".centered")
    .apply(marginInline("auto"))       // horizontal center
    .apply(marginBlock("1rem"))        // vertical margin

// Start/end for asymmetric spacing
rule(".indent")
    .apply(marginInlineStart("2rem"))  // left in LTR, right in RTL
    .apply(paddingInlineEnd("1rem"))

// Padding
rule(".card")
    .apply(paddingInline("1.5rem"))    // horizontal padding
    .apply(paddingBlock("1rem"))       // vertical padding

// Borders
rule(".highlighted")
    .apply(borderInlineStart("3px solid blue"))  // left border in LTR
    .apply(borderBlock("1px solid gray"))

// Inset (replaces top/right/bottom/left)
rule(".overlay")
    .apply(insetInline("0"))           // left: 0; right: 0
    .apply(insetBlock("0"))            // top: 0; bottom: 0

// Text alignment
rule(".start-aligned").apply(textAlignStart())  // left in LTR
rule(".end-aligned").apply(textAlignEnd())      // right in LTR"""),

            docTip("Anchor Positioning and Subgrid are newer CSS features. Check browser support on caniuse.com. " +
                   "Logical properties are well-supported and recommended for all new projects to enable RTL support.")
        );
    }
}
