package com.osmig.Jweb.framework.styles;

import org.junit.jupiter.api.Test;

import java.util.List;

import static jweb.Css.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * In CSS the order of declarations decides which one wins, so anything that
 * emits a rule has to preserve the order the properties were written in.
 */
class StyleOrderingTest {

    @Test
    void toMapKeepsTheOrderPropertiesWereWrittenIn() {
        // A shorthand after a longhand overrides it, so this order is load-bearing.
        var style = style()
            .marginTop("10px")
            .margin("0")
            .paddingLeft("4px")
            .padding("1rem");

        assertEquals(List.of("margin-top", "margin", "padding-left", "padding"),
            List.copyOf(style.toMap().keySet()));
    }

    @Test
    void mediaQueryEmitsDeclarationsInOrder() {
        String css = media().maxWidth(px(768))
            .rule(".card", style().marginTop("10px").margin("0"))
            .build();

        assertTrue(css.indexOf("margin-top") < css.indexOf("margin:"),
            "declaration order must survive into the @media block: " + css);
    }

    @Test
    void containerQueryEmitsDeclarationsInOrder() {
        String css = ContainerQuery.container().minWidth(px(400))
            .rule(".card", style().paddingLeft("4px").padding("1rem"))
            .build();

        assertTrue(css.indexOf("padding-left") < css.indexOf("padding:"),
            "declaration order must survive into the @container block: " + css);
    }

    @Test
    void keyframesEmitDeclarationsInOrder() {
        String css = keyframes("grow")
            .from(style().width("0px").minWidth("10px"))
            .to(style().width("100px"))
            .build();

        assertTrue(css.indexOf("width") < css.indexOf("min-width"),
            "declaration order must survive into @keyframes: " + css);
    }
}
