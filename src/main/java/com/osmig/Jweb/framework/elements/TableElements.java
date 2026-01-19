package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * HTML table elements: table, thead, tbody, tfoot, tr, th, td, caption, colgroup, col.
 */
public final class TableElements {
    private TableElements() {}

    public static Tag table(Object... children) {
        return Tag.create("table", children);
    }

    public static Tag table(Attributes attrs, Object... children) {
        return new Tag("table", attrs, Tag.toVNodes(children));
    }

    public static Tag thead(Object... children) {
        return Tag.create("thead", children);
    }

    public static Tag tbody(Object... children) {
        return Tag.create("tbody", children);
    }

    public static Tag tfoot(Object... children) {
        return Tag.create("tfoot", children);
    }

    public static Tag tr(Object... children) {
        return Tag.create("tr", children);
    }

    public static Tag tr(Attributes attrs, Object... children) {
        return new Tag("tr", attrs, Tag.toVNodes(children));
    }

    public static Tag th(Object... children) {
        return Tag.create("th", children);
    }

    public static Tag th(Attributes attrs, Object... children) {
        return new Tag("th", attrs, Tag.toVNodes(children));
    }

    public static Tag td(Object... children) {
        return Tag.create("td", children);
    }

    public static Tag td(Attributes attrs, Object... children) {
        return new Tag("td", attrs, Tag.toVNodes(children));
    }

    public static Tag caption(Object... children) {
        return Tag.create("caption", children);
    }

    public static Tag colgroup(Object... children) {
        return Tag.create("colgroup", children);
    }

    public static Tag colgroup(Attributes attrs, Object... children) {
        return new Tag("colgroup", attrs, Tag.toVNodes(children));
    }

    public static Tag col() {
        return Tag.create("col");
    }

    public static Tag col(Attributes attrs) {
        return new Tag("col", attrs);
    }
}
