package com.osmig.Jweb.app.docs.sections.elements;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsSVG {
    private ElementsSVG() {}

    public static Element render() {
        return section(
            h3Title("SVG Elements"),
            para("Create scalable vector graphics inline with type-safe methods."),

            codeBlock("""
// Import SVG elements
import static jweb.El.*;
import static jweb.el.SVGElements.*;

// Three elements are renamed to avoid clashing with the CSS DSL under these
// wildcard imports: svgText (the SVG <text> — plain text is a text node),
// svgLinearGradient and svgRadialGradient (the plain names are CSS values)."""),

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

// Multiple transforms (space-separated string)
g(transform(translate(50, 50) + " " + rotate(45) + " " + scale(1.5)),
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
            para("Define reusable elements and gradients. Gradients are svgLinearGradient / " +
                 "svgRadialGradient here — linearGradient and radialGradient are the CSS values."),
            codeBlock("""
svg(viewBox("0 0 200 200"),
    // Define reusable elements
    defs(
        // Linear gradient
        svgLinearGradient(id("gradient1"),
            stop(offset("0%"), stopColor("blue")),
            stop(offset("100%"), stopColor("purple"))
        ),

        // Radial gradient
        svgRadialGradient(id("glow"),
            stop(offset("0%"), stopColor("white")),
            stop(offset("100%"), stopColor("blue"))
        ),

        // Reusable symbol
        symbol(id("star"), viewBox("0 0 24 24"),
            path(d("M12 2l3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z"))
        )
    ),

    // Use gradient
    rect(x(10), y(10), width(80), height(80),
        fill("url(#gradient1)")
    ),
    circle(cx(150), cy(50), r(30), fill("url(#glow)")),

    // Reuse symbol
    use(href("#star"), x(100), y(100), width(50), height(50))
)"""),

            h3Title("Text, Patterns & Filters"),
            para("Text on the SVG canvas, tiled patterns, and filter effects. svgText is the " +
                 "SVG <text> element — plain text() is gone, and a bare String is still a text node."),
            codeBlock("""
// Text with a tspan run and text along a path
svg(viewBox("0 0 200 60"),
    defs(
        path(id("curve"), d("M10 50 Q100 10 190 50"))
    ),
    svgText(x(10), y(20), fontFamily("system-ui"), fontSize(16),
        "Plain, ", tspan(fill("red"), "colored"), " text"
    ),
    svgText(
        textPath(href("#curve"), "Text that follows a curve")
    )
)

// Tiled pattern fill
svg(viewBox("0 0 100 100"),
    defs(
        pattern(id("dots"), width(10), height(10),
            attr("patternUnits", "userSpaceOnUse"),
            circle(cx(5), cy(5), r(2), fill("gray"))
        )
    ),
    rect(x(0), y(0), width(100), height(100), fill("url(#dots)"))
)

// Blur and drop-shadow filters
svg(viewBox("0 0 100 100"),
    defs(
        filter(id("blur"),
            feGaussianBlur(attr("in", "SourceGraphic"), stdDeviation(3))),
        filter(id("shadow"),
            feDropShadow(attr("dx", "2"), attr("dy", "2"), stdDeviation(2)))
    ),
    circle(cx(30), cy(50), r(20), fill("blue"), filterRef("blur")),
    circle(cx(70), cy(50), r(20), fill("green"), filterRef("shadow"))
)

// Clip and mask
svg(viewBox("0 0 100 100"),
    defs(
        clipPath(id("circle-clip"), circle(cx(50), cy(50), r(30)))
    ),
    image(href("/photo.jpg"), x(0), y(0), width(100), height(100),
        clipPathRef("circle-clip"))
)"""),

            h3Title("Animation"),
            para("Native SMIL animation: animate for values, animateTransform for transforms, " +
                 "animateMotion for movement along a path."),
            codeBlock("""
// Fade in
circle(cx(50), cy(50), r(20), fill("blue"),
    animate(attributeName("opacity"), from("0"), to("1"), dur("1s"))
)

// Spin forever
rect(x(40), y(40), width(20), height(20), fill("red"),
    animateTransform(attributeName("transform"), type("rotate"),
        from("0 50 50"), to("360 50 50"), dur("2s"), repeatCount("indefinite"))
)

// Move along a path
svg(viewBox("0 0 200 100"),
    circle(r(6), fill("purple"),
        animateMotion(attr("path", "M10 90 Q100 10 190 90"),
            dur("3s"), repeatCount("indefinite")))
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
