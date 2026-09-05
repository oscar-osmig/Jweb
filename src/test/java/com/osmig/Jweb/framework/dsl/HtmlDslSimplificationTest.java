package com.osmig.Jweb.framework.dsl;

import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.elements.Button;
import com.osmig.Jweb.framework.elements.Elements;
import com.osmig.Jweb.framework.elements.FormElements;
import com.osmig.Jweb.framework.elements.FormEnhancements;
import com.osmig.Jweb.framework.elements.Input;
import com.osmig.Jweb.framework.elements.PictureElements;
import com.osmig.Jweb.framework.elements.PopoverElements;
import com.osmig.Jweb.framework.elements.Tag;
import com.osmig.Jweb.framework.styles.CSS;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The HTML-DSL simplification pass (catalog items H1–H17).
 *
 * <p>Each test names the rule it pins down, so a regression points straight at
 * the decision it broke.</p>
 */
@SuppressWarnings("deprecation")
class HtmlDslSimplificationTest {

    // ==================== H1 / X1 — a lone String is text ====================

    @Test
    void loneStringChildIsTextNotAnAttribute() {
        assertEquals("<a>Home</a>", a("Home").toHtml());
        assertEquals("<label>Email:</label>", label("Email:").toHtml());
        assertEquals("<textarea>Hello</textarea>", textarea("Hello").toHtml());
        assertEquals("<blockquote>To be</blockquote>", blockquote("To be").toHtml());
        assertEquals("<q>Hi</q>", q("Hi").toHtml());
        assertEquals("<datalist>Browsers</datalist>", datalist("Browsers").toHtml());
        assertEquals("<optgroup>Cars</optgroup>", optgroup("Cars").toHtml());
    }

    @Test
    void loneStringTextIsEscaped() {
        assertEquals("<a>&lt;b&gt;</a>", a("<b>").toHtml());
        assertEquals("<label>a &amp; b</label>", label("a & b").toHtml());
    }

    @Test
    void theLegacyElFacadeAgreesWithTheJwebFacade() {
        assertEquals("<a>Home</a>", com.osmig.Jweb.framework.elements.El.a("Home").toHtml());
        assertEquals("<label>Email:</label>", com.osmig.Jweb.framework.elements.El.label("Email:").toHtml());
        assertEquals("<textarea>Hi</textarea>", com.osmig.Jweb.framework.elements.El.textarea("Hi").toHtml());
        assertEquals("<blockquote>Q</blockquote>", com.osmig.Jweb.framework.elements.El.blockquote("Q").toHtml());
        assertEquals("<q>Q</q>", com.osmig.Jweb.framework.elements.El.q("Q").toHtml());
        assertEquals("<datalist>D</datalist>", com.osmig.Jweb.framework.elements.El.datalist("D").toHtml());
        assertEquals("<optgroup>G</optgroup>", com.osmig.Jweb.framework.elements.El.optgroup("G").toHtml());
        assertEquals("<textarea>Hi</textarea>", FormElements.textarea("Hi").toHtml());
        assertEquals("<label>Hi</label>", FormElements.label("Hi").toHtml());
    }

    @Test
    void multiArgumentAttributeFirstFormsStillWork() {
        assertEquals("<a href=\"/home\">Home</a>", a(href("/home"), "Home").toHtml());
        assertEquals("<label for=\"email\">Email:</label>", label(for_("email"), "Email:").toHtml());
        assertEquals("<option value=\"v\">t</option>", option(value("v"), "t").toHtml());
    }

    @Test
    void imgKeepsStringMeaningUrlBecauseItIsAVoidElement() {
        assertEquals("<img src=\"/logo.png\">", img("/logo.png").toHtml());
        assertEquals("<img src=\"/logo.png\" alt=\"Logo\">", img("/logo.png", "Logo").toHtml());
    }

    @Test
    void inlineScriptContentIsNotHtmlEscaped() {
        String html = inlineScript("if (a < b && c) { x('\"q\"'); }").toHtml();
        assertTrue(html.contains("a < b && c"), html);
        assertFalse(html.contains("&lt;"), html);
        assertFalse(html.contains("&amp;"), html);
    }

