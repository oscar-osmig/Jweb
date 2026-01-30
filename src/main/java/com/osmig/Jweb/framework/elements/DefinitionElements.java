package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * Definition list elements for term-definition pairs.
 *
 * <p>Definition lists (dl, dt, dd) are semantic elements for glossaries,
 * metadata, key-value pairs, and FAQs.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.elements.DefinitionElements.*;
 * import static com.osmig.Jweb.framework.elements.El.*;
 *
 * // Glossary
 * dl(
 *     dt("HTML"),
 *     dd("HyperText Markup Language"),
 *     dt("CSS"),
 *     dd("Cascading Style Sheets"),
 *     dt("JS"),
 *     dd("JavaScript")
 * )
 *
 * // Metadata / key-value pairs
 * dl(class_("metadata"),
 *     dt("Author"),
 *     dd("Jane Doe"),
 *     dt("Published"),
 *     dd("2026-01-29"),
 *     dt("Category"),
 *     dd("Technology")
 * )
 * }</pre>
 */
public final class DefinitionElements {
    private DefinitionElements() {}

    /**
     * Creates a definition list element (dl).
     *
     * @param children dt and dd elements
     * @return a Tag
     */
    public static Tag dl(Object... children) {
        return Tag.create("dl", children);
    }

    /**
     * Creates a definition list element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag dl(Attributes attrs, Object... children) {
        return new Tag("dl", attrs, Tag.toVNodes(children));
    }

    /**
     * Creates a definition term element (dt).
     *
     * @param children the term content
     * @return a Tag
     */
    public static Tag dt(Object... children) {
        return Tag.create("dt", children);
    }

    /**
     * Creates a definition term element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag dt(Attributes attrs, Object... children) {
        return new Tag("dt", attrs, Tag.toVNodes(children));
    }

    /**
     * Creates a definition description element (dd).
     *
     * @param children the definition content
     * @return a Tag
     */
    public static Tag dd(Object... children) {
        return Tag.create("dd", children);
    }

    /**
     * Creates a definition description element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag dd(Attributes attrs, Object... children) {
        return new Tag("dd", attrs, Tag.toVNodes(children));
    }
}
