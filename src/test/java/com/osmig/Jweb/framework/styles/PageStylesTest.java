package com.osmig.Jweb.framework.styles;

import com.osmig.Jweb.framework.security.CspNonce;
import com.osmig.Jweb.framework.state.StateManager;
import jweb.Element;
import jweb.Template;
import jweb.css.Stylesheet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static jweb.Css.*;
import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Stylesheets that belong to pages: a template's {@code styles()} hook and the
 * generated class behind a conditional inline style, both collected during the
 * render and delivered once.
 */
class PageStylesTest {

    @AfterEach
    void cleanup() {
        StateManager.clearContext();
        CspNonce.clear();
    }

    // ==================== Generated classes ====================

    @Test
    void noRenderContextKeepsTheInlineDeclarationsOnly() {
        String html = div(style().color(red).hover(style().color(blue)), "hi").toHtml();
        assertTrue(html.contains("color: #f00;"), html);
        assertFalse(html.contains("class="), "no page to deliver the hover rule through: " + html);
    }

    @Test
    void aConditionalStyleGetsAContentHashedClass() {
        StateManager.createContext();
        String html = div(style().color(red).hover(style().color(blue)), "hi").toHtml();
        assertTrue(html.matches(".*class=\"j-[0-9a-f]{6}\".*"), html);
        assertTrue(html.contains("style=\"color: #f00;\""), "plain declarations stay inline: " + html);

        String css = PageStyles.drainCss(StateManager.getContext());
        assertNotNull(css);
        assertTrue(css.matches("\\.j-[0-9a-f]{6}:hover\\{color: #00f;\\}"), css);
    }

    @Test
    void theSameRulesShareOneClassAndOneRuleBlock() {
        var context = StateManager.createContext();
        String a = div(style().padding(px(4)).hover(style().color(blue))).toHtml();
        String b = span(style().margin(px(8)).hover(style().color(blue))).toHtml();

        String cls = classOf(a);
        assertEquals(cls, classOf(b), "identical hover rules mint one class");

        String css = PageStyles.drainCss(context);
        assertEquals(1, countOccurrences(css, "{color: #00f;}"), css);
    }

    @Test
    void classNamesAreStableAcrossContexts() {
        StateManager.createContext();
        String first = classOf(div(style().hover(style().color(blue))).toHtml());
        StateManager.clearContext();
        StateManager.createContext();
        String second = classOf(div(style().hover(style().color(blue))).toHtml());
        assertEquals(first, second, "a content hash is the same in every render");
    }

    @Test
    void differentRulesGetDifferentClasses() {
        StateManager.createContext();
        String hover = classOf(div(style().hover(style().color(blue))).toHtml());
        String focus = classOf(div(style().focus(style().color(blue))).toHtml());
        assertNotEquals(hover, focus);
    }

    @Test
    void aStyleWithOnlyConditionalRulesEmitsNoEmptyStyleAttribute() {
        StateManager.createContext();
        String html = div(style().hover(style().color(blue)), "hi").toHtml();
        assertFalse(html.contains("style="), html);
        assertTrue(html.contains("class=\"j-"), html);
    }

    @Test
    void aGeneratedClassMergesWithAnAuthoredOneInEitherOrder() {
        StateManager.createContext();
        String before = div(class_("card"), style().hover(style().color(blue))).toHtml();
        String after = div(style().hover(style().color(blue)), class_("card")).toHtml();
        assertTrue(before.contains("class=\"card j-"), before);
        assertTrue(after.contains("class=\"card j-"), after);
    }

    @Test
    void attrsStyleBuilderAlsoCarriesConditionalRules() {
        StateManager.createContext();
        String html = div(attrs().class_("btn").style(s -> s.color(red).hover(style().color(blue)))).toHtml();
        assertTrue(html.contains("class=\"btn j-"), html);
        assertTrue(html.contains("color: #f00;"), html);
        assertNotNull(PageStyles.drainCss(StateManager.getContext()));
    }

    // ==================== The variant vocabulary ====================

