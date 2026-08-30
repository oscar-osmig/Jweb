package com.osmig.Jweb.framework.dsl;

import jweb.CSSValue;
import jweb.Element;
import jweb.JWeb;
import jweb.JWebRoutes;
import jweb.Style;
import jweb.Template;
import org.junit.jupiter.api.Test;

import static jweb.Css.*;
import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The new short-import surface: {@code import static jweb.El.*} and friends.
 * Exercises the aggregated façades (El includes Elements' typed inputs,
 * Css includes units/colors/grid/media) and the jweb type aliases.
 */
class JwebShortImportsTest {

    /** New-style page: jweb.Template + jweb.Element, one import each. */
    static class ShortPage implements Template {
        @Override
        public Element render() {
            return div(class_("page"),
                h1(text("short")),
                emailInput("email", "you@example.com"),   // from Elements, now in El
                icon("/favicon.svg"),                     // from legacy El, now in El
                when(true, p(text("visible"))));
        }
    }

    @Test
    void elFacadeCoversElementsAndElSurfaces() {
        Element el = div(
            attrs().role("main"),
            svg(viewBox(0, 0, 24, 24), path(d("M0 0h24v24H0z"))), // legacy El extras
            textInput("name", "Your name"),                        // Elements typed inputs
            submitButton("Go"));
        String html = el.toHtml();
        assertTrue(html.contains("viewBox"));
        assertTrue(html.contains("type=\"text\""));
        assertTrue(html.contains("role=\"main\""));
    }

    @Test
    void cssFacadeCoversUnitsColorsAndMedia() {
        CSSValue accent = hsl(220, 90, 56);
        Style card = style()
            .padding(rem(1.5))
            .background(accent)
            .prop("display", "grid")
            .prop("gap", px(8));
        String css = card.css();
        assertTrue(css.contains("padding: 1.5rem;"));
        assertTrue(css.contains("hsl("));

        String mq = media().maxWidth(px(768)).rule(".card", style().padding(rem(1))).build();
        assertTrue(mq.contains("@media"));
        assertTrue(mq.contains("max-width: 768px"));
    }

    @Test
    void templateAndElementAliasesRenderLikeLegacy() {
        assertTrue(new ShortPage().toHtml().contains("short"));
    }

    @Test
    void jwebAppBuilderChainsWithTightTypes() {
        JWeb app = JWeb.create()
            .get("/new", () -> p(text("ok")))
            .pages();
        assertTrue(app.getRouter().match("GET", "/new").isPresent());
    }

    @Test
    void newRoutesInterfaceConfiguresApp() {
        JWebRoutes routes = app -> app.get("/routed", () -> p(text("ok")));
        JWeb app = JWeb.create();
        routes.configure(app);
        assertTrue(app.getRouter().match("GET", "/routed").isPresent());
    }

    @Test
    void longTailShellsExposeLegacyStatics() {
        // jweb.css.* — specialty style modules keep their class names
        assertEquals(":has(.card)", jweb.css.Selectors.has(".card"));
        String frames = jweb.css.Keyframes.fadeIn().build();
        assertTrue(frames.contains("@keyframes"));
        assertNotNull(jweb.css.Stylesheet.stylesheet());
        assertNotNull(jweb.css.MediaQuery.media().maxWidth(px(768)));
        // jweb.js.* — browser-API modules
        String js = jweb.js.JSClipboard.copyText("hi").build();
        assertTrue(js.contains("clipboard"));
    }

    @Test
    void appInfrastructureAliasesWork() {
        // Static entries reachable through the jweb names
        assertNotNull(jweb.Middlewares.recommended());
        assertEquals(200, jweb.Response.html(p(text("ok"))).getStatusCode().value());
        assertNotNull(jweb.Streamed.of(() -> p(text("late"))));

        // Value types: factories hand out the jweb subtype, so both names assign
        jweb.CsrfToken token = com.osmig.Jweb.framework.security.CsrfToken.generate();
        com.osmig.Jweb.framework.security.CsrfToken legacyTyped = token;
        assertNotNull(legacyTyped.getValue());

        jweb.FormValidator validator = jweb.FormValidator.create();
        assertNotNull(validator);

        jweb.Form form = jweb.Form.create();
        com.osmig.Jweb.framework.forms.Form legacyFormTyped = form;
        assertTrue(legacyFormTyped.action("/submit").text("name").build()
            .toHtml().contains("<form"));

        jweb.state.State<Integer> count =
            com.osmig.Jweb.framework.state.StateManager.createState(0);
        count.set(41);
        assertEquals(41, count.get());

        // Annotations carry the same Spring meta-annotations as the legacy ones
        assertNotNull(jweb.api.REST.class.getAnnotation(
            org.springframework.web.bind.annotation.RequestMapping.class));
        assertNotNull(jweb.JWebApplication.class.getAnnotation(
            org.springframework.boot.autoconfigure.SpringBootApplication.class));

        // jweb.el.* element helper shells
        assertTrue(jweb.el.SVGElements.svg().toHtml().contains("<svg"));
    }

    @Test
    void mixedOldAndNewTypesInteroperate() {
        // A legacy component value assigns to the new Element type...
        Element fromLegacy = com.osmig.Jweb.framework.elements.Elements.fragment(p(text("x")));
        assertNotNull(fromLegacy.toHtml());
        // ...and a new-style style value flows into legacy acceptors.
        String html = div(attrs().style(style().margin(px(4))), text("y")).toHtml();
        assertTrue(html.contains("margin: 4px"));
    }
}
