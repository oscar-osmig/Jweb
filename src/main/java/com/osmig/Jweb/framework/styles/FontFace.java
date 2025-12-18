package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for @font-face CSS declarations.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.FontFace.*;
 *
 * String css = fontFace("CustomFont")
 *     .src("/fonts/custom.woff2", woff2)
 *     .src("/fonts/custom.woff", woff)
 *     .fontWeight(400)
 *     .fontStyle(normal)
 *     .fontDisplay(swap)
 *     .build();
 *
 * // Multiple weights
 * String boldCss = fontFace("CustomFont")
 *     .src("/fonts/custom-bold.woff2", woff2)
 *     .fontWeight(700)
 *     .build();
 * </pre>
 */
public class FontFace {

    // Font format constants
    public static final String woff2 = "woff2";
    public static final String woff = "woff";
    public static final String truetype = "truetype";
    public static final String opentype = "opentype";
    public static final String embeddedOpentype = "embedded-opentype";
    public static final String svg = "svg";

    // Font display constants
    public static final String auto = "auto";
    public static final String block = "block";
    public static final String swap = "swap";
    public static final String fallback = "fallback";
    public static final String optional = "optional";

    // Font style constants
    public static final String normal = "normal";
    public static final String italic = "italic";
    public static final String oblique = "oblique";

    private final String fontFamily;
    private final List<String> sources = new ArrayList<>();
    private Integer fontWeight;
    private Integer fontWeightMax;  // For variable fonts
    private String fontStyle = normal;
    private String fontDisplay = swap;
    private String unicodeRange;
    private String fontStretch;

    private FontFace(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    /**
     * Creates a new font-face builder.
     *
     * @param fontFamily the font family name
     */
    public static FontFace fontFace(String fontFamily) {
        return new FontFace(fontFamily);
    }

    /**
     * Adds a font source with format.
     *
     * @param url the URL to the font file
     * @param format the font format (woff2, woff, truetype, etc.)
     * @return this for chaining
     */
    public FontFace src(String url, String format) {
        sources.add("url('" + url + "') format('" + format + "')");
        return this;
    }

    /**
     * Adds a font source without format (browser auto-detects).
     *
     * @param url the URL to the font file
     * @return this for chaining
     */
    public FontFace src(String url) {
        sources.add("url('" + url + "')");
        return this;
    }

    /**
     * Adds a local font source (checks system fonts first).
     *
     * @param localName the local font name
     * @return this for chaining
     */
    public FontFace local(String localName) {
        sources.add("local('" + localName + "')");
        return this;
    }

    /**
     * Sets the font weight.
     *
     * @param weight the weight (100-900)
     * @return this for chaining
     */
    public FontFace fontWeight(int weight) {
        this.fontWeight = weight;
        return this;
    }

    /**
     * Sets a font weight range (for variable fonts).
     *
     * @param min minimum weight
     * @param max maximum weight
     * @return this for chaining
     */
    public FontFace fontWeight(int min, int max) {
        this.fontWeight = min;
        this.fontWeightMax = max;
        return this;
    }

    /**
     * Sets the font style.
     *
     * @param style normal, italic, or oblique
     * @return this for chaining
     */
    public FontFace fontStyle(String style) {
        this.fontStyle = style;
        return this;
    }

    /**
     * Sets the font-display strategy.
     *
     * @param display auto, block, swap, fallback, or optional
     * @return this for chaining
     */
    public FontFace fontDisplay(String display) {
        this.fontDisplay = display;
        return this;
    }

    /**
     * Sets the unicode range this font covers.
     *
     * @param range unicode range (e.g., "U+0000-00FF", "U+0100-024F")
     * @return this for chaining
     */
    public FontFace unicodeRange(String range) {
        this.unicodeRange = range;
        return this;
    }

    /**
     * Sets the font stretch.
     *
     * @param stretch e.g., "condensed", "expanded", "50% 200%"
     * @return this for chaining
     */
    public FontFace fontStretch(String stretch) {
        this.fontStretch = stretch;
        return this;
    }

    /**
     * Builds the @font-face CSS declaration.
     *
     * @return CSS string
     */
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("@font-face {\n");
        sb.append("  font-family: '").append(fontFamily).append("';\n");

        if (!sources.isEmpty()) {
            sb.append("  src: ").append(String.join(",\n       ", sources)).append(";\n");
        }

        if (fontWeight != null) {
            if (fontWeightMax != null) {
                sb.append("  font-weight: ").append(fontWeight).append(" ").append(fontWeightMax).append(";\n");
            } else {
                sb.append("  font-weight: ").append(fontWeight).append(";\n");
            }
        }

        sb.append("  font-style: ").append(fontStyle).append(";\n");
        sb.append("  font-display: ").append(fontDisplay).append(";\n");

        if (unicodeRange != null) {
            sb.append("  unicode-range: ").append(unicodeRange).append(";\n");
        }

        if (fontStretch != null) {
            sb.append("  font-stretch: ").append(fontStretch).append(";\n");
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
