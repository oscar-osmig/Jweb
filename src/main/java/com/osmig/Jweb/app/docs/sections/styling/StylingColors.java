package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingColors {
    private StylingColors() {}

    public static Element render() {
        return section(
            h3Title("Colors"),
            para("Multiple color formats with full type safety."),
            codeBlock("""
// Hex colors
hex("#6366f1")           // 6-digit hex
hex("#f5f")              // 3-digit hex shorthand

// RGB/RGBA
rgb(99, 102, 241)        // RGB values 0-255
rgba(0, 0, 0, 0.5)       // RGBA with alpha 0-1

// HSL/HSLA
hsl(239, 84, 67)         // hue, saturation%, lightness%
hsla(239, 84, 67, 0.8)   // with alpha

// Named colors
white, black, red, blue, green, yellow,
gray, transparent, currentColor, inherit"""),

            h3Title("Using Colors"),
            codeBlock("""
.color(hex("#1e293b"))
.backgroundColor(rgba(255, 255, 255, 0.9))
.borderColor(hex("#e2e8f0"))

// Gradients
.background(linearGradient(deg(135),
    hex("#667eea"), hex("#764ba2")))

.background(radialGradient(circle,
    hex("#fff"), hex("#f0f0f0")))"""),

            h3Title("Creating a Color Palette"),
            codeBlock("""
public final class Colors {
    public static final CSSValue PRIMARY = hex("#6366f1");
    public static final CSSValue PRIMARY_DARK = hex("#4f46e5");
    public static final CSSValue SECONDARY = hex("#ec4899");
    public static final CSSValue SUCCESS = hex("#10b981");
    public static final CSSValue WARNING = hex("#f59e0b");
    public static final CSSValue ERROR = hex("#ef4444");
    public static final CSSValue TEXT = hex("#1e293b");
    public static final CSSValue TEXT_LIGHT = hex("#64748b");
    public static final CSSValue BG = hex("#ffffff");
    public static final CSSValue BG_MUTED = hex("#f8fafc");
}""")
        );
    }
}
