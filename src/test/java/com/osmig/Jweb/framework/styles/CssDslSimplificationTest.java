package com.osmig.Jweb.framework.styles;

import org.junit.jupiter.api.Test;

import static com.osmig.Jweb.framework.styles.CSS.rule;
import static com.osmig.Jweb.framework.styles.CSS.style;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the CSS-DSL simplification pass: the X2 String overloads, the C3
 * column-gap consolidation, the C4 shared {@link Rule}, the C12 pseudo-element
 * colon fix and the C9 unit additions.
 */
class CssDslSimplificationTest {

    // ==================== X2 — String value overloads ====================

    @Test
    void stringOverloadsEmitExactlyTheValuesGiven() {
        assertEquals(
            "cursor: copy; display: flex; margin: 0 auto;",
            style().cursor("copy").display("flex").margin("0 auto").build());
    }

    @Test
    void stringOverloadsCoverTheSingleValueShorthands() {
        assertEquals(
            "padding: 1rem 2rem; inset: 0; border: 1px solid red; gap: 8px; "
                + "border-radius: 4px 4px 0 0; background: url(a.png) no-repeat; "
                + "animation: spin 2s linear infinite;",
            style()
                .padding("1rem 2rem")
                .inset("0")
                .border("1px solid red")
                .gap("8px")
                .borderRadius("4px 4px 0 0")
                .background("url(a.png) no-repeat")
                .animation("spin 2s linear infinite")
                .build());
    }

    /** C5 — transition composition without the transitions()/trans() ceremony. */
    @Test
    void transitionAcceptsAFullCssTransitionList() {
        assertEquals(
            "transition: color .2s ease, transform .3s ease-out;",
            style().transition("color .2s ease, transform .3s ease-out").build());
    }

    @Test
    void stringAndTypedOverloadsCoexist() {
        assertEquals("width: 100%; height: 50px;",
            style().width("100%").height(px(50)).build(),
            "typed and String overloads must both resolve");
    }

    @Test
    void numericAndStringOverloadsCoexist() {
        assertEquals("z-index: auto; font-weight: 700; line-height: 1.5; tab-size: 4;",
            style().zIndex("auto").fontWeight(700).lineHeight(1.5).tabSize(4).build());
    }

    /** C17 — the grid String overloads are the CSS-parity path, not deprecated. */
    @Test
    void gridStringOverloadsWork() {
        assertEquals(
            "grid-template-columns: repeat(3, 1fr); grid-template-rows: auto 1fr; "
                + "grid-column: 1 / 3; grid-row: span 2; grid-area: header;",
            style()
                .gridTemplateColumns("repeat(3, 1fr)")
                .gridTemplateRows("auto 1fr")
                .gridColumn("1 / 3")
                .gridRow("span 2")
                .gridArea("header")
                .build());
    }

    /** C16 — prop(name, value) is the blessed raw escape hatch. */
    @Test
    void propIsTheRawEscapeHatch() {
        assertEquals("container-type: inline-size;",
            style().prop("container-type", "inline-size").build());
    }

    /** X2(a) — content(String) is the one overload that does not pass through verbatim. */
    @Test
    void contentStringQuotesItsArgumentUnlikeEveryOtherStringOverload() {
        assertEquals("content: '→';", style().content("→").build());
        assertEquals("content: none;", style().prop("content", "none").build());
    }

    // ==================== C1 — promoted string-module properties ====================

    @Test
    void anchorPositioningPropertiesAreFirstClass() {
        assertEquals(
            "anchor-name: --menu; position-anchor: --menu; position-area: bottom; "
                + "position-visibility: anchors-visible; position-try-fallbacks: flip-block, flip-inline;",
            style()
                .anchorName("--menu")
                .positionAnchor("--menu")
                .positionArea("bottom")
                .positionVisibility("anchors-visible")
                .positionTryFallbacks("flip-block, flip-inline")
                .build());
    }

    @Test
    void whiteSpaceCollapseIsFirstClass() {
        assertEquals("white-space-collapse: preserve;",
            style().whiteSpaceCollapse("preserve").build());
    }

    /** lineClamp emits the standard property plus the -webkit-box fallback quartet. */
    @Test
    void lineClampEmitsStandardPropertyAndWebkitFallback() {
        assertEquals(
            "display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 3; "
                + "line-clamp: 3; overflow: hidden;",
            style().lineClamp(3).build());
    }

