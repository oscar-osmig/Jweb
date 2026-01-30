package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * Interactive text and inline semantic elements.
 *
 * <p>These elements provide semantic meaning to inline text content:
 * abbreviations, definitions, citations, quotations, keyboard input,
 * sample output, and variables.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.elements.InteractiveElements.*;
 * import static com.osmig.Jweb.framework.elements.El.*;
 *
 * // Abbreviation with expansion
 * p("The ", abbr("HTML", "HyperText Markup Language"), " specification")
 *
 * // Definition of a term
 * p("A ", dfn("closure"), " is a function that captures its scope.")
 *
 * // Citation
 * p("As described in ", cite("The Art of Programming"), "...")
 *
 * // Inline quotation
 * p("She said, ", q("Hello World"), ", and the program ran.")
 *
 * // Keyboard input
 * p("Press ", kbd("Ctrl"), "+", kbd("C"), " to copy.")
 *
 * // Code sample output
 * p("The program outputs: ", samp("Hello World"))
 *
 * // Variable
 * p("Let ", var_("x"), " equal 5.")
 *
 * // Blockquote with cite
 * blockquote("https://example.com",
 *     p("To be or not to be, that is the question.")
 * )
 * }</pre>
 */
public final class InteractiveElements {
    private InteractiveElements() {}

    // ==================== Abbreviation ====================

    /**
     * Creates an abbr element with title.
     *
     * @param abbreviation the abbreviation text
     * @param title the full expansion
     * @return a Tag
     */
    public static Tag abbr(String abbreviation, String title) {
        return Tag.create("abbr", new Attr("title", title), abbreviation);
    }

    /**
     * Creates an abbr element with children.
     *
     * @param children the content
     * @return a Tag
     */
    public static Tag abbr(Object... children) {
        return Tag.create("abbr", children);
    }

    // ==================== Definition ====================

    /**
     * Creates a dfn (definition) element.
     *
     * @param children the term being defined
     * @return a Tag
     */
    public static Tag dfn(Object... children) {
        return Tag.create("dfn", children);
    }

    /**
     * Creates a dfn element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag dfn(Attributes attrs, Object... children) {
        return new Tag("dfn", attrs, Tag.toVNodes(children));
    }

    // ==================== Citation ====================

    /**
     * Creates a cite element for referencing a creative work.
     *
     * @param children the title of the work
     * @return a Tag
     */
    public static Tag cite(Object... children) {
        return Tag.create("cite", children);
    }

    /**
     * Creates a cite element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag cite(Attributes attrs, Object... children) {
        return new Tag("cite", attrs, Tag.toVNodes(children));
    }

    // ==================== Inline Quotation ====================

    /**
     * Creates a q (inline quotation) element.
     * Browser automatically adds quotation marks.
     *
     * @param children the quoted text
     * @return a Tag
     */
    public static Tag q(Object... children) {
        return Tag.create("q", children);
    }

    /**
     * Creates a q element with a cite URL.
     *
     * @param citeUrl the source URL
     * @param children the quoted text
     * @return a Tag
     */
    public static Tag q(String citeUrl, Object... children) {
        return Tag.create("q", new Attr("cite", citeUrl), children);
    }

    // ==================== Blockquote ====================

    /**
     * Creates a blockquote element.
     *
     * @param children the quoted content
     * @return a Tag
     */
    public static Tag blockquote(Object... children) {
        return Tag.create("blockquote", children);
    }

    /**
     * Creates a blockquote element with a cite URL.
     *
     * @param citeUrl the source URL
     * @param children the quoted content
     * @return a Tag
     */
    public static Tag blockquote(String citeUrl, Object... children) {
        return Tag.create("blockquote", new Attr("cite", citeUrl), children);
    }

    /**
     * Creates a blockquote element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag blockquote(Attributes attrs, Object... children) {
        return new Tag("blockquote", attrs, Tag.toVNodes(children));
    }

    // ==================== Keyboard Input ====================

    /**
     * Creates a kbd element for keyboard input.
     *
     * @param children the key name(s)
     * @return a Tag
     */
    public static Tag kbd(Object... children) {
        return Tag.create("kbd", children);
    }

    // ==================== Sample Output ====================

    /**
     * Creates a samp element for sample program output.
     *
     * @param children the sample output text
     * @return a Tag
     */
    public static Tag samp(Object... children) {
        return Tag.create("samp", children);
    }

    // ==================== Variable ====================

    /**
     * Creates a var element for a mathematical or programming variable.
     *
     * @param children the variable name
     * @return a Tag
     */
    public static Tag var_(Object... children) {
        return Tag.create("var", children);
    }

    // ==================== Mark (Highlight) ====================

    /**
     * Creates a mark element for highlighted/marked text.
     *
     * @param children the text to highlight
     * @return a Tag
     */
    public static Tag mark(Object... children) {
        return Tag.create("mark", children);
    }

    // ==================== Subscript/Superscript ====================

    /**
     * Creates a sub (subscript) element.
     *
     * @param children the subscript content
     * @return a Tag
     */
    public static Tag sub(Object... children) {
        return Tag.create("sub", children);
    }

    /**
     * Creates a sup (superscript) element.
     *
     * @param children the superscript content
     * @return a Tag
     */
    public static Tag sup(Object... children) {
        return Tag.create("sup", children);
    }

    // ==================== Inserted/Deleted Text ====================

    /**
     * Creates an ins (inserted text) element.
     *
     * @param children the inserted content
     * @return a Tag
     */
    public static Tag ins(Object... children) {
        return Tag.create("ins", children);
    }

    /**
     * Creates a del (deleted text) element.
     *
     * @param children the deleted content
     * @return a Tag
     */
    public static Tag del(Object... children) {
        return Tag.create("del", children);
    }

    /**
     * Creates an s (strikethrough) element for content no longer accurate.
     *
     * @param children the content
     * @return a Tag
     */
    public static Tag s(Object... children) {
        return Tag.create("s", children);
    }
}
