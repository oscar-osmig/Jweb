package com.osmig.Jweb.framework.dsl;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.elements.PopoverElements;
import com.osmig.Jweb.framework.elements.Tag;
import com.osmig.Jweb.framework.ref.Ref;
import com.osmig.Jweb.framework.template.Template;
import com.osmig.Jweb.framework.ui.Toast;
import com.osmig.Jweb.framework.ui.UI;
import jweb.CSSValue;
import jweb.Element;
import jweb.Three;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static jweb.El.*;
import static jweb.Css.*;
import static jweb.Js.*;
import static jweb.Three.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The 3.0 syntax pass. This class deliberately imports all four DSL wildcards
 * at once — the first test is a compile-time guard that no shared name has
 * become ambiguous again.
 */
@SuppressWarnings("deprecation")
class DslPass3Test {

    // ==================== the four imports coexist ====================

    @Test
    void fourImportsCoexistWithoutAmbiguity() {
        Attr main = id("main");                        // was ambiguous with the Selector starter
        Object trusted = raw("<b>x</b>");              // was ambiguous with the CSS raw(...)
        Tag sel = select(name("x"), option("A"));      // was a CSS Selector
        Tag custom = tag("my-element", "hi");          // was a CSS Selector
        Object w1 = when(true, () -> div());           // El.when vs Three.when
        Object w2 = when(true, () -> box());
        Object r1 = repeat(2, i -> box());             // Css.repeat vs Three.repeat
        Object r2 = repeat(2, fr(1));
        Object f = fetch("/api/x");                    // page-level Action wins
        Object s = sleep(1);
        Object c = call("init");
        Object unit = em(1.2);                         // CSS unit
        Tag element = em("emphasis");                  // HTML element

        assertEquals("id", main.name());
        assertEquals("<select name=\"x\"><option>A</option></select>", sel.toHtml());
        assertEquals("<my-element>hi</my-element>", custom.toHtml());
        assertEquals("<em>emphasis</em>", element.toHtml());
        assertEquals("1.2em", ((CSSValue) unit).css());
        assertEquals("<p><b>x</b></p>", p(trusted).toHtml());
        assertNotNull(w1); assertNotNull(w2); assertNotNull(r1); assertNotNull(r2);
        assertNotNull(f); assertNotNull(s); assertNotNull(c);
    }

    @Test
    void spanWithAStringIsTheElementEvenWithCssImported() {
        assertEquals("<span>x</span>", span("x").toHtml());
        assertEquals("span 2", span(2).css());
    }

    // ==================== a String is always text ====================

    @Test
    void aStringIsTextEverywhere() {
        assertEquals("<a href=\"/docs\">Get Started</a>", a(href("/docs"), "Get Started").toHtml());
        assertEquals("<a>Hello <strong>world</strong></a>", a("Hello ", strong("world")).toHtml());
        assertEquals("<label for=\"email\">Email:</label>", label(for_("email"), "Email:").toHtml());
        assertEquals("<option value=\"us\">United States</option>", option(value("us"), "United States").toHtml());
        assertEquals("<option>Chrome</option>", option("Chrome").toHtml());
        assertEquals("<abbr title=\"HyperText Markup Language\">HTML</abbr>",
            abbr(attr("title", "HyperText Markup Language"), "HTML").toHtml());
        assertEquals("<blockquote cite=\"https://x\"><p>q</p></blockquote>",
            blockquote(attr("cite", "https://x"), p("q")).toHtml());
        assertEquals("<h1>Title</h1>", h1(style().color("red"), "Title").toHtml().replaceAll(" style=\"[^\"]*\"", ""));
    }

    @Test
    void templatesAreElementsWithoutRender() {
        Template nav = () -> p("nav");
        assertEquals("<div><p>nav</p></div>", div(nav).toHtml());
    }

    // ==================== handlers and server-driven UI as arguments ====================

    @Test
    void handlersAreArguments() {
        String server = button(id("save"), onClick(e -> {}), style().padding(px(8)), "Save").toHtml();
        assertTrue(server.contains("id=\"save\""), server);
        assertTrue(server.contains("onclick"), server);
        assertTrue(server.contains("padding: 8px"), server);
        assertTrue(server.endsWith("Save</button>"), server);

        String client = button(onClick(toggle("panel")), "Menu").toHtml();
        assertTrue(client.contains("onclick"), client);
        assertTrue(client.contains("panel"), client);

        String any = div(on("pointerdown", e -> {}), "x").toHtml();
        assertTrue(any.contains("onpointerdown"), any);
    }

    @Test
    void swapRefAndBindAreArguments() {
        String swapped = button(swap("/list?page=2", "#list"), "Next").toHtml();
        assertTrue(swapped.contains("data-swap-get=\"/list?page=2\""), swapped);
        assertTrue(swapped.contains("data-swap-target=\"#list\""), swapped);

        String form = form(id("f"), action("/x"), method("post"), swapForm("/x", "#s")).toHtml();
        assertTrue(form.contains("action=\"/x\""), form);
        assertTrue(form.contains("data-swap"), form);

        Ref r = Ref.of("search");
        assertEquals("<input id=\"search\" type=\"text\">", input(ref(r), type("text")).toHtml());

        jweb.state.State<Integer> clicks = new jweb.state.State<>("s1", 3);
        assertEquals("<span data-state-bind=\"s1\">3</span>", span(bind(clicks), clicks.get()).toHtml());
    }