    @Test
    void scriptTakesAnAttrForItsSource() {
        assertEquals("<script src=\"/app.js\"></script>", script(src("/app.js")).toHtml());
    }

    // ==================== H2 — (Attributes, ...) overloads deleted ====================

    @Test
    void attributesFirstCallsRenderIdenticallyThroughTheVarargsForm() {
        // The exact output the deleted div(Attributes, Object...) overload produced.
        assertEquals("<div id=\"x\"><p>y</p></div>", div(attrs().id("x"), p("y")).toHtml());

        // ... and the varargs form is now the only one, so both spellings agree.
        assertEquals(div(attrs().id("x"), p("y")).toHtml(),
            tag("div", attrs().id("x"), p("y")).toHtml());
        assertEquals(div(attrs().id("x"), p("y")).toHtml(),
            Elements.tag("div", attrs().id("x"), p("y")).toHtml());
    }

    @Test
    void attributesFirstCallsRenderIdenticallyForEveryFamilyThatLostAnOverload() {
        assertEquals("<section class=\"s\">hi</section>", section(attrs().class_("s"), "hi").toHtml());
        assertEquals("<form action=\"/x\" method=\"POST\"></form>",
            form(attrs().action("/x").method("POST")).toHtml());
        assertEquals("<table class=\"t\"><tr><td>1</td></tr></table>",
            table(attrs().class_("t"), tr(td("1"))).toHtml());
        assertEquals("<video controls></video>", video(attrs().controls()).toHtml());
        assertEquals("<hr class=\"rule\">", hr(attrs().class_("rule")).toHtml());
        assertEquals("<link rel=\"stylesheet\" href=\"/a.css\">",
            link(attrs().rel("stylesheet").href("/a.css")).toHtml());
    }

    @Test
    void anArrayOfChildrenIsStillSpreadAfterTheOverloadDeletion() {
        List<jweb.Element> kids = List.of(p("a"), p("b"));
        assertEquals("<div id=\"x\"><p>a</p><p>b</p></div>",
            div(attrs().id("x"), kids.toArray()).toHtml());
        assertEquals("<div id=\"x\"><p>a</p><p>b</p></div>",
            div(attrs().id("x"), kids).toHtml());
    }

    // ==================== H3 — one (Object...) form per element ====================

    @Test
    void everyElementTakesMixedAttrsAndChildren() {
        assertEquals("<img src=\"/a.png\" alt=\"A\" loading=\"lazy\">",
            img(src("/a.png"), alt("A"), loading("lazy")).toHtml());
        assertEquals("<iframe src=\"/e\"></iframe>", iframe(src("/e")).toHtml());
        assertEquals("<track src=\"/c.vtt\">", track(src("/c.vtt")).toHtml());
        assertEquals("<embed src=\"/m\">", embed(src("/m")).toHtml());
        assertEquals("<source src=\"/v.mp4\">", source(src("/v.mp4")).toHtml());
        assertEquals("<canvas id=\"c\"></canvas>", canvas(id("c")).toHtml());
        assertEquals("<object data=\"/o\"></object>", object(attr("data", "/o")).toHtml());
        assertEquals("<param name=\"k\">", param(name("k")).toHtml());
        assertEquals("<map name=\"m\"><area alt=\"a\"></map>",
            map(name("m"), area(alt("a"))).toHtml());
        assertEquals("<colgroup><col span=\"2\"></colgroup>",
            colgroup(col(attr("span", "2"))).toHtml());
        assertEquals("<table><caption>C</caption><tfoot><tr><td>t</td></tr></tfoot></table>",
            table(caption("C"), tfoot(tr(td("t")))).toHtml());
        assertEquals("<audio controls></audio>", audio(attr("controls", null)).toHtml());
    }

    // ==================== H4 — InlineStyle is lambda + done() ====================

    @Test
    void inlineStyleFinishesWithDoneOrTheLambdaForm() {
        String viaDone = div(attrs().style().color(CSS.hex("#f00")).done().id("a"), "x").toHtml();
        String viaLambda = div(attrs().style(s -> s.color(CSS.hex("#f00"))).id("a"), "x").toHtml();
        assertEquals(viaLambda, viaDone);
        assertTrue(viaDone.contains("style=\"color: #f00;\""), viaDone);
        assertTrue(viaDone.contains("id=\"a\""), viaDone);
    }