    @Test
    void subgridConstantFeedsTheGridProperties() {
        assertEquals("grid-template-columns: subgrid; grid-template-rows: subgrid;",
            style().gridTemplateColumns(CSSGrid.subgrid).gridTemplateRows(CSSGrid.subgrid).build());
    }

    // ==================== C3 — column-gap consolidation ====================

    @Test
    void columnGapIsTheOnlyColumnGapSetter() {
        assertEquals("column-gap: 2rem;", style().columnGap(rem(2)).build());
        assertEquals("column-gap: 2rem;", style().columnGap("2rem").build());

        // columnGapMulti set the very same property and no longer exists.
        assertTrue(java.util.Arrays.stream(jweb.Style.class.getMethods())
                .noneMatch(m -> m.getName().equals("columnGapMulti")),
            "columnGapMulti must be gone");
    }

    @Test
    void colorMixIsOneFamilyInCssColors() {
        assertEquals("color-mix(in srgb, #f00 50%, #00f)",
            CSSColors.colorMix(CSSColors.red, CSSColors.blue, 50).css());
        assertEquals("color-mix(in oklch, #f00 30%, #00f)",
            CSSColors.colorMix("oklch", CSSColors.red, CSSColors.blue, 30).css());
    }

    // ==================== C4 — one Rule for every at-rule builder ====================

    @Test
    void ruleOfRoundTripsThroughMediaAndSupports() {
        String media = MediaQuery.media().minWidth(px(768))
            .rules(Rule.of(".container", style().maxWidth(px(720))))
            .build();
        assertTrue(media.contains("@media (min-width: 768px)"), media);
        assertTrue(media.contains(".container"), media);
        assertTrue(media.contains("max-width: 720px;"), media);

        String supports = Supports.supports("display", "grid")
            .rules(Rule.of(".grid", style().display("grid")))
            .build();
        assertEquals("@supports (display: grid) {\n  .grid { display: grid; }\n}", supports);
    }

    @Test
    void ruleOfRoundTripsThroughContainerQueries() {
        String css = ContainerQuery.container("card").minWidth(px(400))
            .rules(Rule.of(".card-content", style().display("flex")))
            .build();
        assertTrue(css.contains("@container card (min-width: 400px)"), css);
        assertTrue(css.contains("display: flex;"), css);
    }

    @Test
    void scopeTakesTheSameSelectorStylePairAsEveryOtherBuilder() {
        String css = CSSScope.scope(".card").rule("h2", style().fontSize(rem(1.5))).build();
        assertTrue(css.startsWith("@scope (.card) {"), css);
        assertTrue(css.contains("h2"), css);
        assertTrue(css.contains("font-size: 1.5rem"), css);
    }

    @Test
    void stylesheetAcceptsRuleOf() {
        String css = Stylesheet.stylesheet().rule(Rule.of(".card", style().padding(rem(1)))).build();
        assertTrue(css.contains(".card{padding: 1rem;}") || css.contains(".card"), css);
        assertTrue(css.contains("padding: 1rem;"), css);
    }

    /** MediaQuery.and() was a documented no-op and is gone. */
    @Test
    void mediaQueryHasNoAndNoOp() {
        assertTrue(java.util.Arrays.stream(MediaQuery.class.getMethods())
                .noneMatch(m -> m.getName().equals("and")),
            "MediaQuery.and() must be gone");
    }

    // ==================== C9 — units ====================

    @Test
    void newAbsoluteAndResolutionUnitsEmitTheirCssUnit() {
        assertEquals("12pt", pt(12).css());
        assertEquals("2.5cm", cm(2.5).css());
        assertEquals("10mm", mm(10).css());
        assertEquals("4Q", q(4).css());
        assertEquals("8.5in", inch(8.5).css());
        assertEquals("192dpi", dpi(192).css());
        assertEquals("2dppx", dppx(2).css());
    }

    @Test
    void msAcceptsFractionalValues() {
        assertEquals("300ms", ms(300).css());
        assertEquals("16.5ms", ms(16.5).css());
    }

    @Test
    void resolutionUnitsFeedMediaQueries() {
        assertTrue(MediaQuery.media().minResolution(dppx(2)).build()
            .startsWith("@media (min-resolution: 2dppx)"));
        assertTrue(MediaQuery.media().maxResolution(dpi(192)).build()
            .startsWith("@media (max-resolution: 192dpi)"));
    }

