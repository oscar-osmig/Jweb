package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsSVG {
    private ElementsSVG() {}

    public static Element render() {
        return section(
            h3Title("SVG Elements"),
            para("Create scalable vector graphics inline with type-safe methods."),

            codeBlock("""
// Import SVG elements
import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.elements.SVGElements.*;"""),

            h3Title("Basic SVG"),
            para("Create an SVG container with viewBox."),
            codeBlock("""
// SVG container
svg(viewBox("0 0 24 24"), width(24), height(24),
    path(d("M12 2L2 7l10 5 10-5-10-5z"), fill("currentColor"))
)

// Alternative viewBox syntax
svg(viewBox(0, 0, 100, 100),
    circle(cx(50), cy(50), r(40), fill("blue"))
)"""),

            h3Title("Shape Elements"),
            para("Basic shapes for building graphics."),
            codeBlock("""
// Circle
svg(viewBox("0 0 100 100"),
    circle(cx(50), cy(50), r(40),
        fill("blue"),
        stroke("black"),
        strokeWidth(2)
    )
)

// Rectangle
svg(viewBox("0 0 200 100"),
    rect(x(10), y(10), width(180), height(80),
        fill("red"),
        stroke("black"),
        strokeWidth(1)
    )
)

// Rounded rectangle
svg(viewBox("0 0 200 100"),
    rect(x(10), y(10), width(180), height(80),
        rx(10), ry(10),  // Corner radius
        fill("green")
    )
)

// Line
svg(viewBox("0 0 100 100"),
    line(x1(10), y1(10), x2(90), y2(90),
        stroke("black"),
        strokeWidth(2)
    )
)

// Polyline (connected lines, open)
svg(viewBox("0 0 100 100"),
    polyline(points("10,90 50,10 90,90"),
        fill("none"),
        stroke("purple"),
        strokeWidth(2)
    )
)

// Polygon (connected lines, closed)
svg(viewBox("0 0 100 100"),
    polygon(points("50,10 90,90 10,90"),
        fill("yellow"),
        stroke("black")
    )
)"""),

            h3Title("Path Element"),
            para("Complex shapes using path commands."),
            codeBlock("""
// Path with commands
svg(viewBox("0 0 24 24"),
    path(
        d("M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"),
        fill("none"),
        stroke("currentColor"),
        strokeWidth(2),
        strokeLinecap("round"),
        strokeLinejoin("round")
    )
)

// Path commands:
// M = moveto
// L = lineto
// H = horizontal lineto
// V = vertical lineto
// C = curveto (cubic bezier)
// S = smooth curveto
// Q = quadratic bezier
// T = smooth quadratic bezier
// A = arc
// Z = closepath

// Icon example (checkmark)
svg(viewBox("0 0 24 24"), width(24), height(24),
    path(
        d("M5 13l4 4L19 7"),
        fill("none"),
        stroke("green"),
        strokeWidth(2)
    )
)"""),

            h3Title("Grouping & Transforms"),
            para("Group elements and apply transformations."),
            codeBlock("""
// Group elements
svg(viewBox("0 0 200 200"),
    g(transform(translate(100, 100)),
        circle(cx(0), cy(0), r(50), fill("blue")),
        circle(cx(30), cy(30), r(20), fill("red"))
    )
)

// Multiple transforms
g(transform(translate(50, 50), rotate(45), scale(1.5)),
    rect(x(-25), y(-25), width(50), height(50), fill("purple"))
)

// Transform functions
translate(100, 50)     // Move position
rotate(45)             // Rotate degrees
scale(1.5)             // Uniform scale
scale(2, 0.5)          // Non-uniform scale
skewX(10)              // Skew horizontally
skewY(10)              // Skew vertically"""),

            h3Title("Definitions & Reuse"),
            para("Define reusable elements and gradients."),
            codeBlock("""
svg(viewBox("0 0 200 200"),
    // Define reusable elements
    defs(
        // Gradient
        linearGradient(attrs().id("gradient1"),
            stop(attrs().offset("0%").stopColor("blue")),
            stop(attrs().offset("100%").stopColor("purple"))
        ),

        // Reusable symbol
        symbol(attrs().id("star").viewBox("0 0 24 24"),
            path(d("M12 2l3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z"))
        )
    ),

    // Use gradient
    rect(x(10), y(10), width(80), height(80),
        fill("url(#gradient1)")
    ),

    // Reuse symbol
    use(href("#star"), x(100), y(100), width(50), height(50))
)"""),

            h3Title("Icon Example"),
            para("Create a complete icon."),
            codeBlock("""
// Reusable icon method
public static Element icon(String name) {
    return switch (name) {
        case "check" -> svg(viewBox("0 0 24 24"), width(24), height(24),
            path(d("M5 13l4 4L19 7"),
                fill("none"), stroke("currentColor"), strokeWidth(2))
        );
        case "x" -> svg(viewBox("0 0 24 24"), width(24), height(24),
            path(d("M6 6l12 12M6 18L18 6"),
                fill("none"), stroke("currentColor"), strokeWidth(2))
        );
        case "menu" -> svg(viewBox("0 0 24 24"), width(24), height(24),
            path(d("M3 12h18M3 6h18M3 18h18"),
                fill("none"), stroke("currentColor"), strokeWidth(2))
        );
        default -> fragment();
    };
}

// Usage
button(class_("icon-btn"),
    icon("check"),
    span("Confirm")
)"""),

            docTip("SVG elements inherit color from CSS using currentColor. Set the parent's color property to style icons.")
        );
    }
}
