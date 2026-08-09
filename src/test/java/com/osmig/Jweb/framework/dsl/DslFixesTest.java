package com.osmig.Jweb.framework.dsl;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.elements.El;
import com.osmig.Jweb.framework.elements.PopoverElements;
import com.osmig.Jweb.framework.styles.Style;
import com.osmig.Jweb.framework.util.Json;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DslFixesTest {

    @Test
    void stylePropAcceptsPropertyValueStrings() {
        String css = com.osmig.Jweb.framework.styles.CSS.style()
            .prop("anchor-name:--menu")
            .prop("scroll-snap-type: x mandatory")
            .css();

        assertTrue(css.contains("anchor-name: --menu;"));
        assertTrue(css.contains("scroll-snap-type: x mandatory;"));
    }

    @Test
    void stylePropRejectsMalformedStrings() {
        assertThrows(IllegalArgumentException.class,
            () -> com.osmig.Jweb.framework.styles.CSS.style().prop("no-colon-here"));
    }

    @Test
    void attributeValuesEscapeQuotesAndApostrophes() {
        String html = El.div(El.attrs().set("title", "it's \"quoted\"")).toHtml();
        assertFalse(html.contains("it's"), "apostrophe must be escaped: " + html);
        assertTrue(html.contains("&#x27;"));
        assertTrue(html.contains("&quot;"));
    }

    @Test
    void singleArgOptionUsesValueAsText() {
        String html = El.option("blue").toHtml();
        assertTrue(html.contains("value=\"blue\""));
        assertTrue(html.contains(">blue<"));
    }

    @Test
    void popoverKeepsAttrsPassedAmongChildren() {
        String html = PopoverElements.autoPopover("menu",
            Attr.class_("dropdown"), El.p("content")).toHtml();

        assertTrue(html.contains("popover=\"auto\""));
        assertTrue(html.contains("id=\"menu\""));
        assertTrue(html.contains("class=\"dropdown\""), "Attr child must not be dropped: " + html);
        assertTrue(html.contains("content"));
    }

    @Test
    void typedJsonListParsing() {
        List<Point> points = Json.parseList("[{\"x\":1,\"y\":2},{\"x\":3,\"y\":4}]", Point.class);
        // Elements are real Points, not LinkedHashMaps (the old type-erasure bug)
        assertEquals(1, points.get(0).x);
        assertEquals(4, points.get(1).y);
    }

    @Test
    void elReExportsConditionals() {
        String shown = El.div(El.when(true, El.span("yes"))).toHtml();
        String hidden = El.div(El.when(false, El.span("no"))).toHtml();
        assertTrue(shown.contains("yes"));
        assertFalse(hidden.contains("no"));
    }

    public static class Point {
        public int x;
        public int y;
    }

    // ==================== Builder simplifications ====================

    @Test
    void styleFragmentsCompose() {
        var fragment = com.osmig.Jweb.framework.styles.CSS.style()
            .prop("background", "red")
            .prop("color", "white");

        String css = com.osmig.Jweb.framework.styles.CSS.style()
            .prop("padding", "1rem")
            .apply(fragment)
            .css();

        assertTrue(css.contains("padding: 1rem;"));
        assertTrue(css.contains("background: red;"));
        assertTrue(css.contains("color: white;"));
    }

    @Test
    void borderMaskEmitsCrossBrowserPair() {
        String css = com.osmig.Jweb.framework.styles.CSS.style().borderMask().css();
        assertTrue(css.contains("-webkit-mask:"));
        assertTrue(css.contains("mask-composite: exclude;"));
        assertTrue(css.contains("-webkit-mask-composite: xor;"));
    }

    @Test
    void contentEmitsEmptyQuotes() {
        String css = com.osmig.Jweb.framework.styles.CSS.style().content().css();
        assertTrue(css.contains("content: '';"));
    }

    @Test
    void svgIconBuilderChain() {
        String html = El.svg(El.attrs().viewBox(0, 0, 24, 24).width(24).height(24).lineIcon(2)).toHtml();
        assertTrue(html.contains("viewBox=\"0 0 24 24\""));
        assertTrue(html.contains("stroke=\"currentColor\""));
        assertTrue(html.contains("stroke-linecap=\"round\""));
        assertTrue(html.contains("stroke-linejoin=\"round\""));
        assertTrue(html.contains("stroke-width=\"2\""));
    }

    @Test
    void onClickAcceptsActions() {
        String html = El.button(El.attrs().onClick(
            com.osmig.Jweb.framework.js.Actions.reload()), El.text("Retry")).toHtml();
        assertTrue(html.contains("onclick="), "onclick attribute expected: " + html);
        assertTrue(html.contains("reload"), "reload JS expected: " + html);
    }

    @Test
    void metaHelpers() {
        assertTrue(El.metaCharset().toHtml().contains("charset=\"UTF-8\""));
        String viewport = El.metaViewport().toHtml();
        assertTrue(viewport.contains("name=\"viewport\""));
        assertTrue(viewport.contains("width=device-width"));
    }
}