    @Test
    void inlineStylePassedStraightToAnElementStillAutoFinalizes() {
        String html = div(attrs().class_("card").style().padding(CSS.px(10)), p("hi")).toHtml();
        assertTrue(html.contains("class=\"card\""), html);
        assertTrue(html.contains("style=\"padding: 10px;\""), html);
        assertTrue(html.contains("<p>hi</p>"), html);
    }

    // ==================== H6 — one conditional-class API ====================

    @Test
    void classIfAndClassToggleAreTheConditionalClassApi() {
        assertEquals("<div class=\"btn active\"></div>",
            div(attrs().class_("btn").classIf("active", true)).toHtml());
        assertEquals("<div class=\"btn\"></div>",
            div(attrs().class_("btn").classIf("active", false)).toHtml());
        assertEquals("<div class=\"btn open\"></div>",
            div(attrs().class_("btn").classToggle(true, "open", "closed")).toHtml());
    }

    // ==================== H8 — layout presets merge, never clobber ====================

    @Test
    void layoutPresetsMergeIntoAnExistingStyleInsteadOfClobberingIt() {
        String html = div(attrs().style("color:red").flexCenter()).toHtml();
        assertTrue(html.contains("color:red"), "existing style must survive: " + html);
        assertTrue(html.contains("display:flex"), html);
        assertTrue(html.contains("align-items:center"), html);
    }

    @Test
    void layoutPresetsMergeAfterTheStyleBuilderToo() {
        String html = div(attrs().style(s -> s.margin(CSS.px(0))).gridCols(3, "1rem")).toHtml();
        assertTrue(html.contains("margin: 0px;"), html);
        assertTrue(html.contains("display:grid"), html);
        assertTrue(html.contains("repeat(3,1fr)"), html);
    }

    @Test
    void layoutPresetsChainWithEachOtherWithoutLosingEarlierDeclarations() {
        Attributes a = attrs().flexRow("1rem").gridCols(2);
        String style = a.get("style");
        assertTrue(style.contains("flex-direction:row"), style);
        assertTrue(style.contains("grid-template-columns"), style);
    }

    // ==================== H9 — one input story ====================

    @Test
    void everyEntryPointAgreesOnTheIdEqualsNamePolicy() {
        assertEquals(Elements.textInput("u").toHtml(), FormElements.textInput("u").toHtml());
        assertEquals(Elements.dateInput("d").toHtml(), FormEnhancements.dateInput("d").toHtml());
        assertTrue(FormEnhancements.dateInput("d").toHtml().contains("id=\"d\""),
            "FormEnhancements.dateInput used to set no id");
        assertEquals(Elements.colorInput("c", "#fff").toHtml(), FormEnhancements.colorInput("c", "#fff").toHtml());
        assertEquals(Elements.rangeInput("v", 0, 100, 50).toHtml(), FormEnhancements.rangeInput("v", 0, 100, 50).toHtml());
        assertTrue(FormEnhancements.monthInput("m").toHtml().contains("id=\"m\""));
    }

    @Test
    void radioIdsUseTheDashSchemeEverywhere() {
        assertEquals("color-red", Elements.radioId("color", "red"));
        assertTrue(Elements.radio("color", "red").toHtml().contains("id=\"color-red\""));
        assertTrue(FormElements.radio("color", "red").toHtml().contains("id=\"color-red\""));
        String viaBuilder = Input.radio("color", "red").toHtml();
        assertTrue(viaBuilder.contains("id=\"color-red\""), viaBuilder);
        assertFalse(viaBuilder.contains("color_red"), viaBuilder);
    }

    @Test
    void inputBuilderIsNoLongerADeadEnd() {
        String html = Input.text("q")
            .attr("list", "suggestions")
            .data("role", "search")
            .aria("label", "Search")
            .toHtml();
        assertTrue(html.contains("list=\"suggestions\""), html);
        assertTrue(html.contains("data-role=\"search\""), html);
        assertTrue(html.contains("aria-label=\"Search\""), html);

        // ... and hands back a Tag so the full element API stays available
        Tag asTag = Input.text("q").required().toTag();
        assertTrue(asTag.addClass("wide").toHtml().contains("class=\"wide\""));
    }

