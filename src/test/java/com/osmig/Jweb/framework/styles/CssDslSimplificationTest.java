package com.osmig.Jweb.framework.styles;

import jweb.css.Supports;
import jweb.css.Keyframes;
import jweb.css.ContainerQuery;
import jweb.css.MediaQuery;
import jweb.css.Stylesheet;
import jweb.css.Rule;
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
        String css = style()
            .hover(style().color("red"))
            .placeholder(style().color("gray"))
            .before("x", style().color("blue"))
            .after("y", style().color("blue"))
            .variantCss("&");

        assertTrue(css.contains("&:hover{color: red;}"), css);
        assertFalse(css.matches("(?s).*[^:]:placeholder\\{.*"),
            "placeholder is a pseudo-ELEMENT and must not use a single colon: " + css);
        assertTrue(css.contains("&::placeholder{color: gray;}"), css);
        assertTrue(css.contains("&::before{content: \"x\";"), css);
        assertTrue(css.contains("&::after{content: \"y\";"), css);
    }

    @Test
    void anyOtherPseudoIsWrittenWithItsOwnColons() {
        String css = style()
            .on(":nth-of-type(2n)", style().background("gray"))
            .on("::first-line", style().fontWeight(700))
            .variantCss("&");

        assertTrue(css.contains("&:nth-of-type(2n){background: gray;}"), css);
        assertFalse(css.contains("::nth-of-type"), css);
        assertTrue(css.contains("&::first-line{font-weight: 700;}"), css);
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

    // ==================== Museum-app audit gaps ====================

    @Test
    void svgPresentationPropertiesSerialize() {
        assertEquals(
            "fill: none; stroke: currentColor; stroke-width: 2px; stroke-dasharray: 240; "
                + "stroke-dashoffset: 0; stroke-linecap: round; stroke-linejoin: round; "
                + "transform-box: fill-box;",
            style()
                .fill("none")
                .stroke("currentColor")
                .strokeWidth(px(2))
                .strokeDasharray("240")
                .strokeDashoffset("0")
                .strokeLinecap("round")
                .strokeLinejoin("round")
                .transformBox("fill-box")
                .build());

        // CSSValue overloads coexist with the String ones.
        assertEquals("fill: #f00; stroke-width: 2px;",
            style().fill(CSSColors.red).strokeWidth(px(2)).build());
    }

    @Test
    void fontJustifySelfTextRenderingAndClipAreFirstClass() {
        assertEquals("font: italic bold 14px/1.5 Georgia, serif;",
            style().font("italic bold 14px/1.5 Georgia, serif").build());
        assertEquals("justify-self: end;", style().justifySelf("end").build());
        assertEquals("text-rendering: optimizeLegibility;",
            style().textRendering("optimizeLegibility").build());
        assertEquals("clip: rect(0, 0, 0, 0);", style().clip("rect(0, 0, 0, 0)").build());
    }

    /** The general escape hatch — distinct name from {@code prop}, same behavior. */
    @Test
    void propertyIsTheGeneralEscapeHatch() {
        assertEquals(style().prop("interpolate-size", "allow-keywords").build(),
            style().property("interpolate-size", "allow-keywords").build());
        assertEquals("gap: 8px;", style().property("gap", px(8)).build());
    }

    /** backdropFilter must emit the -webkit- twin immediately before the unprefixed property. */
    @Test
    void backdropFilterEmitsTheWebkitTwinRightBeforeTheUnprefixedProperty() {
        String css = style().backdropFilter("blur(10px)").build();
        assertEquals("-webkit-backdrop-filter: blur(10px); backdrop-filter: blur(10px);", css);

        // The typed (CSSValue...) overload already did this — String must match it.
        String typed = style().backdropFilter(CSS.blur(px(10))).build();
        assertEquals(css, typed);
    }

    /** The cross-document View Transitions opt-in at-rule. */
    @Test
    void viewTransitionsAtRuleSerializesExactly() {
        assertEquals("@view-transition{navigation:auto}", ViewTransitions.viewTransitions().build());
        assertEquals("@view-transition{navigation:none}",
            ViewTransitions.viewTransitions().navigation("none").build());

        String css = Stylesheet.stylesheet().add(ViewTransitions.viewTransitions()).build();
        assertEquals("@view-transition{navigation:auto}", css);
    }

    // ==================== Modern-CSS fold-in — the ten stranded modules ====

    /** CSSAnchorPositioning — declarations are Style, functions are CSSValue. */
    @Test
    void anchorPositioningFamilyIsTypedNotStrings() {
        assertEquals("anchor-name: --menu;", CSSAnchorPositioning.anchorName("--menu").build());
        assertEquals("position-area: bottom;", CSSAnchorPositioning.positionArea("bottom").build());
        assertEquals("anchor(--btn bottom)", CSSAnchorPositioning.anchor("--btn", "bottom").css());
        assertEquals("anchor-size(--btn width)", CSSAnchorPositioning.anchorSize("--btn", "width").css());

        // Composes with a real Style property directly — no more prop(String) bridge.
        assertEquals("top: anchor(--btn bottom);",
            style().top(CSSAnchorPositioning.anchor("--btn", "bottom")).build());
    }

    /** CSSTextWrap — every declaration factory now returns a Style fragment. */
    @Test
    void textWrapFamilyReturnsStyleFragments() {
        assertEquals("text-wrap: balance;", CSSTextWrap.textWrapBalance().build());
        assertEquals("word-break: break-all;", CSSTextWrap.wordBreakAll().build());
        assertEquals(
            "display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 3; overflow: hidden;",
            CSSTextWrap.lineClamp(3).build());

        assertEquals("text-wrap: balance; color: red;",
            style().apply(CSSTextWrap.textWrapBalance()).color("red").build());
    }

    /** CSSMasking — clip-path shapes are still complete declarations, now typed. */
    @Test
    void maskingClipPathFamilyEmitsCompleteDeclarations() {
        assertEquals("clip-path: circle(50%);", CSSMasking.clipCircle("50%").build());
        assertEquals("clip-path: polygon(50% 0%,0% 100%,100% 100%);",
            CSSMasking.clipPolygon("50% 0%", "0% 100%", "100% 100%").build());
        assertEquals("-webkit-mask-image: url(m.svg); mask-image: url(m.svg);",
            CSSMasking.maskImage("url(m.svg)").build());
    }

    /** CSSLogicalProperties — writing-mode-aware declarations, plus CSSValue overloads. */
    @Test
    void logicalPropertiesFamilyEmitsDeclarations() {
        assertEquals("margin-inline: auto;", CSSLogicalProperties.marginInline("auto").build());
        assertEquals("inline-size: 50%;", CSSLogicalProperties.inlineSize(percent(50)).build());
        assertEquals("padding-block: 1rem 2rem;",
            CSSLogicalProperties.paddingBlock(rem(1), rem(2)).build());
    }

    /** CSSScrollSnap — real property names (scroll-padding, not scroll-snap-padding). */
    @Test
    void scrollSnapFamilyEmitsDeclarations() {
        assertEquals("scroll-snap-align: start;", CSSScrollSnap.snapAlign("start").build());
        assertEquals("scroll-padding: 0 20px;", CSSScrollSnap.snapPadding("0 20px").build());
        assertEquals("scroll-padding: 0 20px;", CSSScrollSnap.snapPadding(jweb.CSSValue.of("0 20px")).build());
    }

    /** CSSSubgrid — subgrid values as typed declarations. */
    @Test
    void subgridFamilyEmitsDeclarations() {
        assertEquals("grid-template-columns: subgrid;", CSSSubgrid.subgridColumns().build());
        assertEquals("grid-template-columns: subgrid; grid-template-rows: subgrid;",
            CSSSubgrid.subgridBoth().build());
        assertEquals("grid-column: 1 / -1;", CSSSubgrid.gridColumnFull().build());
    }

    /** C1(b) — the chain: import static jweb.Css.*; reaches every stranded module now. */
    @Test
    void oneImportReachesAllTenModules() {
        assertEquals("text-wrap: balance;", jweb.Css.textWrapBalance().build());
        assertEquals("anchor-name: --menu;", jweb.Css.anchorName("--menu").build());
        assertEquals("grid-template-columns: subgrid;", jweb.Css.subgridColumns().build());
        assertEquals("scroll-snap-align: start;", jweb.Css.snapAlign("start").build());
        assertEquals("clip-path: circle(50%);", jweb.Css.clipCircle("50%").build());
        assertEquals("margin-inline: auto;", jweb.Css.marginInline("auto").build());
    }

    /** CSSNested.rule renamed to nest — CSS.rule kept the name (C1 rename). */
    @Test
    void nestedRuleStarterIsNamedNestNotRule() {
        assertEquals(".card {\n  padding: 1rem;\n}\n", CSSNested.nest(".card").prop("padding", "1rem").build());
        assertTrue(java.util.Arrays.stream(CSSNested.class.getMethods())
                .noneMatch(m -> m.getName().equals("rule")),
            "CSSNested.rule must be gone — renamed to nest() to stop clashing with CSS.rule");
    }

    /** CSSProperty's syntax helpers renamed to avoid clashing with unit/colour statics. */
    @Test
    void propertySyntaxHelpersAreRenamedWithPropertySuffix() {
        assertEquals("@property --hue {\n  syntax: '<number>';\n  inherits: true;\n  initial-value: 210;\n}",
            CSSProperty.numberProperty("--hue").inherits(true).initialValue("210").build());
        for (String gone : new String[] {"color", "length", "number", "percentage", "integer", "angle", "time", "image"}) {
            assertTrue(java.util.Arrays.stream(CSSProperty.class.getMethods())
                    .noneMatch(m -> m.getName().equals(gone)),
                "CSSProperty." + gone + "(String) must be gone — renamed to " + gone + "Property");
        }
    }

    /** CSSAnchorPositioning.top/right/bottom/left(String) were redundant one-liners — deleted. */
    @Test
    void anchorPositioningInsetAliasesAreGone() {
        for (String gone : new String[] {"top", "right", "bottom", "left"}) {
            assertTrue(java.util.Arrays.stream(CSSAnchorPositioning.class.getMethods())
                    .noneMatch(m -> m.getName().equals(gone)),
                "CSSAnchorPositioning." + gone + "(String) must be gone — redundant with style()." + gone + "(anchor(...))");
        }
    }

    /** CSSSubgrid's grid-template/placement String setters were redundant with Style's own — deleted. */
    @Test
    void subgridRedundantGridSettersAreGone() {
        for (String gone : new String[] {"gridTemplateColumns", "gridTemplateRows", "gridColumn", "gridRow"}) {
            assertTrue(java.util.Arrays.stream(CSSSubgrid.class.getMethods())
                    .noneMatch(m -> m.getName().equals(gone)),
                "CSSSubgrid." + gone + "(String) must be gone — redundant with the Style instance method");
        }
    }
}
