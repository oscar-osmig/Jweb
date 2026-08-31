package com.osmig.Jweb.framework.dsl;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.elements.El;
import com.osmig.Jweb.framework.elements.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Builder style ({@code div().id("x")}) and varargs/Attributes style
 * ({@code div(id("x"))}) share one attribute surface, so neither dead-ends and
 * both emit the same HTML.
 */
class BuilderStyleTest {

    // ==================== (a) Builder style reaches the full surface ====================

    @Test
    void builderStyleReachesGlobalAttributes() {
        assertEquals("<div id=\"x\" rel=\"noopener\" tabindex=\"1\"></div>",
            El.div().id("x").rel("noopener").tabindex(1).toHtml());
    }

    @Test
    void builderStyleReachesTableAttributes() {
        assertEquals("<td colspan=\"2\" rowspan=\"3\" scope=\"col\"></td>",
            El.td().colspan(2).rowspan(3).scope("col").toHtml());
    }

    @Test
    void builderStyleReachesCompositeHelpers() {
        assertEquals("<a href=\"/x\" target=\"_blank\" rel=\"noopener noreferrer\"></a>",
            El.a().href("/x").targetBlank().toHtml());
    }

    @Test
    void builderStyleReachesFormAttributes() {
        assertEquals("<input type=\"file\" accept=\"image/*\" multiple>",
            El.input().type("file").accept("image/*").multiple().toHtml());
    }

    @Test
    void builderStyleReachesSvgAttributes() {
        assertEquals("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\""
                + " fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\""
                + " stroke-linecap=\"round\" stroke-linejoin=\"round\"></svg>",
            El.svg().viewBox(0, 0, 24, 24).lineIcon(2).toHtml());
    }

    @Test
    void builderStyleReachesExtraEventHandlers() {
        String html = El.div().onScroll(e -> { }).onDragStart(e -> { }).toHtml();

        assertTrue(html.contains("onscroll=\"JWeb.call("), html);
        assertTrue(html.contains("ondragstart=\"JWeb.call("), html);
    }

    @Test
    void builderStyleReachesFrameworkHelpers() {
        assertEquals("<button data-swap-get=\"/list\" data-swap-target=\"#out\""
                + " data-swap-mode=\"morph\" data-swap-push=\"/page/2\"></button>",
            El.button().swapMorph("/list", "#out").swapPush("/page/2").toHtml());
    }

    // ==================== (b) Both styles emit identical HTML ====================

    @Test
    void plainAttributesMatchAcrossStyles() {
        String builder = El.div().id("x").class_("card").title("hi").toHtml();
        String varargs = El.div(Attr.id("x"), Attr.class_("card"), Attr.title("hi")).toHtml();
        String attrs = El.div(El.attrs().id("x").class_("card").title("hi")).toHtml();

        assertEquals("<div id=\"x\" class=\"card\" title=\"hi\"></div>", builder);
        assertEquals(builder, varargs);
        assertEquals(builder, attrs);
    }

    @Test
    void booleanAttributesMatchAcrossStyles() {
        String builder = El.input().type("checkbox").checked().disabled().required().toHtml();
        String varargs = El.input(Attr.type("checkbox"), Attr.checked(), Attr.disabled(), Attr.required()).toHtml();
        String attrs = El.input(El.attrs().type("checkbox").checked().disabled().required()).toHtml();

        // Bare attributes, not name="" — the null-value convention both styles share.
        assertEquals("<input type=\"checkbox\" checked disabled required>", builder);
        assertEquals(builder, varargs);
        assertEquals(builder, attrs);
    }

    @Test
    void conditionalBooleanAttributesMatchAcrossStyles() {
        String builder = El.input().disabled(false).checked(true).hidden(false).toHtml();
        String attrs = El.input(El.attrs().disabled(false).checked(true).hidden(false)).toHtml();

        assertEquals("<input checked>", builder);
        assertEquals(builder, attrs);
    }

    @Test
    void classHelpersMatchAcrossStyles() {
        String builder = El.div()
            .class_("btn").addClass("primary").classIf("active", true)
            .classIf("muted", false).classToggle(false, "open", "closed").toHtml();
        String attrs = El.div(El.attrs()
            .class_("btn").addClass("primary").classIf("active", true)
            .classIf("muted", false).classToggle(false, "open", "closed")).toHtml();

        assertEquals("<div class=\"btn primary active closed\"></div>", builder);
        assertEquals(builder, attrs);
    }

    @Test
    void classesVarargsMatchAcrossStyles() {
        String builder = El.div().classes("btn", "primary", "lg").toHtml();
        String attrs = El.div(El.attrs().classes("btn", "primary", "lg")).toHtml();

        assertEquals("<div class=\"btn primary lg\"></div>", builder);
        assertEquals(builder, attrs);
    }

    @Test
    void addClassOnEmptyElementDoesNotLeadWithSpace() {
        assertEquals("<div class=\"solo\"></div>", El.div().addClass("solo").toHtml());
    }

    @Test
    void styleLambdaMatchesAcrossStyles() {
        String builder = El.div().style(s -> s.prop("color:red")).toHtml();
        String attrs = El.div(El.attrs().style(s -> s.prop("color:red"))).toHtml();

        assertEquals(builder, attrs);
        assertTrue(builder.contains("color: red"), builder);
    }

    @Test
    void numericAndSvgAttributesMatchAcrossStyles() {
        String builder = El.div().width(24).height(24).min(0).max(10).step(0.5).toHtml();
        String attrs = El.div(El.attrs().width(24).height(24).min(0).max(10).step(0.5)).toHtml();

        assertEquals("<div width=\"24\" height=\"24\" min=\"0\" max=\"10\" step=\"0.5\"></div>", builder);
        assertEquals(builder, attrs);
    }

    // ==================== (c) Chaining keeps the precise type ====================

    @Test
    void tagChainStaysATag() {
        // No cast anywhere: every link in the chain is typed Tag.
        Tag tag = El.div()
            .id("card")
            .class_("panel")
            .rel("noopener")
            .tabindex(0)
            .onScroll(e -> { })
            .text("body");

        assertEquals("body", tag.getChildren().get(0).toHtml());
        assertEquals("card", tag.get("id"));
    }

    @Test
    void attributesChainStaysAttributes() {
        // No cast anywhere: every link in the chain is typed Attributes.
        Attributes attributes = El.attrs()
            .id("card")
            .class_("panel")
            .rel("noopener")
            .tabindex(0);

        assertEquals("noopener", attributes.get("rel"));
        assertEquals("0", attributes.get("tabindex"));
    }

    @Test
    void transitionBuilderReturnsToItsOwnType() {
        Tag tag = El.div().transition().property("opacity").done();
        Attributes attributes = El.attrs().transition().property("opacity").done();

        assertEquals("transition:opacity 300ms ease;", tag.get("style"));
        assertEquals(tag.get("style"), attributes.get("style"));
    }
}
