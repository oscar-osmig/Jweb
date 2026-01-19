package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * HTML media and embedded content elements: img, video, audio, canvas, svg, iframe.
 */
public final class MediaElements {
    private MediaElements() {}

    // Images
    public static Tag img(String src) { return new Tag("img", new Attributes().src(src)); }
    public static Tag img(String src, String alt) { return new Tag("img", new Attributes().src(src).alt(alt)); }
    public static Tag img(Attributes attrs) { return new Tag("img", attrs); }
    public static Tag picture(Object... children) { return Tag.create("picture", children); }
    public static Tag source(Attributes attrs) { return new Tag("source", attrs); }

    // Video & Audio
    public static Tag video(Attributes attrs, Object... children) { return new Tag("video", attrs, Tag.toVNodes(children)); }
    public static Tag video(Object... children) { return Tag.create("video", children); }
    public static Tag audio(Attributes attrs, Object... children) { return new Tag("audio", attrs, Tag.toVNodes(children)); }
    public static Tag audio(Object... children) { return Tag.create("audio", children); }
    public static Tag track(Attributes attrs) { return new Tag("track", attrs); }

    // Canvas & SVG
    public static Tag canvas(Attributes attrs) { return new Tag("canvas", attrs); }
    public static Tag canvas(Object... children) { return Tag.create("canvas", children); }
    public static Tag svg(Attributes attrs, Object... children) { return new Tag("svg", attrs, Tag.toVNodes(children)); }
    public static Tag svg(Object... children) { return Tag.create("svg", children); }

    // Iframe & Embed
    public static Tag iframe(Attributes attrs) { return new Tag("iframe", attrs); }
    public static Tag iframe(Attributes attrs, Object... children) { return new Tag("iframe", attrs, Tag.toVNodes(children)); }
    public static Tag object(Attributes attrs, Object... children) { return new Tag("object", attrs, Tag.toVNodes(children)); }
    public static Tag embed(Attributes attrs) { return new Tag("embed", attrs); }

    // Image maps
    public static Tag map(Attributes attrs, Object... children) { return new Tag("map", attrs, Tag.toVNodes(children)); }
    public static Tag area(Attributes attrs) { return new Tag("area", attrs); }
}
