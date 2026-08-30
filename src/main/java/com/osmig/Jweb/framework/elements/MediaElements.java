package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * HTML media and embedded content elements: img, video, audio, canvas, svg, iframe.
 */
public final class MediaElements {
    private MediaElements() {}

    // Images
    /**
     * {@code img("/logo.png")} renders {@code <img src="/logo.png">}.
     *
     * <p>Deliberate exception to the "a lone String is text" rule: {@code <img>}
     * is a void element and cannot contain text, so a String can only be a URL.</p>
     */
    public static Tag img(String src) { return new Tag("img", new Attributes().src(src)); }
    /** {@code img(src, alt)} — see {@link #img(String)} for why a String here is a URL, not text. */
    public static Tag img(String src, String alt) { return new Tag("img", new Attributes().src(src).alt(alt)); }
    /** {@code img(src("/a.png"), alt("A"), loading("lazy"))}. */
    public static Tag img(Object... attrs) { return Tag.create("img", attrs); }
    public static Tag picture(Object... children) { return Tag.create("picture", children); }
    public static Tag source(Object... attrs) { return Tag.create("source", attrs); }

    // Video & Audio
    public static Tag video(Object... children) { return Tag.create("video", children); }
    public static Tag audio(Object... children) { return Tag.create("audio", children); }
    public static Tag track(Object... attrs) { return Tag.create("track", attrs); }

    // Canvas & SVG
    public static Tag canvas(Object... children) { return Tag.create("canvas", children); }
    public static Tag svg(Object... children) { return Tag.create("svg", children); }

    // Iframe & Embed
    public static Tag iframe(Object... children) { return Tag.create("iframe", children); }
    public static Tag object(Object... children) { return Tag.create("object", children); }
    public static Tag embed(Object... attrs) { return Tag.create("embed", attrs); }
    public static Tag param(Object... attrs) { return Tag.create("param", attrs); }

    // Image maps
    public static Tag map(Object... children) { return Tag.create("map", children); }
    public static Tag area(Object... attrs) { return Tag.create("area", attrs); }
}
