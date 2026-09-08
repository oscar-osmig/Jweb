package jweb;

import org.junit.jupiter.api.Test;

import static jweb.Css.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The layout mixins, the token system, and the properties 3.0 added — the
 * things an app used to hand-roll one declaration at a time.
 */
class CssMixinsTest {

    // ==================== Mixins ====================

    @Test
    void rowAndStackAreFlexOneWayOrTheOther() {
        assertEquals("display: flex; align-items: center;", row().build());
        assertEquals("display: flex; align-items: center; gap: 1rem;", row(rem(1)).build());
        assertEquals("display: flex; flex-direction: column;", stack().build());
        assertEquals("display: flex; flex-direction: column; gap: 0.5rem;", stack(rem(0.5)).build());
    }

    @Test
    void centreAndClusterAreTheTwoOtherFlexArrangements() {
        assertEquals("display: flex; align-items: center; justify-content: center;", center().build());
        assertEquals("display: flex; flex-wrap: wrap; align-items: center; gap: 0.5rem;",
            cluster(rem(0.5)).build());
    }

    @Test
    void containerIsACentredColumnWithAMaximum() {
        assertEquals("width: 100%; max-width: 1200px; margin-inline: auto;",
            container(px(1200)).build());
    }

    @Test
    void cardReadsTheThemeTokensWithNeutralFallbacks() {
        String css = card().build();
        assertTrue(css.contains("var(--color-surface, #ffffff)"), css);
        assertTrue(css.contains("var(--color-border, #e2e8f0)"), css);
        assertTrue(css.contains("var(--radius-lg, 12px)"), css);
        assertTrue(css.contains("var(--space-4, 1rem)"), css);
    }

    @Test
    void truncateIsOneLineOrN() {
        assertEquals("overflow: hidden; text-overflow: ellipsis; white-space: nowrap;",
            truncate().build());
        String clamped = truncate(3).build();
        assertTrue(clamped.contains("-webkit-line-clamp: 3;"), clamped);
    }

    @Test
    void theRestOfTheMixins() {
        assertTrue(srOnly().build().contains("position: absolute;"));
        assertEquals("width: 100vw; margin-inline: calc(50% - 50vw);", fullBleed().build());
        assertEquals("aspect-ratio: 16 / 9;", aspect(16, 9).build());
        assertEquals("width: 100%; height: 100%; object-fit: cover;", cover().build());
        assertEquals("width: 100%; height: 100%; object-fit: contain;", contain().build());
        assertEquals("display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 1rem;",
            grid(3, rem(1)).build());
        assertEquals("display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));",
            autoGrid(px(240)).build());
        assertEquals("display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem;",
            autoGrid(px(240), rem(1)).build());
    }

    @Test
    void mixinsComposeWithApplyAndWithConditionalRules() {
        Style<?> chip = row(rem(0.5)).apply(card()).hover(style().color(hex("#4f46e5")));
        assertTrue(chip.build().startsWith("display: flex;"), chip.build());
        assertTrue(chip.build().contains("var(--color-surface"), chip.build());
        assertTrue(chip.hasVariants());
        assertEquals("&:hover{color: #4f46e5;}", chip.variantCss("&"));
    }

    // ==================== Theme ====================

    @Test
    void aThemeIsCustomPropertiesReadBackAsVars() {
        jweb.css.Theme theme = jweb.css.Theme.light()
            .color("primary", hex("#4f46e5"))
            .space("4", rem(1))
            .radius("md", px(6));

        assertEquals(":root{--color-primary:#4f46e5;--space-4:1rem;--radius-md:6px;}", theme.css());
        assertEquals("var(--color-primary)", jweb.css.Theme.color("primary").css());
        assertEquals("var(--space-4)", jweb.css.Theme.space("4").css());
        assertEquals("var(--radius-md)", jweb.css.Theme.radius("md").css());
        assertEquals("#4f46e5", theme.valueOf("color-primary"));
    }