    /** retina() emitted the legacy -webkit-min-device-pixel-ratio. */
    @Test
    void retinaEmitsTheStandardResolutionQuery() {
        assertTrue(MediaQuery.media().retina().build().startsWith("@media (min-resolution: 2dppx)"),
            MediaQuery.media().retina().build());
    }

    // ==================== C12 — pseudo-element vs pseudo-class colons ====================

    @Test
    void pseudoElementsGetDoubleColonsAndPseudoClassesGetSingle() {
        String html = new StyledElement(com.osmig.Jweb.framework.vdom.VElement.of("input"))
            .hover(style().color("red"))
            .placeholder(style().color("gray"))
            .before(style().content("x"))
            .after(style().content("y"))
            .toHtml();

        assertTrue(html.contains(":hover{color: red;}"), html);
        assertFalse(html.matches("(?s).*[^:]:placeholder\\{.*"),
            "placeholder is a pseudo-ELEMENT and must not use a single colon: " + html);
        assertTrue(html.contains("::placeholder{color: gray;}"), html);
        assertTrue(html.contains("::before{"), html);
        assertTrue(html.contains("::after{"), html);
    }

    @Test
    void genericPseudoEscapeHatchesWork() {
        String html = new StyledElement(com.osmig.Jweb.framework.vdom.VElement.of("div"))
            .pseudo("nth-of-type(2n)", style().background("gray"))
            .pseudoElement("first-line", style().fontWeight(700))
            .toHtml();

        assertTrue(html.contains(":nth-of-type(2n){background: gray;}"), html);
        assertFalse(html.contains("::nth-of-type"), html);
        assertTrue(html.contains("::first-line{font-weight: 700;}"), html);
    }

    // ==================== C14 — BEM emits flat rules ====================

    @Test
    void bemBlockEmitsFlatRulesNotUnresolvableAmpersandNesting() {
        String css = CSSNested.block("card")
            .prop("padding", "1rem")
            .element("header").prop("font-weight", "bold")
            .modifier("featured").prop("border", "2px solid gold")
            .build();

        assertFalse(css.contains("&__"), "native CSS nesting cannot resolve &__: " + css);
        assertTrue(css.contains(".card__header"), css);
        assertTrue(css.contains(".card--featured"), css);
    }

    // ==================== C7 — animation presets are keyframe-backed ====================

    @Test
    void spinPresetMatchesTheKeyframesItNeeds() {
        assertEquals("spin 2s", CSSAnimations.spin(s(2)).css());
        assertTrue(Keyframes.spin().build().contains("@keyframes spin"));
    }

    @Test
    void unbackedAnimationPresetsAreGone() {
        for (String gone : new String[] {"fadeInUp", "slideOutLeft", "flipX", "jello", "tada"}) {
            assertTrue(java.util.Arrays.stream(CSSAnimations.class.getMethods())
                    .noneMatch(m -> m.getName().equals(gone)),
                gone + " animated nothing and must be gone");
        }
    }

    // ==================== C8 — AnimationBuilder drops nothing silently =============

    @Test
    void animationBuilderHasNoSilentlyDroppedTimeline() {
        assertTrue(java.util.Arrays.stream(CSSAnimations.AnimationBuilder.class.getMethods())
                .noneMatch(m -> m.getName().equals("timeline")),
            "timeline() was never emitted by css() and must be gone");
        assertEquals("animation-timeline: scroll();",
            style().animationTimeline("scroll()").build());
    }

    // ==================== C13 — dropped-from-spec at-rules =========================

    @Test
    void droppedPositionFallbackSyntaxIsGone() {
        for (String gone : new String[] {"positionFallback", "tryTactic"}) {
            assertTrue(java.util.Arrays.stream(CSSAnchorPositioning.class.getMethods())
                    .noneMatch(m -> m.getName().equals(gone)),
                gone + " emits syntax dropped from the spec and must be gone");
        }
        assertEquals("@position-try --flip-up {\n  bottom: anchor(top); top: auto;\n}",
            CSSAnchorPositioning.positionTry("--flip-up",
                style().bottom("anchor(top)").top("auto")));
    }

    // ==================== rule() still composes with the String overloads =========

    @Test
    void ruleBuilderInheritsTheStringOverloads() {
        assertEquals(".btn { display: inline-flex; padding: 8px 16px; cursor: pointer; }",
            rule(".btn").display("inline-flex").padding("8px 16px").cursor("pointer").toRule());
    }
}