    @Test
    void buttonBuilderHasOnClickAndEscapeHatches() {
        String handler = Button.of("Save").onClick(e -> { }).toHtml();
        assertTrue(handler.contains("data-jweb-onclick=\"h_"),
            "server handlers delegate via data attribute (nonce CSPs block inline handlers): " + handler);

        String action = Button.of("Retry")
            .onClick(com.osmig.Jweb.framework.js.Actions.reload()).toHtml();
        assertTrue(action.contains("onclick="), "inline fallback outside a render context: " + action);
        assertTrue(action.contains("reload"), action);

        // Inside a render context the Actions form delegates like the server
        // form does — inline on*= attributes can't run under a nonce CSP
        var context = com.osmig.Jweb.framework.state.StateManager.createContext();
        try {
            String cspSafe = Button.of("Retry")
                .onClick(com.osmig.Jweb.framework.js.Actions.reload()).toHtml();
            assertTrue(cspSafe.contains("data-jweb-actclick=\"a"), cspSafe);
            assertFalse(cspSafe.contains("onclick="), cspSafe);
        } finally {
            com.osmig.Jweb.framework.state.StateManager.clearContext();
        }

        String extras = Button.of("More")
            .attr("popovertarget", "menu").data("role", "menu").aria("expanded", "false").toHtml();
        assertTrue(extras.contains("popovertarget=\"menu\""), extras);
        assertTrue(extras.contains("data-role=\"menu\""), extras);
        assertTrue(extras.contains("aria-expanded=\"false\""), extras);

        assertTrue(Button.submit("Go").toTag().addClass("btn").toHtml().contains("class=\"btn\""));
    }

    // ==================== H10 — exact HTML attribute spelling ====================

    @Test
    void attributeHelpersUseTheExactHtmlSpelling() {
        assertEquals("fetchpriority", PictureElements.fetchpriority("high").name());
        assertEquals("popovertarget", PopoverElements.popovertarget("m").name());
        assertEquals("popovertargetaction", PopoverElements.popovertargetaction("show").name());
        assertEquals(PopoverElements.popoverTarget("m"), PopoverElements.popovertarget("m"));
        String html = Input.text("t").minlength(3).maxlength(9).toHtml();
        assertTrue(html.contains("minlength=\"3\""), html);
        assertTrue(html.contains("maxlength=\"9\""), html);
    }

    // ==================== H11/H12 — replacements for the pruned helpers ====================

    @Test
    void theBlessedReplacementsForThePrunedCompositesWork() {
        assertEquals("<time datetime=\"2026-01-21\">January 21, 2026</time>",
            time(datetime("2026-01-21"), text("January 21, 2026")).toHtml());
        assertEquals("<img src=\"/a.png\" loading=\"lazy\">", img(src("/a.png"), loading("lazy")).toHtml());
        assertEquals("<button type=\"submit\">Go</button>", button(type("submit"), "Go").toHtml());
        assertEquals("<fieldset disabled></fieldset>", fieldset(disabled()).toHtml());
        assertEquals("<template><p>x</p></template>", template(p("x")).toHtml());
    }

    @Test
    void linkBuildsALinkElementNotAnAnchor() {
        assertTrue(link(attrs().rel("icon").href("/f.ico")).toHtml().startsWith("<link"));
    }

    // ==================== H13 — Tag's varargs rules are loud ====================

    @Test
    void stylesInsideAnIterableAreExtractedInsteadOfDropped() {
        List<Object> items = List.of(class_("card"), CSS.style().prop("color", "red"));
        String html = div(items, p("x")).toHtml();
        assertTrue(html.contains("class=\"card\""), html);
        assertTrue(html.contains("style=\"color: red;\""), "Style in an Iterable used to vanish: " + html);
        assertTrue(html.contains("<p>x</p>"), html);
    }

    @Test
    void inlineStyleInsideAnIterableIsExtractedToo() {
        List<Object> items = List.of(attrs().id("a").style().color(CSS.hex("#00f")));
        String html = div(items).toHtml();
        assertTrue(html.contains("id=\"a\""), html);
        assertTrue(html.contains("style=\"color: #00f;\""), html);
    }