    @Test
    void theDarkBlockSwapsUnderTheSchemeAndUnderTheDataAttribute() {
        String css = jweb.css.Theme.light()
            .color("bg", hex("#ffffff"))
            .dark(jweb.css.Theme.dark().color("bg", hex("#0f172a")))
            .css();

        assertTrue(css.startsWith(":root{--color-bg:#ffffff;}"), css);
        assertTrue(css.contains("@media (prefers-color-scheme: dark){:root:not([data-theme=light]){--color-bg:#0f172a;}}"), css);
        assertTrue(css.contains(":root[data-theme=dark]{--color-bg:#0f172a;}"), css);
    }

    @Test
    void aTokenCanCarryAFallbackAndAnyName() {
        assertEquals("var(--gap, 1rem)", jweb.css.Theme.var("gap", rem(1)).css());
        assertEquals("var(--brand-x)", jweb.css.Theme.var("--brand-x").css());
        assertEquals(":root{--brand-x:red;}", jweb.css.Theme.tokens().token("brand-x", "red").css());
    }

    @Test
    void aThemeRidesThePageStylesheet() {
        String css = stylesheet().add(jweb.css.Theme.light().color("bg", hex("#fff"))).build();
        assertEquals(":root{--color-bg:#fff;}", css);
    }

    // ==================== New properties ====================

    @Test
    void theMissingModernProperties() {
        assertEquals("field-sizing: content;", style().fieldSizing(content).build());
        assertEquals("scrollbar-color: #94a3b8 transparent;",
            style().scrollbarColor(hex("#94a3b8"), transparent).build());
        assertEquals("scrollbar-width: thin;", style().scrollbarWidth(thin).build());
        assertEquals("scrollbar-gutter: stable;", style().scrollbarGutter(stable).build());
        assertEquals("scrollbar-gutter: stable both-edges;",
            style().scrollbarGutter(stableBothEdges).build());
        assertEquals("text-box-trim: trim-both;", style().textBoxTrim(trimBoth).build());
        assertEquals("text-box-edge: cap alphabetic;", style().textBoxEdge(capAlphabetic).build());
        assertEquals("text-box: trim-both cap alphabetic;",
            style().textBox(trimBoth, capAlphabetic).build());
        assertEquals("anchor-scope: all;", style().anchorScope("all").build());
        assertEquals("view-transition-class: card;", style().viewTransitionClass("card").build());
        assertEquals("interpolate-size: allow-keywords;",
            style().interpolateSize(allowKeywords).build());
    }

    @Test
    void singleValueBorderSides() {
        assertEquals("border-right: none;", style().borderRight(none).build());
        assertEquals("border-left: none;", style().borderLeft(none).build());
        assertEquals("border-top: none;", style().borderTop(none).build());
        assertEquals("border-bottom: none;", style().borderBottom(none).build());
    }

    @Test
    void startingStyleOnAStylesheet() {
        assertEquals("@starting-style{.toast{opacity: 0.0;}}",
            stylesheet().startingStyle(".toast", style().opacity(0)).build());
        assertEquals("@starting-style{.a{opacity: 0.0;}.b{opacity: 0.0;}}",
            stylesheet().startingStyle(
                jweb.css.Rule.of(".a", style().opacity(0)),
                jweb.css.Rule.of(".b", style().opacity(0))).build());
    }

    @Test
    void popoverOpenOnTheSelectorBuilder() {
        assertEquals(".menu:popover-open", jweb.css.Selectors.cls("menu").popoverOpen().toString());
        assertEquals("details:open", jweb.css.Selectors.tag("details").open().toString());
    }

    @Test
    void containerStyleQueries() {
        String css = jweb.css.ContainerQuery.container("card")
            .style("variant", "featured")
            .rule(".title", style().fontWeight(700))
            .build();
        assertTrue(css.startsWith("@container card style(--variant: featured) {"), css);
    }

    @Test
    void aQueryCanBeAskedForItsConditionsWithoutItsRules() {
        assertEquals("@media (min-width: 768px)", md().query());
        assertEquals("@container card (min-width: 400px)",
            jweb.css.ContainerQuery.container("card").minWidth(px(400)).query());
    }
}