    // ==================== everything that emits JS is an Action ====================

    @Test
    void refToastModalAndPopoverAreActions() {
        Ref r = Ref.of("q");
        assertEquals("document.getElementById('q').focus()", r.focus().build());
        assertEquals("document.getElementById('q').classList.add('hi')", r.addClass("hi").build());
        assertEquals("document.getElementById('q').value", r.get("value").js());
        assertTrue(button(onClick(r.focus()), "Focus").toHtml().contains("focus()"));

        assertTrue(Toast.success("Saved!").build().contains("Toast.success"));
        assertTrue(UI.Modal.open("m").build().contains("display='flex'"));
        assertEquals("document.getElementById('t').showPopover()", PopoverElements.showPopover("t").build());
        assertTrue(Toast.builder().message("Update").action("Reload", reload()).build().contains("location.reload()"));
    }

    @Test
    void templateHooksReturnActions() {
        Template page = new Template() {
            @Override public Element render() { return p("x"); }
            @Override public com.osmig.Jweb.framework.js.Actions.Action onMount() { return call("initCharts"); }
        };
        assertEquals("initCharts()", page.onMount().build());
        assertNull(page.onUnmount());
    }

    // ==================== JavaScript: inline if/elif/else, Actions as statements ====================

    @Test
    void ifElifElseTakeBodiesInlineAndActionsAreStatements() {
        String js = func("check", "x")
            .if_(v("x").gt(10), call("big"))
            .elif(v("x").gt(5), call("mid"))
            .else_(call("small"))
            .toDecl();
        assertEquals("function check(x){if((x>10)){big();}else if((x>5)){mid();}else{small();}}", js);

        String lone = func("f").if_(v("ok"), toggle("panel")).toDecl();
        assertTrue(lone.startsWith("function f(){if(ok){"), lone);
        assertTrue(lone.contains("panel"), lone);

        assertThrows(IllegalStateException.class, () -> func("g").elif(v("a"), call("x")));
    }

    @Test
    void renamedBuilderMethodsHaveNoUnderscore() {
        String js = script().let("n", 1).build();
        assertEquals("let n=1;", js);
    }

    // ==================== CSS ====================

    @Test
    void animationTakesAPlainName() {
        assertEquals("animation: spin 3s linear;", style().animation("spin", s(3), linear).build());
        assertEquals("animation: spin 3s linear 0s infinite;",
            style().animation("spin", s(3), linear, s(0), infinite).build());
    }

    @Test
    void stylesheetAddTakesAnyAtRule() {
        String css = stylesheet()
            .rule("body", style().margin(zero))
            .add(md().rule(".sidebar", style().display(block)))
            .add(keyframes("k").from(style().opacity(0)).to(style().opacity(1)))
            .build();
        assertTrue(css.contains("body{margin: 0;}") || css.contains("body {"), css);
        assertTrue(css.contains("@media"), css);
        assertTrue(css.contains(".sidebar"), css);
        assertTrue(css.contains("@keyframes k"), css);
    }

    @Test
    void cssValueOfIsTheTypedEscapeHatch() {
        assertEquals("calc(100% - 2rem)", CSSValue.of("calc(100% - 2rem)").css());
        assertEquals("margin: calc(100% - 2rem) auto;", style().margin(CSSValue.of("calc(100% - 2rem)"), auto).build());
    }

    @Test
    void selectorStartersLiveInSelectors() {
        assertEquals(".card:hover", jweb.css.Selectors.cls("card").hover().toString());
        assertEquals("#header", jweb.css.Selectors.id("header").toString());
    }

    // ==================== async / three ====================

    @Test
    void suspenseOfTakesALambdaWithoutACast() {
        assertNotNull(jweb.Suspense.of(() -> { Thread.sleep(1); return "x"; }));
        assertNotNull(jweb.Suspense.of(() -> "no checked exception either"));
    }

    @Test
    void flatLaysAShapeOnTheGround() {
        Map<String, Object> plane = plane(8, 14).flat().toMap();
        assertTrue(String.valueOf(plane.get("rot")).contains("-90"), String.valueOf(plane));
        assertTrue(String.valueOf(disc(2).flat().toMap().get("rot")).contains("-90"));
        assertTrue(String.valueOf(ring(1, 2).flat().toMap().get("rot")).contains("-90"));
    }

    @Test
    void patchIsTheOneNameThatStaysQualified() {
        // With Js.* and Three.* both imported, a bare patch(String) is ambiguous by
        // design: Three.patch(sceneId) is the live-scene patch, jweb.Js.patch(url) is
        // HTTP PATCH. Both are one qualifier away.
        assertNotNull(Three.patch("hall").node("x").opacity(0.5));
        assertNotNull(jweb.Js.patch("/api/x"));
    }
}
