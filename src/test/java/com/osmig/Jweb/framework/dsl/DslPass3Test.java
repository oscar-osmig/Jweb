package com.osmig.Jweb.framework.dsl;

import jweb.Attr;
import com.osmig.Jweb.framework.elements.PopoverElements;
import jweb.Tag;
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

    /**
     * The layout mixins share their names with keyword constants ({@code grid},
     * {@code center}, {@code cover}, {@code contain}, {@code row}) and, for
     * {@code grid}, with a Three scene setting. Fields and methods are separate
     * namespaces, so both spellings must keep resolving under all four
     * wildcards.
     */
    @Test
    void theLayoutMixinsDoNotShadowTheKeywordConstants() {
        jweb.Style<?> asMixin = grid(3, rem(1));
        jweb.Style<?> asConstant = style().display(grid);
        jweb.three.SceneSetting sceneGrid = Three.grid(10, 40);

        assertEquals("display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 1rem;",
            asMixin.build());
        assertEquals("display: grid;", asConstant.build());
        assertNotNull(sceneGrid.toMap());

        assertEquals("display: flex; align-items: center;", row().build());
        assertEquals("align-items: center;", style().alignItems(center).build());
        assertEquals("object-fit: cover;", style().objectFit(cover).build());
        assertTrue(cover().build().contains("object-fit: cover;"));
        assertTrue(contain().build().contains("object-fit: contain;"));
        assertTrue(center().build().startsWith("display: flex;"));
        assertTrue(stack(rem(1)).build().contains("flex-direction: column;"));
        assertTrue(cluster(rem(1)).build().contains("flex-wrap: wrap;"));
        assertTrue(container(px(900)).build().contains("max-width: 900px;"));
        assertTrue(card().build().contains("var(--color-surface"));
        assertTrue(truncate().build().contains("ellipsis"));
        assertTrue(truncate(2).build().contains("-webkit-line-clamp: 2;"));
        assertTrue(srOnly().build().contains("position: absolute;"));
        assertTrue(fullBleed().build().contains("100vw"));
        assertEquals("aspect-ratio: 4 / 3;", aspect(4, 3).build());
        assertTrue(autoGrid(px(240)).build().contains("auto-fit"));
        assertTrue(autoGrid(px(240), rem(1)).build().contains("gap: 1rem;"));
    }

    /** The 3.0 keyword constants, under all four wildcards. */
    @Test
    void theNewKeywordConstantsResolve() {
        assertEquals("field-sizing: content;", style().fieldSizing(content).build());
        assertEquals("scrollbar-width: thin;", style().scrollbarWidth(thin).build());
        assertEquals("scrollbar-gutter: stable both-edges;",
            style().scrollbarGutter(stableBothEdges).build());
        assertEquals("text-box: trim-both cap alphabetic;",
            style().textBox(trimBoth, capAlphabetic).build());
        assertEquals("text-box-trim: trim-start;", style().textBoxTrim(trimStart).build());
        assertEquals("text-box-trim: trim-end;", style().textBoxTrim(trimEnd).build());
        assertEquals("text-box-edge: ex alphabetic;", style().textBoxEdge(exAlphabetic).build());
        assertEquals("interpolate-size: allow-keywords;",
            style().interpolateSize(allowKeywords).build());
    }

    /**
     * The behavior verbs and the ten re-exported browser modules join the same
     * four-wildcard import, so every name here has to stay unambiguous against
     * {@code El}, {@code Css} and {@code Three}.
     */
    @Test
    void behaviorsAndModulesJoinTheFourImports() {
        Object copied = copy("x").feedback("Copied!");
        Object fromEl = copyFrom("pre");
        Object nav = navigate("/x").target("#main").prefetch();
        Object warm = prefetch(".link").onVisible();
        Object marked = activeLink(".link");
        Object spy = scrollSpy("#toc", "h2").within(".content");
        Object pane = splitPane("#g", "#l").min(120);
        Object gutter = lineGutter("#e", "#l");
        Object grow = resizeToContent("textarea");
        Object element = customElement("x-card").shadow();
        Object stmt = if_(v("a"), preventDefault(), stopPropagation(), return_());
        Object stateRead = syncState("s1");
        Object stateWatch = onStateChange("s1", callback("now").log(v("now")));

        // re-exported module statics, under all four wildcards
        Object clip = writeText("hi");
        Object store = local();
        Object history = replaceState("/x");
        Object params = queryParamsObject();
        Object fd = formData();
        Object frames = raf(callback("t").log(v("t")));
        Object db = openDB("app", 1);
        Object watch = intersection();

        assertNotNull(copied); assertNotNull(fromEl); assertNotNull(nav);
        assertNotNull(warm); assertNotNull(marked); assertNotNull(spy);
        assertNotNull(pane); assertNotNull(gutter); assertNotNull(grow);
        assertNotNull(element); assertNotNull(stmt); assertNotNull(stateRead);
        assertNotNull(stateWatch); assertNotNull(clip); assertNotNull(store);
        assertNotNull(history); assertNotNull(params); assertNotNull(fd);
        assertNotNull(frames); assertNotNull(db); assertNotNull(watch);
    }

    /** Any action is a statement in any builder body, through {@code does(...)}. */
    @Test
    void doesTakesActionsValsAndStatements() {
        assertEquals("function(e){e.preventDefault();toggleit();}",
            callback("e").does(preventDefault(), call("toggleit")).toExpr());
        assertEquals("(function(){JWeb.resizeToContent('textarea');})()",
            actions().does(resizeToContent("textarea")).build());
        assertEquals("(function(){var n=1;})()", iife().var_("n", 1).build());
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

        // onDblClick is an element argument the same way onClick is.
        String dbl = button(onDblClick(e -> {}), "Zoom").toHtml();
        assertTrue(dbl.contains("ondblclick"), dbl);
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
            @Override public jweb.Action onMount() { return call("initCharts"); }
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

    // ==================== the ten modern-CSS modules join the Css chain =====

    /**
     * CSSColors → CSSNested → CSSProperty → CSSScope → CSSLayer → CSSScrollSnap
     * → CSSMasking → CSSLogicalProperties → CSSSubgrid → CSSTextWrap →
     * CSSAnchorPositioning is now one chain, all reachable through the single
     * {@code import static jweb.Css.*;} already at the top of this file — with
     * no ambiguity against El/Js/Three.
     */
    @Test
    void modernCssModulesReachThroughOneImportWithNoAmbiguity() {
        assertEquals("text-wrap: balance;", textWrapBalance().build());
        assertEquals("anchor-name: --menu;", anchorName("--menu").build());
        assertEquals("anchor(--menu bottom)", anchor("--menu", "bottom").css());
        assertEquals("grid-template-columns: subgrid;", subgridColumns().build());
        assertEquals("scroll-snap-align: start;", snapAlign("start").build());
        assertEquals("clip-path: circle(50%);", clipCircle("50%").build());
        assertEquals("margin-inline: auto;", marginInline("auto").build());
        assertEquals(".menu {\n  color: red;\n}\n", nest(".menu").prop("color", "red").build());

        // Composes with the rest of the CSS DSL via apply() — the point of retyping.
        assertEquals("text-wrap: balance; color: red;",
            style().apply(textWrapBalance()).color("red").build());
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
