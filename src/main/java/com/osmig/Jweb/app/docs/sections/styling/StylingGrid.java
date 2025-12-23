package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingGrid {
    private StylingGrid() {}

    public static Element render() {
        return section(
            h3Title("CSS Grid"),
            para("Two-dimensional grid layout system."),
            codeBlock("""
// Basic grid
.display(grid)
.gridTemplateColumns(repeat(3, fr(1)))  // 3 equal columns
.gridTemplateRows(auto)
.gap(rem(2))"""),

            h3Title("Grid Column Patterns"),
            codeBlock("""
// Fixed columns
.gridTemplateColumns(px(200), px(200), px(200))

// Flexible columns
.gridTemplateColumns(fr(1), fr(2), fr(1))  // 1:2:1 ratio

// Auto-fit responsive grid
.gridTemplateColumns(repeat(autoFit, minmax(px(250), fr(1))))

// Mixed
.gridTemplateColumns(px(250), fr(1), px(300))  // sidebar, content, aside"""),

            h3Title("Grid Areas"),
            codeBlock("""
// Define areas
.gridTemplateAreas(
    "header header header",
    "sidebar content aside",
    "footer footer footer"
)
.gridTemplateRows(auto, fr(1), auto)
.gridTemplateColumns(px(200), fr(1), px(200))

// Place items in areas
.gridArea("header")
.gridArea("sidebar")
.gridArea("content")"""),

            h3Title("Grid Item Placement"),
            codeBlock("""
// Span columns/rows
.gridColumn("span 2")
.gridRow("span 3")

// Explicit placement
.gridColumnStart(1)
.gridColumnEnd(3)
.gridRowStart(1)
.gridRowEnd(2)""")
        );
    }
}