    @Test
    void anUnrenderableChildThrowsInsteadOfRenderingToString() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> div(Map.of("a", "b")));
        assertTrue(ex.getMessage().contains("Map"), ex.getMessage());
        assertTrue(ex.getMessage().contains("text(String)"), ex.getMessage());
        assertTrue(ex.getMessage().contains("raw(String)"), ex.getMessage());

        assertThrows(IllegalArgumentException.class, () -> p(new Object()));
    }

    @Test
    void theAcceptedChildTypesStillRender() {
        assertEquals("<p>hi</p>", p("hi").toHtml());
        assertEquals("<p>42</p>", p(42).toHtml());
        assertEquals("<p>3.5</p>", p(3.5).toHtml());
        assertEquals("<p>true</p>", p(true).toHtml());
        assertEquals("<p><span>s</span></p>", p(span("s")).toHtml());
        assertEquals("<p>raw<b>x</b></p>", p(text("raw"), raw("<b>x</b>")).toHtml());
        assertEquals("<p></p>", p((Object) null).toHtml());
    }

    // ==================== H15 — boolean attributes are uniform ====================

    @Test
    void booleanAttributesRenderBareAndTakeAConditionalForm() {
        assertEquals("<input type=\"text\" required>",
            input(attrs().type("text").required()).toHtml());
        assertEquals("<input type=\"text\" required>",
            input(attrs().type("text").required(true)).toHtml());
        assertEquals("<input type=\"text\">",
            input(attrs().type("text").required(false)).toHtml());

        assertEquals("<input readonly>", input(attrs().readonly(true)).toHtml());
        assertEquals("<input autofocus>", input(attrs().autofocus(true)).toHtml());
        assertEquals("<select multiple></select>", select(attrs().multiple(true)).toHtml());
        assertEquals("<video controls autoplay loop muted playsinline></video>",
            video(attrs().controls(true).autoplay(true).loop(true).muted(true).playsinline(true)).toHtml());
        assertEquals("<form novalidate></form>", form(attrs().novalidate(true)).toHtml());
        assertEquals("<div inert></div>", div(attrs().inert(true)).toHtml());
        assertEquals("<iframe allowfullscreen></iframe>", iframe(attrs().allowfullscreen(true)).toHtml());
        assertEquals("<div itemscope></div>", div(attrs().itemscope(true)).toHtml());
        assertEquals("<a download></a>", a(attrs().download(true)).toHtml());

        // false never emits the attribute
        assertEquals("<video></video>",
            video(attrs().controls(false).autoplay(false).loop(false).muted(false)).toHtml());
    }

    @Test
    void tagAndAttrAgreeOnTheBareBooleanConvention() {
        assertEquals(input(attrs().disabled()).toHtml(), new Tag("input").disabled().toHtml());
        assertEquals("<input disabled>", new Tag("input").disabled().toHtml());
        assertEquals("<input required>", new Tag("input").required(true).toHtml());
        assertEquals("<input>", new Tag("input").required(false).toHtml());
        assertEquals("<input checked>", new Tag("input").checked().toHtml());
        assertEquals("<div hidden></div>", new Tag("div").hidden(true).toHtml());
        assertEquals("<input autofocus>", new Tag("input").autofocus().toHtml());
        assertEquals("<input readonly>", new Tag("input").readonly().toHtml());
        // ... and the typed builders use the same convention
        assertEquals("<input type=\"text\" name=\"n\" id=\"n\" required>",
            Input.text("n").required().toHtml());
    }

    // ==================== H17 — numeric overloads ====================

    @Test
    void wholeNumberAttributesDoNotWidenToDouble() {
        assertEquals("<input value=\"3\">", input(attrs().value(3)).toHtml());
        assertEquals("<input step=\"2\">", input(attrs().step(2)).toHtml());
        assertEquals("<progress value=\"0.5\" max=\"1.0\"></progress>",
            progress(attrs().value(0.5).set("max", "1.0")).toHtml());
        assertTrue(Input.number("n").step(2).toHtml().contains("step=\"2\""));
        assertTrue(Input.number("n").step(0.5).toHtml().contains("step=\"0.5\""));
    }
}