    @Test
    void everyConditionalFormEmitsItsRule() {
        StateManager.createContext();
        var context = StateManager.getContext();

        div(style()
            .focusVisible(style().outline(px(2), solid, blue))
            .disabled(style().opacity(0.5))
            .placeholder(style().color(gray))
            .popoverOpen(style().display(block))
            .before("• ", style().color(gray))
            .after("", style().display(block))
            .at(md(), style().fontSize(rem(1.25)))
            .dark(style().color(white))
            .reducedMotion(style().transition("none"))
            .startingStyle(style().opacity(0))).toHtml();

        String css = PageStyles.drainCss(context);
        assertTrue(css.contains(":focus-visible{"), css);
        assertTrue(css.contains(":disabled{"), css);
        assertTrue(css.contains("::placeholder{"), css);
        assertTrue(css.contains(":popover-open{"), css);
        assertTrue(css.contains("::before{content: \"• \";"), css);
        assertTrue(css.contains("::after{content: \"\";"), css);
        assertTrue(css.contains("@media (min-width: 768px){"), css);
        assertTrue(css.contains("@media (prefers-color-scheme: dark){"), css);
        assertTrue(css.contains("@media (prefers-reduced-motion: reduce){"), css);
        assertTrue(css.contains("@starting-style{"), css);
    }

    @Test
    void aConditionalRuleNestsInsideItsMediaQuery() {
        StateManager.createContext();
        var context = StateManager.getContext();
        div(style().at(md(), style().color(red).hover(style().color(blue)))).toHtml();
        String css = PageStyles.drainCss(context);
        assertTrue(css.contains("@media (min-width: 768px)"), css);
        assertTrue(css.contains(":hover{color: #00f;}"), css);
        assertEquals(2, countOccurrences(css, "@media (min-width: 768px)"),
            "the hover inside the query is wrapped by it too");
    }

    @Test
    void applyCarriesConditionalRulesTooSoMixinsCompose() {
        StateManager.createContext();
        var context = StateManager.getContext();
        jweb.Style<?> chip = style().padding(px(4)).hover(style().color(blue));
        div(style().margin(px(2)).apply(chip)).toHtml();
        assertNotNull(PageStyles.drainCss(context));
    }

    // ==================== Template stylesheets ====================

    record Card(String title) implements Template {
        @Override public Element render() { return div(class_("card"), title); }
        @Override public Stylesheet styles() {
            return stylesheet().rule(".card", style().padding(rem(1)));
        }
    }

    @Test
    void aTemplatesStylesheetIsCollectedOnceHoweverManyInstancesRender() {
        var context = StateManager.createContext();
        div(new Card("a"), new Card("b"), new Card("c")).toHtml();
        String css = PageStyles.drainCss(context);
        assertNotNull(css);
        assertEquals(1, countOccurrences(css, ".card{padding: 1rem;}"), css);
    }

    @Test
    void drainDeliversEachBlockExactlyOnce() {
        var context = StateManager.createContext();
        div(new Card("a")).toHtml();
        assertNotNull(PageStyles.drainCss(context));
        assertNull(PageStyles.drainCss(context), "second drain has nothing new");

        div(style().hover(style().color(blue))).toHtml();
        String second = PageStyles.drainCss(context);
        assertNotNull(second);
        assertFalse(second.contains(".card"), "already delivered: " + second);
    }

    @Test
    void componentStylesheetsComeFirstSoGeneratedClassesWinTheTies() {
        var context = StateManager.createContext();
        // the element renders (and registers its class) before the template
        // that declares .card is walked
        div(style().hover(style().color(blue)), new Card("a")).toHtml();
        String css = PageStyles.drainCss(context);
        assertTrue(css.indexOf(".card{") < css.indexOf(".j-"),
            "a stylesheet is what a component declares; a generated class is what one "
                + "element overrode it with: " + css);
    }

    @Test
    void nothingPendingRendersNoStyleTag() {
        var context = StateManager.createContext();
        assertEquals("", PageStyles.drainStyleTag(context));
        assertEquals("", PageStyles.drainStyleTag(null));
    }

    @Test
    void theStyleTagCarriesTheCspNonce() {
        var context = StateManager.createContext();
        CspNonce.set("n0nce");
        div(new Card("a")).toHtml();
        String tag = PageStyles.drainStyleTag(context);
        assertTrue(tag.startsWith("<style nonce=\"n0nce\">"), tag);
        assertTrue(tag.endsWith("</style>"), tag);
    }

    // ==================== Helpers ====================

    private static String classOf(String html) {
        var m = java.util.regex.Pattern.compile("class=\"([^\"]*)\"").matcher(html);
        assertTrue(m.find(), "no class attribute in " + html);
        return m.group(1);
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) count++;
        return count;
    }
}
