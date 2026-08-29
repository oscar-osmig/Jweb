package com.osmig.Jweb.framework.dsl;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.styles.CSSValue;
import com.osmig.Jweb.framework.styles.Style;
import com.osmig.Jweb.framework.template.Template;
import org.junit.jupiter.api.Test;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.elements.Elements.each;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Compile-time proof that the legacy (pre-{@code jweb}) import surface keeps
 * working: old entry points, old type names in signatures and assignments,
 * and old wildcard-import combinations. If this file compiles, existing apps
 * built on the long imports are safe.
 */
@SuppressWarnings("deprecation")
class LegacyImportsCompatTest {

    /** Old-style component: implements Template, returns legacy Element. */
    static class LegacyPage implements Template {
        @Override
        public Element render() {
            return div(attrs().id("page-root"), h1(text("legacy")));
        }
    }

    @Test
    void legacyElementAssignmentsStillWork() {
        Element el = div(attrs().id("a"), span(text("hi")));
        Element cond = when(true, el);
        Element list = each(java.util.List.of("x"), item -> li(text(item)));
        assertTrue(el.toHtml().contains("hi"));
        assertNotNull(cond);
        assertNotNull(list);
    }

    @Test
    void legacyStyleAndCssValueAssignmentsStillWork() {
        CSSValue accent = hsl(220, 90, 56);
        Style card = style().padding(rem(1.5)).background(accent).borderRadius(px(12));
        String css = card.css();
        assertTrue(css.contains("padding: 1.5rem;"));
        assertTrue(css.contains("border-radius: 12px;"));
        assertTrue(css.contains("hsl("));
        CSSValue mixed = colorMix(red, blue, 50);
        assertNotNull(mixed.css());
    }

    @Test
    void legacyTemplateRenderStillWorks() {
        assertTrue(new LegacyPage().toHtml().contains("legacy"));
    }

    @Test
    void legacyJWebBuilderStillChains() {
        com.osmig.Jweb.framework.JWeb app = com.osmig.Jweb.framework.JWeb.create();
        com.osmig.Jweb.framework.JWeb chained =
            app.get("/legacy", () -> p(text("ok"))).pages();
        assertSame(app, chained);
        assertTrue(app.getRouter().match("GET", "/legacy").isPresent());
    }

    @Test
    void legacyRoutesInterfaceStillAcceptsLegacyJWeb() {
        com.osmig.Jweb.framework.JWebRoutes routes =
            app -> app.get("/from-legacy-routes", () -> p(text("ok")));
        // The runtime always hands routes a jweb.JWeb instance (which is a
        // legacy JWeb too) — simulate that here.
        jweb.JWeb app = jweb.JWeb.create();
        routes.configure(app);
        assertTrue(app.getRouter().match("GET", "/from-legacy-routes").isPresent());
    }
}
