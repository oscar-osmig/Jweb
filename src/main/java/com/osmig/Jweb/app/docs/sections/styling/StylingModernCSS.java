package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingModernCSS {
    private StylingModernCSS() {}

    public static Element render() {
        return section(
            h3Title("Modern CSS Features"),
            para("Cutting-edge CSS features for positioning, scrolling, text, grids, masking, and logical layout."),

            h3Title("Anchor Positioning"),
            para("Position elements relative to an anchor element without JavaScript."),
            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSAnchorPositioning.*;

// Define an anchor
rule(".trigger").prop(anchorName("--tooltip-anchor"))

// Position relative to anchor using position-area
rule(".tooltip")
    .prop(positionAnchor("--tooltip-anchor"))
    .prop(positionArea("top"))
    .position(absolute)

// Using anchor() function for fine-grained control
rule(".popup")
    .prop(top(anchor("--btn", "bottom")))
    .prop(left(anchor("--btn", "left")))

// Fallback positioning (try top, then bottom)
rule(".dropdown")
    .prop(positionAnchor("--menu"))
    .prop(positionTryFallbacks("flip-block", "flip-inline"))

// Convenience: position above/below/left/right
rule(".tip")
    .prop(positionAnchor("--target"))
    .prop(positionAbove())    // shorthand for positionArea("top")
rule(".sub")
    .prop(positionAnchor("--target"))
    .prop(positionBelow())    // shorthand for positionArea("bottom")"""),

            h3Title("Scroll Snap"),
            para("Control scroll behavior to snap to specific elements."),
            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSScrollSnap.*;

// Horizontal carousel
rule(".carousel")
    .prop(snapTypeX("mandatory"))    // snap on X axis
    .prop(snapPadding("0 20px"))     // padding at snap edges
    .overflowX(auto)

rule(".carousel-item")
    .prop(snapAlignCenter())         // snap to center
    .prop(snapStopAlways())          // always stop at each item

// Vertical page snap
rule(".page-container")
    .prop(snapTypeY("proximity"))    // snap when close
    .overflowY(scroll)
    .height("100vh")

rule(".page-section")
    .prop(snapAlignStart())
    .height("100vh")

// Overscroll behavior
rule(".panel")
    .prop(overscrollBehaviorY("contain"))  // prevent parent scroll"""),

            h3Title("Text Wrapping"),
            para("Modern text wrapping and truncation controls."),
            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSTextWrap.*;

// Balanced line lengths (great for headings)
rule("h1, h2").prop(textWrapBalance())

// Better orphan/widow handling (for body text)
rule("p").prop(textWrapPretty())

// Truncate to N lines with ellipsis
rule(".preview").prop(lineClamp(3))
// Shows max 3 lines, then "..."

// Prevent wrapping
rule(".nowrap").prop(textWrapNowrap())

// Word breaking
rule(".long-urls").prop(wordBreakAll())        // break anywhere
rule(".cjk").prop(wordBreakKeepAll())          // keep CJK together
rule(".overflow").prop(overflowWrapBreakWord()) // break on overflow

// Hyphenation
rule(".article").prop(hyphensAuto())

// Text overflow
rule(".ellipsis").prop(textOverflowEllipsis())"""),

            h3Title("Subgrid"),
            para("Inherit parent grid tracks in nested grids."),
            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSSubgrid.*;

// Parent grid
rule(".card-grid")
    .display("grid")
    .prop(gridTemplateColumns("repeat(3, 1fr)"))
    .gap(rem(1))

// Child inherits parent columns
rule(".card")
    .display("grid")
    .prop(subgridColumns())    // grid-template-columns: subgrid

// Inherit both axes
rule(".full-child")
    .display("grid")
    .prop(subgridBoth())       // columns and rows from parent

// Named line references
rule(".named-child")
    .display("grid")
    .prop(subgridColumnsNamed("[start] [end]"))

// Grid placement helpers
rule(".span-all")
    .prop(gridColumnFull())    // grid-column: 1 / -1"""),

            h3Title("Masking and Clipping"),
            para("Apply masks and clip paths for creative shapes."),
            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSMasking.*;

// Mask with image
rule(".masked").prop(maskImage("url(mask.svg)"))
               .prop(maskMode("alpha"))

// Clip to circle
rule(".avatar").prop(clipCircle("50%"))

// Clip to ellipse
rule(".oval").prop(clipEllipse("50%", "40%"))

// Clip to polygon
rule(".arrow").prop(clipPolygon(
    "50% 0%", "100% 100%", "0% 100%"))

// Preset shapes
rule(".diamond").prop(clipDiamond())
rule(".pentagon").prop(clipPentagon())
rule(".hexagon").prop(clipHexagon())
rule(".star").prop(clipStar())
rule(".tri-up").prop(clipTriangleUp())
rule(".tri-down").prop(clipTriangleDown())

// SVG path clipping
rule(".custom").prop(clipSvgPath("M0,0 L100,0 L50,100 Z"))"""),

            h3Title("Logical Properties"),
            para("Direction-aware properties that adapt to RTL/LTR and writing modes."),
            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSLogicalProperties.*;

// Sizing (replaces width/height)
rule(".box")
    .prop(inlineSize("300px"))       // width in LTR
    .prop(blockSize("200px"))        // height in LTR
    .prop(maxInlineSize("100%"))

// Margin (replaces margin-left/right/top/bottom)
rule(".centered")
    .prop(marginInline("auto"))       // horizontal center
    .prop(marginBlock("1rem"))        // vertical margin

// Start/end for asymmetric spacing
rule(".indent")
    .prop(marginInlineStart("2rem"))  // left in LTR, right in RTL
    .prop(paddingInlineEnd("1rem"))

// Padding
rule(".card")
    .prop(paddingInline("1.5rem"))    // horizontal padding
    .prop(paddingBlock("1rem"))       // vertical padding

// Borders
rule(".highlighted")
    .prop(borderInlineStart("3px solid blue"))  // left border in LTR
    .prop(borderBlock("1px solid gray"))

// Inset (replaces top/right/bottom/left)
rule(".overlay")
    .prop(insetInline("0"))           // left: 0; right: 0
    .prop(insetBlock("0"))            // top: 0; bottom: 0

// Text alignment
rule(".start-aligned").prop(textAlignStart())  // left in LTR
rule(".end-aligned").prop(textAlignEnd())      // right in LTR"""),

            docTip("Anchor Positioning and Subgrid are newer CSS features. Check browser support on caniuse.com. " +
                   "Logical properties are well-supported and recommended for all new projects to enable RTL support.")
        );
    }
}
