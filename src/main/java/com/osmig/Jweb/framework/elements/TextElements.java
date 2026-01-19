package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * Text and inline content elements: div, span, p, h1-h6, a, strong, em, code, etc.
 */
public final class TextElements {
    private TextElements() {}

    // Container elements
    public static Tag div(Object... children) {
        return Tag.create("div", children);
    }

    public static Tag div(Attributes attrs, Object... children) {
        return new Tag("div", attrs, Tag.toVNodes(children));
    }

    public static Tag span(Object... children) {
        return Tag.create("span", children);
    }

    public static Tag span(Attributes attrs, Object... children) {
        return new Tag("span", attrs, Tag.toVNodes(children));
    }

    // Paragraph
    public static Tag p(Object... children) {
        return Tag.create("p", children);
    }

    public static Tag p(Attributes attrs, Object... children) {
        return new Tag("p", attrs, Tag.toVNodes(children));
    }

    // Headings
    public static Tag h1(Object... children) { return Tag.create("h1", children); }
    public static Tag h2(Object... children) { return Tag.create("h2", children); }
    public static Tag h3(Object... children) { return Tag.create("h3", children); }
    public static Tag h4(Object... children) { return Tag.create("h4", children); }
    public static Tag h5(Object... children) { return Tag.create("h5", children); }
    public static Tag h6(Object... children) { return Tag.create("h6", children); }

    // Links
    public static Tag a(String href, Object... children) {
        return new Tag("a", new Attributes().href(href), Tag.toVNodes(children));
    }

    public static Tag a(Attributes attrs, Object... children) {
        return new Tag("a", attrs, Tag.toVNodes(children));
    }

    // Text formatting
    public static Tag strong(Object... children) { return Tag.create("strong", children); }
    public static Tag em(Object... children) { return Tag.create("em", children); }
    public static Tag b(Object... children) { return Tag.create("b", children); }
    public static Tag i(Object... children) { return Tag.create("i", children); }
    public static Tag u(Object... children) { return Tag.create("u", children); }
    public static Tag s(Object... children) { return Tag.create("s", children); }
    public static Tag small(Object... children) { return Tag.create("small", children); }
    public static Tag mark(Object... children) { return Tag.create("mark", children); }
    public static Tag del(Object... children) { return Tag.create("del", children); }
    public static Tag ins(Object... children) { return Tag.create("ins", children); }
    public static Tag sub(Object... children) { return Tag.create("sub", children); }
    public static Tag sup(Object... children) { return Tag.create("sup", children); }

    // Code elements
    public static Tag code(Object... children) { return Tag.create("code", children); }
    public static Tag pre(Object... children) { return Tag.create("pre", children); }
    public static Tag kbd(Object... children) { return Tag.create("kbd", children); }
    public static Tag samp(Object... children) { return Tag.create("samp", children); }
    public static Tag var_(Object... children) { return Tag.create("var", children); }

    // Block elements
    public static Tag blockquote(Object... children) { return Tag.create("blockquote", children); }
    public static Tag cite(Object... children) { return Tag.create("cite", children); }
    public static Tag q(Object... children) { return Tag.create("q", children); }
    public static Tag abbr(Object... children) { return Tag.create("abbr", children); }
    public static Tag dfn(Object... children) { return Tag.create("dfn", children); }
    public static Tag time(Object... children) { return Tag.create("time", children); }

    // Line breaks
    public static Tag br() { return Tag.create("br"); }
    public static Tag hr() { return Tag.create("hr"); }
    public static Tag wbr() { return Tag.create("wbr"); }
}
