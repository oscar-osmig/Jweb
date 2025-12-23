package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingFlexbox {
    private StylingFlexbox() {}

    public static Element render() {
        return section(
            h3Title("Flexbox"),
            para("Flexible box layout for one-dimensional layouts."),
            codeBlock("""
// Basic flex container
.display(flex)
.flexDirection(row)      // row, column, rowReverse, columnReverse
.justifyContent(center)  // start, end, center, spaceBetween, spaceAround
.alignItems(center)      // start, end, center, stretch, baseline
.gap(rem(1))"""),

            h3Title("Common Flex Patterns"),
            codeBlock("""
// Center content horizontally and vertically
Style centered = style()
    .display(flex)
    .justifyContent(center)
    .alignItems(center)
    .minHeight(vh(100));

// Horizontal navigation
Style nav = style()
    .display(flex)
    .gap(rem(2))
    .alignItems(center);

// Space between (logo left, nav right)
Style header = style()
    .display(flex)
    .justifyContent(spaceBetween)
    .alignItems(center)
    .padding(rem(1), rem(2));

// Vertical stack
Style stack = style()
    .display(flex)
    .flexDirection(column)
    .gap(rem(1));"""),

            h3Title("Flex Item Properties"),
            codeBlock("""
// Flex item sizing
.flexGrow(1)       // grow to fill space
.flexShrink(0)     // don't shrink
.flexBasis(px(200)) // initial size
.flex(1)           // shorthand: grow shrink basis

// Self alignment
.alignSelf(flexEnd)""")
        );
    }
}
