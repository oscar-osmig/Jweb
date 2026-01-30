package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * Responsive image elements using the picture element and source sets.
 *
 * <p>The picture element allows art direction and format selection for
 * responsive images, serving different images based on viewport size,
 * pixel density, or supported image formats.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.elements.PictureElements.*;
 * import static com.osmig.Jweb.framework.elements.El.*;
 *
 * // Art direction: different images for different viewports
 * picture(
 *     source(srcset("hero-wide.jpg"), media("(min-width: 1024px)")),
 *     source(srcset("hero-medium.jpg"), media("(min-width: 640px)")),
 *     img(attrs().src("hero-small.jpg").attr("alt", "Hero image"))
 * )
 *
 * // Format selection: serve WebP with JPEG fallback
 * picture(
 *     source(srcset("image.avif"), type("image/avif")),
 *     source(srcset("image.webp"), type("image/webp")),
 *     img(attrs().src("image.jpg").attr("alt", "Photo"))
 * )
 *
 * // Responsive with density descriptors
 * responsiveImg("photo.jpg", "Photo", "photo-2x.jpg")
 * }</pre>
 */
public final class PictureElements {
    private PictureElements() {}

    // ==================== Picture Element ====================

    /**
     * Creates a picture element for responsive image selection.
     *
     * @param children source elements and a fallback img element
     * @return a Tag
     */
    public static Tag picture(Object... children) {
        return Tag.create("picture", children);
    }

    /**
     * Creates a picture element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag picture(Attributes attrs, Object... children) {
        return new Tag("picture", attrs, Tag.toVNodes(children));
    }

    // ==================== Source Element ====================

    /**
     * Creates a source element for use within picture.
     *
     * @param children attribute objects (srcset, media, type, sizes)
     * @return a Tag
     */
    public static Tag source(Object... children) {
        return Tag.create("source", children);
    }

    /**
     * Creates a source element with attributes.
     *
     * @param attrs the attributes
     * @return a Tag
     */
    public static Tag source(Attributes attrs) {
        return new Tag("source", attrs);
    }

    // ==================== Source Attributes ====================

    /**
     * Creates a srcset attribute.
     *
     * @param value the source set (e.g., "image.jpg", "image-2x.jpg 2x", "image-300.jpg 300w")
     * @return the Attr
     */
    public static Attr srcset(String value) {
        return new Attr("srcset", value);
    }

    /**
     * Creates a media attribute for art direction.
     *
     * @param query the media query (e.g., "(min-width: 1024px)")
     * @return the Attr
     */
    public static Attr media(String query) {
        return new Attr("media", query);
    }

    /**
     * Creates a type attribute for format selection.
     *
     * @param mimeType the MIME type (e.g., "image/webp", "image/avif")
     * @return the Attr
     */
    public static Attr type(String mimeType) {
        return new Attr("type", mimeType);
    }

    /**
     * Creates a sizes attribute for responsive sizing.
     *
     * @param value the sizes descriptor (e.g., "(max-width: 600px) 100vw, 50vw")
     * @return the Attr
     */
    public static Attr sizes(String value) {
        return new Attr("sizes", value);
    }

    /**
     * Creates a width attribute.
     *
     * @param value the width in pixels
     * @return the Attr
     */
    public static Attr width(int value) {
        return new Attr("width", String.valueOf(value));
    }

    /**
     * Creates a height attribute.
     *
     * @param value the height in pixels
     * @return the Attr
     */
    public static Attr height(int value) {
        return new Attr("height", String.valueOf(value));
    }

    // ==================== Loading Attributes ====================

    /**
     * Creates a loading attribute for lazy/eager loading.
     *
     * @param value "lazy" or "eager"
     * @return the Attr
     */
    public static Attr loading(String value) {
        return new Attr("loading", value);
    }

    /** Lazy loading attribute. */
    public static Attr lazyLoad() {
        return new Attr("loading", "lazy");
    }

    /** Eager loading attribute (default). */
    public static Attr eagerLoad() {
        return new Attr("loading", "eager");
    }

    /**
     * Creates a decoding attribute.
     *
     * @param value "sync", "async", or "auto"
     * @return the Attr
     */
    public static Attr decoding(String value) {
        return new Attr("decoding", value);
    }

    /**
     * Creates a fetchpriority attribute.
     *
     * @param value "high", "low", or "auto"
     * @return the Attr
     */
    public static Attr fetchPriority(String value) {
        return new Attr("fetchpriority", value);
    }

    // ==================== Convenience Methods ====================

    /**
     * Creates a responsive image with 1x and 2x density descriptors.
     *
     * @param src the default image source
     * @param alt the alt text
     * @param src2x the 2x resolution source
     * @return a Tag img with srcset
     */
    public static Tag responsiveImg(String src, String alt, String src2x) {
        return Tag.create("img",
            new Attr("src", src),
            new Attr("alt", alt),
            new Attr("srcset", src + " 1x," + src2x + " 2x")
        );
    }

    /**
     * Creates a lazy-loaded image with width and height for CLS prevention.
     *
     * @param src the image source
     * @param alt the alt text
     * @param w the width
     * @param h the height
     * @return a Tag img with lazy loading and dimensions
     */
    public static Tag lazyImg(String src, String alt, int w, int h) {
        return Tag.create("img",
            new Attr("src", src),
            new Attr("alt", alt),
            new Attr("loading", "lazy"),
            new Attr("width", String.valueOf(w)),
            new Attr("height", String.valueOf(h))
        );
    }
}
