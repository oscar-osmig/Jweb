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

Cls TRIGGER = cls("trigger");
Cls TOOLTIP = cls("tooltip");
Cls POPUP = cls("popup");
Cls DROPDOWN = cls("dropdown");
Cls TIP = cls("tip");
Cls SUB = cls("sub");

// Define an anchor
rule(TRIGGER).apply(anchorName("--tooltip-anchor"))

// Position relative to anchor using position-area
rule(TOOLTIP)
    .apply(positionAnchor("--tooltip-anchor"))
    .apply(positionArea("top"))
    .position(absolute)

// Using anchor() function for fine-grained control
rule(POPUP)
    .top(anchor("--btn", "bottom"))
    .left(anchor("--btn", "left"))

// Fallback positioning (try top, then bottom)
rule(DROPDOWN)
    .apply(positionAnchor("--menu"))
    .apply(positionTryFallbacks("flip-block", "flip-inline"))

// Convenience: position above/below/left/right
rule(TIP)
    .apply(positionAnchor("--target"))
    .apply(positionAbove())    // shorthand for positionArea("top")
rule(SUB)
    .apply(positionAnchor("--target"))
    .apply(positionBelow())    // shorthand for positionArea("bottom")"""),

            h3Title("Scroll Snap"),
            para("Control scroll behavior to snap to specific elements."),
            codeBlock("""
import static jweb.Css.*;

Cls CAROUSEL = cls("carousel");
Cls CAROUSEL_ITEM = cls("carousel-item");
Cls PAGE_CONTAINER = cls("page-container");
Cls PAGE_SECTION = cls("page-section");
Cls PANEL = cls("panel");

// Horizontal carousel
rule(CAROUSEL)
    .apply(snapTypeX("mandatory"))    // snap on X axis
    .apply(snapPadding("0 20px"))     // padding at snap edges
    .overflowX(auto)

rule(CAROUSEL_ITEM)
    .apply(snapAlignCenter())         // snap to center
    .apply(snapStopAlways())          // always stop at each item

// Vertical page snap
rule(PAGE_CONTAINER)
    .apply(snapTypeY("proximity"))    // snap when close
    .overflowY(scroll)
    .height(vh(100))

rule(PAGE_SECTION)
    .apply(snapAlignStart())
    .height(vh(100))

// Overscroll behavior
rule(PANEL)
    .apply(overscrollBehaviorY("contain"))  // prevent parent scroll"""),

            h3Title("Text Wrapping"),
            para("Modern text wrapping and truncation controls."),
            codeBlock("""
import static jweb.Css.*;

Cls PREVIEW = cls("preview");
Cls NOWRAP = cls("nowrap");
Cls LONG_URLS = cls("long-urls");
Cls CJK = cls("cjk");
Cls OVERFLOW = cls("overflow");
Cls ARTICLE = cls("article");
Cls ELLIPSIS = cls("ellipsis");

// Balanced line lengths (great for headings)
rule(Selector.type("h1").or(Selector.type("h2"))).apply(textWrapBalance())

// Better orphan/widow handling (for body text)
rule(Selector.type("p")).apply(textWrapPretty())

// Truncate to N lines with ellipsis
rule(PREVIEW).apply(lineClamp(3))
// Shows max 3 lines, then "..."

// Prevent wrapping
rule(NOWRAP).apply(textWrapNowrap())

// Word breaking
rule(LONG_URLS).apply(wordBreakAll())        // break anywhere
rule(CJK).apply(wordBreakKeepAll())          // keep CJK together
rule(OVERFLOW).apply(overflowWrapBreakWord()) // break on overflow

// Hyphenation
rule(ARTICLE).apply(hyphensAuto())

// Text overflow
rule(ELLIPSIS).apply(textOverflowEllipsis())"""),

            h3Title("Subgrid"),
            para("Inherit parent grid tracks in nested grids."),
            codeBlock("""
import static jweb.Css.*;

Cls CARD_GRID = cls("card-grid");
Cls CARD = cls("card");
Cls FULL_CHILD = cls("full-child");
Cls NAMED_CHILD = cls("named-child");
Cls SPAN_ALL = cls("span-all");

// Parent grid
rule(CARD_GRID)
    .display(grid)
    .gridTemplateColumns("repeat(3, 1fr)")
    .gap(rem(1))

// Child inherits parent columns
rule(CARD)
    .display(grid)
    .apply(subgridColumns())    // grid-template-columns: subgrid

// Inherit both axes
rule(FULL_CHILD)
    .display(grid)
    .apply(subgridBoth())       // columns and rows from parent

// Named line references
rule(NAMED_CHILD)
    .display(grid)
    .apply(subgridColumnsNamed("[start] [end]"))

// Grid placement helpers
rule(SPAN_ALL)
    .apply(gridColumnFull())    // grid-column: 1 / -1"""),

            h3Title("Masking and Clipping"),
            para("Apply masks and clip paths for creative shapes."),
            codeBlock("""
import static jweb.Css.*;

Cls MASKED = cls("masked");
Cls AVATAR = cls("avatar");
Cls OVAL = cls("oval");
Cls ARROW = cls("arrow");
Cls DIAMOND = cls("diamond");
Cls PENTAGON = cls("pentagon");
Cls HEXAGON = cls("hexagon");
Cls STAR = cls("star");
Cls TRI_UP = cls("tri-up");
Cls TRI_DOWN = cls("tri-down");
Cls CUSTOM = cls("custom");

// Mask with image
rule(MASKED).apply(maskImage("url(mask.svg)"))
               .apply(maskMode("alpha"))

// Clip to circle
rule(AVATAR).apply(clipCircle("50%"))

// Clip to ellipse
rule(OVAL).apply(clipEllipse("50%", "40%"))

// Clip to polygon
rule(ARROW).apply(clipPolygon(
    "50% 0%", "100% 100%", "0% 100%"))

// Preset shapes
rule(DIAMOND).apply(clipDiamond())
rule(PENTAGON).apply(clipPentagon())
rule(HEXAGON).apply(clipHexagon())
rule(STAR).apply(clipStar())
rule(TRI_UP).apply(clipTriangleUp())
rule(TRI_DOWN).apply(clipTriangleDown())

// SVG path clipping
rule(CUSTOM).apply(clipSvgPath("M0,0 L100,0 L50,100 Z"))"""),

            h3Title("Logical Properties"),
            para("Direction-aware properties that adapt to RTL/LTR and writing modes."),
            codeBlock("""
import static jweb.Css.*;

Cls BOX = cls("box");
Cls CENTERED = cls("centered");
Cls INDENT = cls("indent");
Cls CARD = cls("card");
Cls HIGHLIGHTED = cls("highlighted");
Cls OVERLAY = cls("overlay");
Cls START_ALIGNED = cls("start-aligned");
Cls END_ALIGNED = cls("end-aligned");

// Sizing (replaces width/height)
rule(BOX)
    .apply(inlineSize("300px"))       // width in LTR
    .apply(blockSize("200px"))        // height in LTR
    .apply(maxInlineSize("100%"))

// Margin (replaces margin-left/right/top/bottom)
rule(CENTERED)
    .apply(marginInline("auto"))       // horizontal center
    .apply(marginBlock("1rem"))        // vertical margin

// Start/end for asymmetric spacing
rule(INDENT)
    .apply(marginInlineStart("2rem"))  // left in LTR, right in RTL
    .apply(paddingInlineEnd("1rem"))

// Padding
rule(CARD)
    .apply(paddingInline("1.5rem"))    // horizontal padding
    .apply(paddingBlock("1rem"))       // vertical padding

// Borders
rule(HIGHLIGHTED)
    .apply(borderInlineStart("3px solid blue"))  // left border in LTR
    .apply(borderBlock("1px solid gray"))

// Inset (replaces top/right/bottom/left)
rule(OVERLAY)
    .apply(insetInline("0"))           // left: 0; right: 0
    .apply(insetBlock("0"))            // top: 0; bottom: 0

// Text alignment
rule(START_ALIGNED).apply(textAlignStart())  // left in LTR
rule(END_ALIGNED).apply(textAlignEnd())      // right in LTR"""),

            docTip("Anchor Positioning and Subgrid are newer CSS features. Check browser support on caniuse.com. " +
                   "Logical properties are well-supported and recommended for all new projects to enable RTL support.")
        );
    }
}
