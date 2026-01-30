package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * Figure and caption elements for self-contained content with captions.
 *
 * <p>The figure element represents content that is referenced from the main
 * flow of a document, such as images, diagrams, code listings, etc.,
 * with an optional figcaption.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.elements.FigureElements.*;
 * import static com.osmig.Jweb.framework.elements.El.*;
 *
 * // Image with caption
 * figure(
 *     img(attrs().src("chart.png").attr("alt", "Sales chart")),
 *     figcaption("Figure 1: Quarterly sales data")
 * )
 *
 * // Code listing with caption
 * figure(class_("code-example"),
 *     pre(code("const x = 42;")),
 *     figcaption("Example: Variable declaration")
 * )
 *
 * // Blockquote with attribution
 * figure(
 *     blockquote("To be or not to be..."),
 *     figcaption("William Shakespeare")
 * )
 * }</pre>
 */
public final class FigureElements {
    private FigureElements() {}

    /**
     * Creates a figure element.
     *
     * @param children the figure content (images, code, etc.) and optional figcaption
     * @return a Tag
     */
    public static Tag figure(Object... children) {
        return Tag.create("figure", children);
    }

    /**
     * Creates a figure element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag figure(Attributes attrs, Object... children) {
        return new Tag("figure", attrs, Tag.toVNodes(children));
    }

    /**
     * Creates a figcaption element.
     *
     * @param children the caption content
     * @return a Tag
     */
    public static Tag figcaption(Object... children) {
        return Tag.create("figcaption", children);
    }

    /**
     * Creates a figcaption element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag figcaption(Attributes attrs, Object... children) {
        return new Tag("figcaption", attrs, Tag.toVNodes(children));
    }
}
