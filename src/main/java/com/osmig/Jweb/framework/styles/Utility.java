package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.List;

/**
 * Tailwind-style utility classes for rapid styling.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.Utility.*;
 *
 * // Build a class string
 * String classes = util()
 *     .flex().itemsCenter().justifyBetween()
 *     .p(4).m(2)
 *     .bgPrimary().textWhite()
 *     .roundedLg()
 *     .shadowMd()
 *     .hoverBgPrimaryDark()
 *     .build();
 *
 * div().className(classes).children(...)
 *
 * // Or use with Theme
 * Theme theme = Theme.preset();
 * String css = Utility.generateCss(theme);
 * </pre>
 */
public class Utility {

    private Utility() {}

    /**
     * Creates a new utility class builder.
     */
    public static Builder util() {
        return new Builder();
    }

    /**
     * Shorthand for creating utility classes.
     */
    public static Builder classes() {
        return new Builder();
    }

    /**
     * Builder for composing utility classes.
     */
    public static class Builder {
        private final List<String> classes = new ArrayList<>();

        // ==================== Display ====================

        public Builder block() { classes.add("block"); return this; }
        public Builder inlineBlock() { classes.add("inline-block"); return this; }
        public Builder inline() { classes.add("inline"); return this; }
        public Builder flex() { classes.add("flex"); return this; }
        public Builder inlineFlex() { classes.add("inline-flex"); return this; }
        public Builder grid() { classes.add("grid"); return this; }
        public Builder inlineGrid() { classes.add("inline-grid"); return this; }
        public Builder hidden() { classes.add("hidden"); return this; }
        public Builder contents() { classes.add("contents"); return this; }

        // ==================== Flexbox ====================

        public Builder flexRow() { classes.add("flex-row"); return this; }
        public Builder flexRowReverse() { classes.add("flex-row-reverse"); return this; }
        public Builder flexCol() { classes.add("flex-col"); return this; }
        public Builder flexColReverse() { classes.add("flex-col-reverse"); return this; }
        public Builder flexWrap() { classes.add("flex-wrap"); return this; }
        public Builder flexWrapReverse() { classes.add("flex-wrap-reverse"); return this; }
        public Builder flexNowrap() { classes.add("flex-nowrap"); return this; }
        public Builder flex1() { classes.add("flex-1"); return this; }
        public Builder flexAuto() { classes.add("flex-auto"); return this; }
        public Builder flexInitial() { classes.add("flex-initial"); return this; }
        public Builder flexNone() { classes.add("flex-none"); return this; }
        public Builder grow() { classes.add("grow"); return this; }
        public Builder grow0() { classes.add("grow-0"); return this; }
        public Builder shrink() { classes.add("shrink"); return this; }
        public Builder shrink0() { classes.add("shrink-0"); return this; }

        // ==================== Grid ====================

        public Builder gridCols(int n) { classes.add("grid-cols-" + n); return this; }
        public Builder gridRows(int n) { classes.add("grid-rows-" + n); return this; }
        public Builder colSpan(int n) { classes.add("col-span-" + n); return this; }
        public Builder colSpanFull() { classes.add("col-span-full"); return this; }
        public Builder rowSpan(int n) { classes.add("row-span-" + n); return this; }
        public Builder rowSpanFull() { classes.add("row-span-full"); return this; }

        // ==================== Alignment ====================

        public Builder justifyStart() { classes.add("justify-start"); return this; }
        public Builder justifyEnd() { classes.add("justify-end"); return this; }
        public Builder justifyCenter() { classes.add("justify-center"); return this; }
        public Builder justifyBetween() { classes.add("justify-between"); return this; }
        public Builder justifyAround() { classes.add("justify-around"); return this; }
        public Builder justifyEvenly() { classes.add("justify-evenly"); return this; }

        public Builder itemsStart() { classes.add("items-start"); return this; }
        public Builder itemsEnd() { classes.add("items-end"); return this; }
        public Builder itemsCenter() { classes.add("items-center"); return this; }
        public Builder itemsBaseline() { classes.add("items-baseline"); return this; }
        public Builder itemsStretch() { classes.add("items-stretch"); return this; }

        public Builder contentStart() { classes.add("content-start"); return this; }
        public Builder contentEnd() { classes.add("content-end"); return this; }
        public Builder contentCenter() { classes.add("content-center"); return this; }
        public Builder contentBetween() { classes.add("content-between"); return this; }
        public Builder contentAround() { classes.add("content-around"); return this; }
        public Builder contentEvenly() { classes.add("content-evenly"); return this; }

        public Builder selfAuto() { classes.add("self-auto"); return this; }
        public Builder selfStart() { classes.add("self-start"); return this; }
        public Builder selfEnd() { classes.add("self-end"); return this; }
        public Builder selfCenter() { classes.add("self-center"); return this; }
        public Builder selfStretch() { classes.add("self-stretch"); return this; }

        public Builder placeContentCenter() { classes.add("place-content-center"); return this; }
        public Builder placeItemsCenter() { classes.add("place-items-center"); return this; }

        // ==================== Gap ====================

        public Builder gap(int n) { classes.add("gap-" + n); return this; }
        public Builder gapX(int n) { classes.add("gap-x-" + n); return this; }
        public Builder gapY(int n) { classes.add("gap-y-" + n); return this; }

        // ==================== Padding ====================

        public Builder p(int n) { classes.add("p-" + n); return this; }
        public Builder px(int n) { classes.add("px-" + n); return this; }
        public Builder py(int n) { classes.add("py-" + n); return this; }
        public Builder pt(int n) { classes.add("pt-" + n); return this; }
        public Builder pr(int n) { classes.add("pr-" + n); return this; }
        public Builder pb(int n) { classes.add("pb-" + n); return this; }
        public Builder pl(int n) { classes.add("pl-" + n); return this; }

        // ==================== Margin ====================

        public Builder m(int n) { classes.add("m-" + n); return this; }
        public Builder mx(int n) { classes.add("mx-" + n); return this; }
        public Builder my(int n) { classes.add("my-" + n); return this; }
        public Builder mt(int n) { classes.add("mt-" + n); return this; }
        public Builder mr(int n) { classes.add("mr-" + n); return this; }
        public Builder mb(int n) { classes.add("mb-" + n); return this; }
        public Builder ml(int n) { classes.add("ml-" + n); return this; }
        public Builder mAuto() { classes.add("m-auto"); return this; }
        public Builder mxAuto() { classes.add("mx-auto"); return this; }
        public Builder myAuto() { classes.add("my-auto"); return this; }

        // Negative margins
        public Builder mNeg(int n) { classes.add("-m-" + n); return this; }
        public Builder mtNeg(int n) { classes.add("-mt-" + n); return this; }
        public Builder mrNeg(int n) { classes.add("-mr-" + n); return this; }
        public Builder mbNeg(int n) { classes.add("-mb-" + n); return this; }
        public Builder mlNeg(int n) { classes.add("-ml-" + n); return this; }

        // ==================== Width & Height ====================

        public Builder w(int n) { classes.add("w-" + n); return this; }
        public Builder wFull() { classes.add("w-full"); return this; }
        public Builder wScreen() { classes.add("w-screen"); return this; }
        public Builder wMin() { classes.add("w-min"); return this; }
        public Builder wMax() { classes.add("w-max"); return this; }
        public Builder wFit() { classes.add("w-fit"); return this; }
        public Builder wAuto() { classes.add("w-auto"); return this; }
        public Builder wHalf() { classes.add("w-1/2"); return this; }
        public Builder wThird() { classes.add("w-1/3"); return this; }
        public Builder wTwoThirds() { classes.add("w-2/3"); return this; }
        public Builder wQuarter() { classes.add("w-1/4"); return this; }
        public Builder wThreeQuarters() { classes.add("w-3/4"); return this; }

        public Builder h(int n) { classes.add("h-" + n); return this; }
        public Builder hFull() { classes.add("h-full"); return this; }
        public Builder hScreen() { classes.add("h-screen"); return this; }
        public Builder hMin() { classes.add("h-min"); return this; }
        public Builder hMax() { classes.add("h-max"); return this; }
        public Builder hFit() { classes.add("h-fit"); return this; }
        public Builder hAuto() { classes.add("h-auto"); return this; }

        public Builder minW0() { classes.add("min-w-0"); return this; }
        public Builder minWFull() { classes.add("min-w-full"); return this; }
        public Builder maxWNone() { classes.add("max-w-none"); return this; }
        public Builder maxWSm() { classes.add("max-w-sm"); return this; }
        public Builder maxWMd() { classes.add("max-w-md"); return this; }
        public Builder maxWLg() { classes.add("max-w-lg"); return this; }
        public Builder maxWXl() { classes.add("max-w-xl"); return this; }
        public Builder maxW2xl() { classes.add("max-w-2xl"); return this; }
        public Builder maxW3xl() { classes.add("max-w-3xl"); return this; }
        public Builder maxW4xl() { classes.add("max-w-4xl"); return this; }
        public Builder maxW5xl() { classes.add("max-w-5xl"); return this; }
        public Builder maxW6xl() { classes.add("max-w-6xl"); return this; }
        public Builder maxW7xl() { classes.add("max-w-7xl"); return this; }
        public Builder maxWFull() { classes.add("max-w-full"); return this; }
        public Builder maxWProse() { classes.add("max-w-prose"); return this; }

        public Builder minH0() { classes.add("min-h-0"); return this; }
        public Builder minHFull() { classes.add("min-h-full"); return this; }
        public Builder minHScreen() { classes.add("min-h-screen"); return this; }

        // ==================== Position ====================

        public Builder static_() { classes.add("static"); return this; }
        public Builder fixed() { classes.add("fixed"); return this; }
        public Builder absolute() { classes.add("absolute"); return this; }
        public Builder relative() { classes.add("relative"); return this; }
        public Builder sticky() { classes.add("sticky"); return this; }

        public Builder inset(int n) { classes.add("inset-" + n); return this; }
        public Builder inset0() { classes.add("inset-0"); return this; }
        public Builder insetX(int n) { classes.add("inset-x-" + n); return this; }
        public Builder insetY(int n) { classes.add("inset-y-" + n); return this; }
        public Builder top(int n) { classes.add("top-" + n); return this; }
        public Builder right(int n) { classes.add("right-" + n); return this; }
        public Builder bottom(int n) { classes.add("bottom-" + n); return this; }
        public Builder left(int n) { classes.add("left-" + n); return this; }
        public Builder top0() { classes.add("top-0"); return this; }
        public Builder right0() { classes.add("right-0"); return this; }
        public Builder bottom0() { classes.add("bottom-0"); return this; }
        public Builder left0() { classes.add("left-0"); return this; }

        public Builder z(int n) { classes.add("z-" + n); return this; }
        public Builder z0() { classes.add("z-0"); return this; }
        public Builder z10() { classes.add("z-10"); return this; }
        public Builder z20() { classes.add("z-20"); return this; }
        public Builder z30() { classes.add("z-30"); return this; }
        public Builder z40() { classes.add("z-40"); return this; }
        public Builder z50() { classes.add("z-50"); return this; }
        public Builder zAuto() { classes.add("z-auto"); return this; }

        // ==================== Typography ====================

        public Builder textXs() { classes.add("text-xs"); return this; }
        public Builder textSm() { classes.add("text-sm"); return this; }
        public Builder textBase() { classes.add("text-base"); return this; }
        public Builder textLg() { classes.add("text-lg"); return this; }
        public Builder textXl() { classes.add("text-xl"); return this; }
        public Builder text2xl() { classes.add("text-2xl"); return this; }
        public Builder text3xl() { classes.add("text-3xl"); return this; }
        public Builder text4xl() { classes.add("text-4xl"); return this; }
        public Builder text5xl() { classes.add("text-5xl"); return this; }
        public Builder text6xl() { classes.add("text-6xl"); return this; }
        public Builder text7xl() { classes.add("text-7xl"); return this; }
        public Builder text8xl() { classes.add("text-8xl"); return this; }
        public Builder text9xl() { classes.add("text-9xl"); return this; }

        public Builder fontThin() { classes.add("font-thin"); return this; }
        public Builder fontExtralight() { classes.add("font-extralight"); return this; }
        public Builder fontLight() { classes.add("font-light"); return this; }
        public Builder fontNormal() { classes.add("font-normal"); return this; }
        public Builder fontMedium() { classes.add("font-medium"); return this; }
        public Builder fontSemibold() { classes.add("font-semibold"); return this; }
        public Builder fontBold() { classes.add("font-bold"); return this; }
        public Builder fontExtrabold() { classes.add("font-extrabold"); return this; }
        public Builder fontBlack() { classes.add("font-black"); return this; }

        public Builder italic() { classes.add("italic"); return this; }
        public Builder notItalic() { classes.add("not-italic"); return this; }

        public Builder textLeft() { classes.add("text-left"); return this; }
        public Builder textCenter() { classes.add("text-center"); return this; }
        public Builder textRight() { classes.add("text-right"); return this; }
        public Builder textJustify() { classes.add("text-justify"); return this; }

        public Builder uppercase() { classes.add("uppercase"); return this; }
        public Builder lowercase() { classes.add("lowercase"); return this; }
        public Builder capitalize() { classes.add("capitalize"); return this; }
        public Builder normalCase() { classes.add("normal-case"); return this; }

        public Builder underline() { classes.add("underline"); return this; }
        public Builder overline() { classes.add("overline"); return this; }
        public Builder lineThrough() { classes.add("line-through"); return this; }
        public Builder noUnderline() { classes.add("no-underline"); return this; }

        public Builder leadingNone() { classes.add("leading-none"); return this; }
        public Builder leadingTight() { classes.add("leading-tight"); return this; }
        public Builder leadingSnug() { classes.add("leading-snug"); return this; }
        public Builder leadingNormal() { classes.add("leading-normal"); return this; }
        public Builder leadingRelaxed() { classes.add("leading-relaxed"); return this; }
        public Builder leadingLoose() { classes.add("leading-loose"); return this; }

        public Builder trackingTighter() { classes.add("tracking-tighter"); return this; }
        public Builder trackingTight() { classes.add("tracking-tight"); return this; }
        public Builder trackingNormal() { classes.add("tracking-normal"); return this; }
        public Builder trackingWide() { classes.add("tracking-wide"); return this; }
        public Builder trackingWider() { classes.add("tracking-wider"); return this; }
        public Builder trackingWidest() { classes.add("tracking-widest"); return this; }

        public Builder truncate() { classes.add("truncate"); return this; }
        public Builder textEllipsis() { classes.add("text-ellipsis"); return this; }
        public Builder textClip() { classes.add("text-clip"); return this; }

        public Builder whitespaceNormal() { classes.add("whitespace-normal"); return this; }
        public Builder whitespaceNowrap() { classes.add("whitespace-nowrap"); return this; }
        public Builder whitespacePre() { classes.add("whitespace-pre"); return this; }
        public Builder whitespacePreLine() { classes.add("whitespace-pre-line"); return this; }
        public Builder whitespacePreWrap() { classes.add("whitespace-pre-wrap"); return this; }

        // ==================== Colors ====================

        // Text colors
        public Builder textWhite() { classes.add("text-white"); return this; }
        public Builder textBlack() { classes.add("text-black"); return this; }
        public Builder textPrimary() { classes.add("text-primary"); return this; }
        public Builder textSecondary() { classes.add("text-secondary"); return this; }
        public Builder textMuted() { classes.add("text-muted"); return this; }
        public Builder textSuccess() { classes.add("text-success"); return this; }
        public Builder textWarning() { classes.add("text-warning"); return this; }
        public Builder textError() { classes.add("text-error"); return this; }
        public Builder textInfo() { classes.add("text-info"); return this; }
        public Builder textGray(int shade) { classes.add("text-gray-" + shade); return this; }
        public Builder textColor(String color) { classes.add("text-" + color); return this; }

        // Background colors
        public Builder bgWhite() { classes.add("bg-white"); return this; }
        public Builder bgBlack() { classes.add("bg-black"); return this; }
        public Builder bgPrimary() { classes.add("bg-primary"); return this; }
        public Builder bgSecondary() { classes.add("bg-secondary"); return this; }
        public Builder bgMuted() { classes.add("bg-muted"); return this; }
        public Builder bgSuccess() { classes.add("bg-success"); return this; }
        public Builder bgWarning() { classes.add("bg-warning"); return this; }
        public Builder bgError() { classes.add("bg-error"); return this; }
        public Builder bgInfo() { classes.add("bg-info"); return this; }
        public Builder bgTransparent() { classes.add("bg-transparent"); return this; }
        public Builder bgGray(int shade) { classes.add("bg-gray-" + shade); return this; }
        public Builder bgColor(String color) { classes.add("bg-" + color); return this; }

        // Border colors
        public Builder borderWhite() { classes.add("border-white"); return this; }
        public Builder borderBlack() { classes.add("border-black"); return this; }
        public Builder borderPrimary() { classes.add("border-primary"); return this; }
        public Builder borderMuted() { classes.add("border-muted"); return this; }
        public Builder borderGray(int shade) { classes.add("border-gray-" + shade); return this; }
        public Builder borderTransparent() { classes.add("border-transparent"); return this; }
        public Builder borderColor(String color) { classes.add("border-" + color); return this; }

        // ==================== Border ====================

        public Builder border() { classes.add("border"); return this; }
        public Builder border0() { classes.add("border-0"); return this; }
        public Builder border2() { classes.add("border-2"); return this; }
        public Builder border4() { classes.add("border-4"); return this; }
        public Builder border8() { classes.add("border-8"); return this; }
        public Builder borderT() { classes.add("border-t"); return this; }
        public Builder borderR() { classes.add("border-r"); return this; }
        public Builder borderB() { classes.add("border-b"); return this; }
        public Builder borderL() { classes.add("border-l"); return this; }

        public Builder borderSolid() { classes.add("border-solid"); return this; }
        public Builder borderDashed() { classes.add("border-dashed"); return this; }
        public Builder borderDotted() { classes.add("border-dotted"); return this; }
        public Builder borderDouble() { classes.add("border-double"); return this; }
        public Builder borderNone() { classes.add("border-none"); return this; }

        // ==================== Border Radius ====================

        public Builder roundedNone() { classes.add("rounded-none"); return this; }
        public Builder roundedSm() { classes.add("rounded-sm"); return this; }
        public Builder rounded() { classes.add("rounded"); return this; }
        public Builder roundedMd() { classes.add("rounded-md"); return this; }
        public Builder roundedLg() { classes.add("rounded-lg"); return this; }
        public Builder roundedXl() { classes.add("rounded-xl"); return this; }
        public Builder rounded2xl() { classes.add("rounded-2xl"); return this; }
        public Builder rounded3xl() { classes.add("rounded-3xl"); return this; }
        public Builder roundedFull() { classes.add("rounded-full"); return this; }
        public Builder roundedT(String size) { classes.add("rounded-t-" + size); return this; }
        public Builder roundedR(String size) { classes.add("rounded-r-" + size); return this; }
        public Builder roundedB(String size) { classes.add("rounded-b-" + size); return this; }
        public Builder roundedL(String size) { classes.add("rounded-l-" + size); return this; }

        // ==================== Shadow ====================

        public Builder shadowSm() { classes.add("shadow-sm"); return this; }
        public Builder shadow() { classes.add("shadow"); return this; }
        public Builder shadowMd() { classes.add("shadow-md"); return this; }
        public Builder shadowLg() { classes.add("shadow-lg"); return this; }
        public Builder shadowXl() { classes.add("shadow-xl"); return this; }
        public Builder shadow2xl() { classes.add("shadow-2xl"); return this; }
        public Builder shadowInner() { classes.add("shadow-inner"); return this; }
        public Builder shadowNone() { classes.add("shadow-none"); return this; }

        // ==================== Opacity ====================

        public Builder opacity0() { classes.add("opacity-0"); return this; }
        public Builder opacity5() { classes.add("opacity-5"); return this; }
        public Builder opacity10() { classes.add("opacity-10"); return this; }
        public Builder opacity20() { classes.add("opacity-20"); return this; }
        public Builder opacity25() { classes.add("opacity-25"); return this; }
        public Builder opacity30() { classes.add("opacity-30"); return this; }
        public Builder opacity40() { classes.add("opacity-40"); return this; }
        public Builder opacity50() { classes.add("opacity-50"); return this; }
        public Builder opacity60() { classes.add("opacity-60"); return this; }
        public Builder opacity70() { classes.add("opacity-70"); return this; }
        public Builder opacity75() { classes.add("opacity-75"); return this; }
        public Builder opacity80() { classes.add("opacity-80"); return this; }
        public Builder opacity90() { classes.add("opacity-90"); return this; }
        public Builder opacity95() { classes.add("opacity-95"); return this; }
        public Builder opacity100() { classes.add("opacity-100"); return this; }

        // ==================== Overflow ====================

        public Builder overflowAuto() { classes.add("overflow-auto"); return this; }
        public Builder overflowHidden() { classes.add("overflow-hidden"); return this; }
        public Builder overflowClip() { classes.add("overflow-clip"); return this; }
        public Builder overflowVisible() { classes.add("overflow-visible"); return this; }
        public Builder overflowScroll() { classes.add("overflow-scroll"); return this; }
        public Builder overflowXAuto() { classes.add("overflow-x-auto"); return this; }
        public Builder overflowYAuto() { classes.add("overflow-y-auto"); return this; }
        public Builder overflowXHidden() { classes.add("overflow-x-hidden"); return this; }
        public Builder overflowYHidden() { classes.add("overflow-y-hidden"); return this; }

        // ==================== Cursor ====================

        public Builder cursorAuto() { classes.add("cursor-auto"); return this; }
        public Builder cursorDefault() { classes.add("cursor-default"); return this; }
        public Builder cursorPointer() { classes.add("cursor-pointer"); return this; }
        public Builder cursorWait() { classes.add("cursor-wait"); return this; }
        public Builder cursorText() { classes.add("cursor-text"); return this; }
        public Builder cursorMove() { classes.add("cursor-move"); return this; }
        public Builder cursorHelp() { classes.add("cursor-help"); return this; }
        public Builder cursorNotAllowed() { classes.add("cursor-not-allowed"); return this; }
        public Builder cursorNone() { classes.add("cursor-none"); return this; }
        public Builder cursorGrab() { classes.add("cursor-grab"); return this; }
        public Builder cursorGrabbing() { classes.add("cursor-grabbing"); return this; }

        // ==================== Pointer Events ====================

        public Builder pointerEventsNone() { classes.add("pointer-events-none"); return this; }
        public Builder pointerEventsAuto() { classes.add("pointer-events-auto"); return this; }

        // ==================== User Select ====================

        public Builder selectNone() { classes.add("select-none"); return this; }
        public Builder selectText() { classes.add("select-text"); return this; }
        public Builder selectAll() { classes.add("select-all"); return this; }
        public Builder selectAuto() { classes.add("select-auto"); return this; }

        // ==================== Transition ====================

        public Builder transition() { classes.add("transition"); return this; }
        public Builder transitionNone() { classes.add("transition-none"); return this; }
        public Builder transitionAll() { classes.add("transition-all"); return this; }
        public Builder transitionColors() { classes.add("transition-colors"); return this; }
        public Builder transitionOpacity() { classes.add("transition-opacity"); return this; }
        public Builder transitionShadow() { classes.add("transition-shadow"); return this; }
        public Builder transitionTransform() { classes.add("transition-transform"); return this; }

        public Builder duration75() { classes.add("duration-75"); return this; }
        public Builder duration100() { classes.add("duration-100"); return this; }
        public Builder duration150() { classes.add("duration-150"); return this; }
        public Builder duration200() { classes.add("duration-200"); return this; }
        public Builder duration300() { classes.add("duration-300"); return this; }
        public Builder duration500() { classes.add("duration-500"); return this; }
        public Builder duration700() { classes.add("duration-700"); return this; }
        public Builder duration1000() { classes.add("duration-1000"); return this; }

        public Builder easeLinear() { classes.add("ease-linear"); return this; }
        public Builder easeIn() { classes.add("ease-in"); return this; }
        public Builder easeOut() { classes.add("ease-out"); return this; }
        public Builder easeInOut() { classes.add("ease-in-out"); return this; }

        // ==================== Transform ====================

        public Builder transformNone() { classes.add("transform-none"); return this; }
        public Builder transformGpu() { classes.add("transform-gpu"); return this; }

        public Builder scale0() { classes.add("scale-0"); return this; }
        public Builder scale50() { classes.add("scale-50"); return this; }
        public Builder scale75() { classes.add("scale-75"); return this; }
        public Builder scale90() { classes.add("scale-90"); return this; }
        public Builder scale95() { classes.add("scale-95"); return this; }
        public Builder scale100() { classes.add("scale-100"); return this; }
        public Builder scale105() { classes.add("scale-105"); return this; }
        public Builder scale110() { classes.add("scale-110"); return this; }
        public Builder scale125() { classes.add("scale-125"); return this; }
        public Builder scale150() { classes.add("scale-150"); return this; }

        public Builder rotate0() { classes.add("rotate-0"); return this; }
        public Builder rotate1() { classes.add("rotate-1"); return this; }
        public Builder rotate2() { classes.add("rotate-2"); return this; }
        public Builder rotate3() { classes.add("rotate-3"); return this; }
        public Builder rotate6() { classes.add("rotate-6"); return this; }
        public Builder rotate12() { classes.add("rotate-12"); return this; }
        public Builder rotate45() { classes.add("rotate-45"); return this; }
        public Builder rotate90() { classes.add("rotate-90"); return this; }
        public Builder rotate180() { classes.add("rotate-180"); return this; }

        public Builder translateXHalf() { classes.add("translate-x-1/2"); return this; }
        public Builder translateYHalf() { classes.add("translate-y-1/2"); return this; }
        public Builder translateXNegHalf() { classes.add("-translate-x-1/2"); return this; }
        public Builder translateYNegHalf() { classes.add("-translate-y-1/2"); return this; }
        public Builder translateXFull() { classes.add("translate-x-full"); return this; }
        public Builder translateYFull() { classes.add("translate-y-full"); return this; }

        // ==================== Visibility ====================

        public Builder visible() { classes.add("visible"); return this; }
        public Builder invisible() { classes.add("invisible"); return this; }
        public Builder collapse() { classes.add("collapse"); return this; }

        // ==================== Object Fit ====================

        public Builder objectContain() { classes.add("object-contain"); return this; }
        public Builder objectCover() { classes.add("object-cover"); return this; }
        public Builder objectFill() { classes.add("object-fill"); return this; }
        public Builder objectNone() { classes.add("object-none"); return this; }
        public Builder objectScaleDown() { classes.add("object-scale-down"); return this; }

        // ==================== Aspect Ratio ====================

        public Builder aspectAuto() { classes.add("aspect-auto"); return this; }
        public Builder aspectSquare() { classes.add("aspect-square"); return this; }
        public Builder aspectVideo() { classes.add("aspect-video"); return this; }

        // ==================== Hover States ====================

        public Builder hoverBgPrimary() { classes.add("hover:bg-primary"); return this; }
        public Builder hoverBgSecondary() { classes.add("hover:bg-secondary"); return this; }
        public Builder hoverBgMuted() { classes.add("hover:bg-muted"); return this; }
        public Builder hoverBgGray(int shade) { classes.add("hover:bg-gray-" + shade); return this; }
        public Builder hoverBgColor(String color) { classes.add("hover:bg-" + color); return this; }
        public Builder hoverTextPrimary() { classes.add("hover:text-primary"); return this; }
        public Builder hoverTextColor(String color) { classes.add("hover:text-" + color); return this; }
        public Builder hoverUnderline() { classes.add("hover:underline"); return this; }
        public Builder hoverNoUnderline() { classes.add("hover:no-underline"); return this; }
        public Builder hoverOpacity(int n) { classes.add("hover:opacity-" + n); return this; }
        public Builder hoverScale105() { classes.add("hover:scale-105"); return this; }
        public Builder hoverScale110() { classes.add("hover:scale-110"); return this; }
        public Builder hoverShadowLg() { classes.add("hover:shadow-lg"); return this; }

        // ==================== Focus States ====================

        public Builder focusOutlineNone() { classes.add("focus:outline-none"); return this; }
        public Builder focusRing() { classes.add("focus:ring"); return this; }
        public Builder focusRing2() { classes.add("focus:ring-2"); return this; }
        public Builder focusRingPrimary() { classes.add("focus:ring-primary"); return this; }
        public Builder focusRingOffset2() { classes.add("focus:ring-offset-2"); return this; }
        public Builder focusBorderPrimary() { classes.add("focus:border-primary"); return this; }

        // ==================== Active States ====================

        public Builder activeBgColor(String color) { classes.add("active:bg-" + color); return this; }
        public Builder activeScale95() { classes.add("active:scale-95"); return this; }

        // ==================== Disabled State ====================

        public Builder disabledOpacity50() { classes.add("disabled:opacity-50"); return this; }
        public Builder disabledCursorNotAllowed() { classes.add("disabled:cursor-not-allowed"); return this; }

        // ==================== Dark Mode ====================

        public Builder darkBgColor(String color) { classes.add("dark:bg-" + color); return this; }
        public Builder darkTextColor(String color) { classes.add("dark:text-" + color); return this; }
        public Builder darkBorderColor(String color) { classes.add("dark:border-" + color); return this; }

        // ==================== Responsive ====================

        public Builder sm(String cls) { classes.add("sm:" + cls); return this; }
        public Builder md(String cls) { classes.add("md:" + cls); return this; }
        public Builder lg(String cls) { classes.add("lg:" + cls); return this; }
        public Builder xl(String cls) { classes.add("xl:" + cls); return this; }
        public Builder xxl(String cls) { classes.add("2xl:" + cls); return this; }

        // ==================== Custom ====================

        public Builder add(String className) {
            classes.add(className);
            return this;
        }

        public Builder addIf(boolean condition, String className) {
            if (condition) classes.add(className);
            return this;
        }

        // ==================== Build ====================

        public String build() {
            return String.join(" ", classes);
        }

        @Override
        public String toString() {
            return build();
        }
    }

    // ==================== CSS Generation ====================

    /**
     * Generates CSS for all utility classes.
     * Use this with Theme.preset() for a complete utility system.
     */
    public static String generateCss(Theme theme) {
        StringBuilder css = new StringBuilder();

        // Display
        css.append(".block { display: block; }\n");
        css.append(".inline-block { display: inline-block; }\n");
        css.append(".inline { display: inline; }\n");
        css.append(".flex { display: flex; }\n");
        css.append(".inline-flex { display: inline-flex; }\n");
        css.append(".grid { display: grid; }\n");
        css.append(".inline-grid { display: inline-grid; }\n");
        css.append(".hidden { display: none; }\n");
        css.append(".contents { display: contents; }\n\n");

        // Flex direction
        css.append(".flex-row { flex-direction: row; }\n");
        css.append(".flex-row-reverse { flex-direction: row-reverse; }\n");
        css.append(".flex-col { flex-direction: column; }\n");
        css.append(".flex-col-reverse { flex-direction: column-reverse; }\n\n");

        // Flex wrap
        css.append(".flex-wrap { flex-wrap: wrap; }\n");
        css.append(".flex-wrap-reverse { flex-wrap: wrap-reverse; }\n");
        css.append(".flex-nowrap { flex-wrap: nowrap; }\n\n");

        // Flex
        css.append(".flex-1 { flex: 1 1 0%; }\n");
        css.append(".flex-auto { flex: 1 1 auto; }\n");
        css.append(".flex-initial { flex: 0 1 auto; }\n");
        css.append(".flex-none { flex: none; }\n");
        css.append(".grow { flex-grow: 1; }\n");
        css.append(".grow-0 { flex-grow: 0; }\n");
        css.append(".shrink { flex-shrink: 1; }\n");
        css.append(".shrink-0 { flex-shrink: 0; }\n\n");

        // Justify content
        css.append(".justify-start { justify-content: flex-start; }\n");
        css.append(".justify-end { justify-content: flex-end; }\n");
        css.append(".justify-center { justify-content: center; }\n");
        css.append(".justify-between { justify-content: space-between; }\n");
        css.append(".justify-around { justify-content: space-around; }\n");
        css.append(".justify-evenly { justify-content: space-evenly; }\n\n");

        // Align items
        css.append(".items-start { align-items: flex-start; }\n");
        css.append(".items-end { align-items: flex-end; }\n");
        css.append(".items-center { align-items: center; }\n");
        css.append(".items-baseline { align-items: baseline; }\n");
        css.append(".items-stretch { align-items: stretch; }\n\n");

        // Self alignment
        css.append(".self-auto { align-self: auto; }\n");
        css.append(".self-start { align-self: flex-start; }\n");
        css.append(".self-end { align-self: flex-end; }\n");
        css.append(".self-center { align-self: center; }\n");
        css.append(".self-stretch { align-self: stretch; }\n\n");

        // Place content/items
        css.append(".place-content-center { place-content: center; }\n");
        css.append(".place-items-center { place-items: center; }\n\n");

        // Grid columns
        for (int i = 1; i <= 12; i++) {
            css.append(".grid-cols-").append(i).append(" { grid-template-columns: repeat(").append(i).append(", minmax(0, 1fr)); }\n");
        }
        css.append("\n");

        // Column span
        for (int i = 1; i <= 12; i++) {
            css.append(".col-span-").append(i).append(" { grid-column: span ").append(i).append(" / span ").append(i).append("; }\n");
        }
        css.append(".col-span-full { grid-column: 1 / -1; }\n\n");

        // Generate spacing utilities
        String[] spacingKeys = {"0", "px", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9", "10", "11", "12", "14", "16", "20", "24", "28", "32", "36", "40", "44", "48", "52", "56", "60", "64", "72", "80", "96"};

        // Padding
        for (String key : spacingKeys) {
            String val = "var(--spacing-" + key + ")";
            css.append(".p-").append(key).append(" { padding: ").append(val).append("; }\n");
            css.append(".px-").append(key).append(" { padding-left: ").append(val).append("; padding-right: ").append(val).append("; }\n");
            css.append(".py-").append(key).append(" { padding-top: ").append(val).append("; padding-bottom: ").append(val).append("; }\n");
            css.append(".pt-").append(key).append(" { padding-top: ").append(val).append("; }\n");
            css.append(".pr-").append(key).append(" { padding-right: ").append(val).append("; }\n");
            css.append(".pb-").append(key).append(" { padding-bottom: ").append(val).append("; }\n");
            css.append(".pl-").append(key).append(" { padding-left: ").append(val).append("; }\n");
        }
        css.append("\n");

        // Margin
        for (String key : spacingKeys) {
            String val = "var(--spacing-" + key + ")";
            css.append(".m-").append(key).append(" { margin: ").append(val).append("; }\n");
            css.append(".mx-").append(key).append(" { margin-left: ").append(val).append("; margin-right: ").append(val).append("; }\n");
            css.append(".my-").append(key).append(" { margin-top: ").append(val).append("; margin-bottom: ").append(val).append("; }\n");
            css.append(".mt-").append(key).append(" { margin-top: ").append(val).append("; }\n");
            css.append(".mr-").append(key).append(" { margin-right: ").append(val).append("; }\n");
            css.append(".mb-").append(key).append(" { margin-bottom: ").append(val).append("; }\n");
            css.append(".ml-").append(key).append(" { margin-left: ").append(val).append("; }\n");
        }
        css.append(".m-auto { margin: auto; }\n");
        css.append(".mx-auto { margin-left: auto; margin-right: auto; }\n");
        css.append(".my-auto { margin-top: auto; margin-bottom: auto; }\n\n");

        // Gap
        for (String key : spacingKeys) {
            String val = "var(--spacing-" + key + ")";
            css.append(".gap-").append(key).append(" { gap: ").append(val).append("; }\n");
            css.append(".gap-x-").append(key).append(" { column-gap: ").append(val).append("; }\n");
            css.append(".gap-y-").append(key).append(" { row-gap: ").append(val).append("; }\n");
        }
        css.append("\n");

        // Width & Height with spacing
        for (String key : spacingKeys) {
            String val = "var(--spacing-" + key + ")";
            css.append(".w-").append(key).append(" { width: ").append(val).append("; }\n");
            css.append(".h-").append(key).append(" { height: ").append(val).append("; }\n");
        }
        css.append(".w-full { width: 100%; }\n");
        css.append(".w-screen { width: 100vw; }\n");
        css.append(".w-auto { width: auto; }\n");
        css.append(".w-min { width: min-content; }\n");
        css.append(".w-max { width: max-content; }\n");
        css.append(".w-fit { width: fit-content; }\n");
        css.append(".w-1\\/2 { width: 50%; }\n");
        css.append(".w-1\\/3 { width: 33.333333%; }\n");
        css.append(".w-2\\/3 { width: 66.666667%; }\n");
        css.append(".w-1\\/4 { width: 25%; }\n");
        css.append(".w-3\\/4 { width: 75%; }\n");
        css.append(".h-full { height: 100%; }\n");
        css.append(".h-screen { height: 100vh; }\n");
        css.append(".h-auto { height: auto; }\n");
        css.append(".h-min { height: min-content; }\n");
        css.append(".h-max { height: max-content; }\n");
        css.append(".h-fit { height: fit-content; }\n\n");

        // Max width
        css.append(".max-w-none { max-width: none; }\n");
        css.append(".max-w-sm { max-width: 24rem; }\n");
        css.append(".max-w-md { max-width: 28rem; }\n");
        css.append(".max-w-lg { max-width: 32rem; }\n");
        css.append(".max-w-xl { max-width: 36rem; }\n");
        css.append(".max-w-2xl { max-width: 42rem; }\n");
        css.append(".max-w-3xl { max-width: 48rem; }\n");
        css.append(".max-w-4xl { max-width: 56rem; }\n");
        css.append(".max-w-5xl { max-width: 64rem; }\n");
        css.append(".max-w-6xl { max-width: 72rem; }\n");
        css.append(".max-w-7xl { max-width: 80rem; }\n");
        css.append(".max-w-full { max-width: 100%; }\n");
        css.append(".max-w-prose { max-width: 65ch; }\n\n");

        // Position
        css.append(".static { position: static; }\n");
        css.append(".fixed { position: fixed; }\n");
        css.append(".absolute { position: absolute; }\n");
        css.append(".relative { position: relative; }\n");
        css.append(".sticky { position: sticky; }\n\n");

        // Inset
        css.append(".inset-0 { inset: 0; }\n");
        css.append(".top-0 { top: 0; }\n");
        css.append(".right-0 { right: 0; }\n");
        css.append(".bottom-0 { bottom: 0; }\n");
        css.append(".left-0 { left: 0; }\n\n");

        // Z-index
        css.append(".z-0 { z-index: 0; }\n");
        css.append(".z-10 { z-index: 10; }\n");
        css.append(".z-20 { z-index: 20; }\n");
        css.append(".z-30 { z-index: 30; }\n");
        css.append(".z-40 { z-index: 40; }\n");
        css.append(".z-50 { z-index: 50; }\n");
        css.append(".z-auto { z-index: auto; }\n\n");

        // Typography
        css.append(".text-xs { font-size: var(--font-size-xs); line-height: 1rem; }\n");
        css.append(".text-sm { font-size: var(--font-size-sm); line-height: 1.25rem; }\n");
        css.append(".text-base { font-size: var(--font-size-base); line-height: 1.5rem; }\n");
        css.append(".text-lg { font-size: var(--font-size-lg); line-height: 1.75rem; }\n");
        css.append(".text-xl { font-size: var(--font-size-xl); line-height: 1.75rem; }\n");
        css.append(".text-2xl { font-size: var(--font-size-2xl); line-height: 2rem; }\n");
        css.append(".text-3xl { font-size: var(--font-size-3xl); line-height: 2.25rem; }\n");
        css.append(".text-4xl { font-size: var(--font-size-4xl); line-height: 2.5rem; }\n");
        css.append(".text-5xl { font-size: var(--font-size-5xl); line-height: 1; }\n");
        css.append(".text-6xl { font-size: var(--font-size-6xl); line-height: 1; }\n\n");

        // Font weight
        css.append(".font-thin { font-weight: 100; }\n");
        css.append(".font-extralight { font-weight: 200; }\n");
        css.append(".font-light { font-weight: 300; }\n");
        css.append(".font-normal { font-weight: 400; }\n");
        css.append(".font-medium { font-weight: 500; }\n");
        css.append(".font-semibold { font-weight: 600; }\n");
        css.append(".font-bold { font-weight: 700; }\n");
        css.append(".font-extrabold { font-weight: 800; }\n");
        css.append(".font-black { font-weight: 900; }\n\n");

        // Text alignment
        css.append(".text-left { text-align: left; }\n");
        css.append(".text-center { text-align: center; }\n");
        css.append(".text-right { text-align: right; }\n");
        css.append(".text-justify { text-align: justify; }\n\n");

        // Text transform
        css.append(".uppercase { text-transform: uppercase; }\n");
        css.append(".lowercase { text-transform: lowercase; }\n");
        css.append(".capitalize { text-transform: capitalize; }\n");
        css.append(".normal-case { text-transform: none; }\n\n");

        // Text decoration
        css.append(".underline { text-decoration-line: underline; }\n");
        css.append(".overline { text-decoration-line: overline; }\n");
        css.append(".line-through { text-decoration-line: line-through; }\n");
        css.append(".no-underline { text-decoration-line: none; }\n\n");

        // Text colors
        css.append(".text-white { color: var(--color-white); }\n");
        css.append(".text-black { color: var(--color-black); }\n");
        css.append(".text-primary { color: var(--color-primary-600); }\n");
        css.append(".text-muted { color: var(--color-muted-foreground); }\n");
        css.append(".text-success { color: var(--color-success); }\n");
        css.append(".text-warning { color: var(--color-warning); }\n");
        css.append(".text-error { color: var(--color-error); }\n");
        css.append(".text-info { color: var(--color-info); }\n\n");

        // Background colors
        css.append(".bg-white { background-color: var(--color-white); }\n");
        css.append(".bg-black { background-color: var(--color-black); }\n");
        css.append(".bg-primary { background-color: var(--color-primary-600); }\n");
        css.append(".bg-muted { background-color: var(--color-muted); }\n");
        css.append(".bg-success { background-color: var(--color-success); }\n");
        css.append(".bg-warning { background-color: var(--color-warning); }\n");
        css.append(".bg-error { background-color: var(--color-error); }\n");
        css.append(".bg-info { background-color: var(--color-info); }\n");
        css.append(".bg-transparent { background-color: transparent; }\n\n");

        // Border
        css.append(".border { border-width: 1px; }\n");
        css.append(".border-0 { border-width: 0px; }\n");
        css.append(".border-2 { border-width: 2px; }\n");
        css.append(".border-4 { border-width: 4px; }\n");
        css.append(".border-8 { border-width: 8px; }\n");
        css.append(".border-t { border-top-width: 1px; }\n");
        css.append(".border-r { border-right-width: 1px; }\n");
        css.append(".border-b { border-bottom-width: 1px; }\n");
        css.append(".border-l { border-left-width: 1px; }\n\n");

        css.append(".border-solid { border-style: solid; }\n");
        css.append(".border-dashed { border-style: dashed; }\n");
        css.append(".border-dotted { border-style: dotted; }\n");
        css.append(".border-double { border-style: double; }\n");
        css.append(".border-none { border-style: none; }\n\n");

        // Border radius
        css.append(".rounded-none { border-radius: 0px; }\n");
        css.append(".rounded-sm { border-radius: var(--radius-sm); }\n");
        css.append(".rounded { border-radius: var(--radius-DEFAULT); }\n");
        css.append(".rounded-md { border-radius: var(--radius-md); }\n");
        css.append(".rounded-lg { border-radius: var(--radius-lg); }\n");
        css.append(".rounded-xl { border-radius: var(--radius-xl); }\n");
        css.append(".rounded-2xl { border-radius: var(--radius-2xl); }\n");
        css.append(".rounded-3xl { border-radius: var(--radius-3xl); }\n");
        css.append(".rounded-full { border-radius: 9999px; }\n\n");

        // Shadow
        css.append(".shadow-sm { box-shadow: var(--shadow-sm); }\n");
        css.append(".shadow { box-shadow: var(--shadow-DEFAULT); }\n");
        css.append(".shadow-md { box-shadow: var(--shadow-md); }\n");
        css.append(".shadow-lg { box-shadow: var(--shadow-lg); }\n");
        css.append(".shadow-xl { box-shadow: var(--shadow-xl); }\n");
        css.append(".shadow-2xl { box-shadow: var(--shadow-2xl); }\n");
        css.append(".shadow-inner { box-shadow: var(--shadow-inner); }\n");
        css.append(".shadow-none { box-shadow: var(--shadow-none); }\n\n");

        // Opacity
        css.append(".opacity-0 { opacity: 0; }\n");
        css.append(".opacity-5 { opacity: 0.05; }\n");
        css.append(".opacity-10 { opacity: 0.1; }\n");
        css.append(".opacity-20 { opacity: 0.2; }\n");
        css.append(".opacity-25 { opacity: 0.25; }\n");
        css.append(".opacity-30 { opacity: 0.3; }\n");
        css.append(".opacity-40 { opacity: 0.4; }\n");
        css.append(".opacity-50 { opacity: 0.5; }\n");
        css.append(".opacity-60 { opacity: 0.6; }\n");
        css.append(".opacity-70 { opacity: 0.7; }\n");
        css.append(".opacity-75 { opacity: 0.75; }\n");
        css.append(".opacity-80 { opacity: 0.8; }\n");
        css.append(".opacity-90 { opacity: 0.9; }\n");
        css.append(".opacity-95 { opacity: 0.95; }\n");
        css.append(".opacity-100 { opacity: 1; }\n\n");

        // Overflow
        css.append(".overflow-auto { overflow: auto; }\n");
        css.append(".overflow-hidden { overflow: hidden; }\n");
        css.append(".overflow-clip { overflow: clip; }\n");
        css.append(".overflow-visible { overflow: visible; }\n");
        css.append(".overflow-scroll { overflow: scroll; }\n");
        css.append(".overflow-x-auto { overflow-x: auto; }\n");
        css.append(".overflow-y-auto { overflow-y: auto; }\n");
        css.append(".overflow-x-hidden { overflow-x: hidden; }\n");
        css.append(".overflow-y-hidden { overflow-y: hidden; }\n\n");

        // Cursor
        css.append(".cursor-auto { cursor: auto; }\n");
        css.append(".cursor-default { cursor: default; }\n");
        css.append(".cursor-pointer { cursor: pointer; }\n");
        css.append(".cursor-wait { cursor: wait; }\n");
        css.append(".cursor-text { cursor: text; }\n");
        css.append(".cursor-move { cursor: move; }\n");
        css.append(".cursor-help { cursor: help; }\n");
        css.append(".cursor-not-allowed { cursor: not-allowed; }\n");
        css.append(".cursor-none { cursor: none; }\n");
        css.append(".cursor-grab { cursor: grab; }\n");
        css.append(".cursor-grabbing { cursor: grabbing; }\n\n");

        // Pointer events
        css.append(".pointer-events-none { pointer-events: none; }\n");
        css.append(".pointer-events-auto { pointer-events: auto; }\n\n");

        // User select
        css.append(".select-none { user-select: none; }\n");
        css.append(".select-text { user-select: text; }\n");
        css.append(".select-all { user-select: all; }\n");
        css.append(".select-auto { user-select: auto; }\n\n");

        // Transition
        css.append(".transition-none { transition-property: none; }\n");
        css.append(".transition-all { transition-property: all; transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); transition-duration: 150ms; }\n");
        css.append(".transition { transition-property: color, background-color, border-color, text-decoration-color, fill, stroke, opacity, box-shadow, transform, filter, backdrop-filter; transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); transition-duration: 150ms; }\n");
        css.append(".transition-colors { transition-property: color, background-color, border-color, text-decoration-color, fill, stroke; transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); transition-duration: 150ms; }\n");
        css.append(".transition-opacity { transition-property: opacity; transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); transition-duration: 150ms; }\n");
        css.append(".transition-shadow { transition-property: box-shadow; transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); transition-duration: 150ms; }\n");
        css.append(".transition-transform { transition-property: transform; transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); transition-duration: 150ms; }\n\n");

        // Duration
        css.append(".duration-75 { transition-duration: 75ms; }\n");
        css.append(".duration-100 { transition-duration: 100ms; }\n");
        css.append(".duration-150 { transition-duration: 150ms; }\n");
        css.append(".duration-200 { transition-duration: 200ms; }\n");
        css.append(".duration-300 { transition-duration: 300ms; }\n");
        css.append(".duration-500 { transition-duration: 500ms; }\n");
        css.append(".duration-700 { transition-duration: 700ms; }\n");
        css.append(".duration-1000 { transition-duration: 1000ms; }\n\n");

        // Easing
        css.append(".ease-linear { transition-timing-function: linear; }\n");
        css.append(".ease-in { transition-timing-function: cubic-bezier(0.4, 0, 1, 1); }\n");
        css.append(".ease-out { transition-timing-function: cubic-bezier(0, 0, 0.2, 1); }\n");
        css.append(".ease-in-out { transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); }\n\n");

        // Scale
        css.append(".scale-0 { transform: scale(0); }\n");
        css.append(".scale-50 { transform: scale(.5); }\n");
        css.append(".scale-75 { transform: scale(.75); }\n");
        css.append(".scale-90 { transform: scale(.9); }\n");
        css.append(".scale-95 { transform: scale(.95); }\n");
        css.append(".scale-100 { transform: scale(1); }\n");
        css.append(".scale-105 { transform: scale(1.05); }\n");
        css.append(".scale-110 { transform: scale(1.1); }\n");
        css.append(".scale-125 { transform: scale(1.25); }\n");
        css.append(".scale-150 { transform: scale(1.5); }\n\n");

        // Visibility
        css.append(".visible { visibility: visible; }\n");
        css.append(".invisible { visibility: hidden; }\n");
        css.append(".collapse { visibility: collapse; }\n\n");

        // Truncate
        css.append(".truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }\n\n");

        // Aspect ratio
        css.append(".aspect-auto { aspect-ratio: auto; }\n");
        css.append(".aspect-square { aspect-ratio: 1 / 1; }\n");
        css.append(".aspect-video { aspect-ratio: 16 / 9; }\n\n");

        // Object fit
        css.append(".object-contain { object-fit: contain; }\n");
        css.append(".object-cover { object-fit: cover; }\n");
        css.append(".object-fill { object-fit: fill; }\n");
        css.append(".object-none { object-fit: none; }\n");
        css.append(".object-scale-down { object-fit: scale-down; }\n\n");

        // Hover states
        css.append(".hover\\:bg-primary:hover { background-color: var(--color-primary-600); }\n");
        css.append(".hover\\:bg-muted:hover { background-color: var(--color-muted); }\n");
        css.append(".hover\\:text-primary:hover { color: var(--color-primary-600); }\n");
        css.append(".hover\\:underline:hover { text-decoration-line: underline; }\n");
        css.append(".hover\\:no-underline:hover { text-decoration-line: none; }\n");
        css.append(".hover\\:scale-105:hover { transform: scale(1.05); }\n");
        css.append(".hover\\:scale-110:hover { transform: scale(1.1); }\n");
        css.append(".hover\\:shadow-lg:hover { box-shadow: var(--shadow-lg); }\n\n");

        // Focus states
        css.append(".focus\\:outline-none:focus { outline: 2px solid transparent; outline-offset: 2px; }\n");
        css.append(".focus\\:ring:focus { box-shadow: 0 0 0 3px var(--color-ring); }\n");
        css.append(".focus\\:ring-2:focus { box-shadow: 0 0 0 2px var(--color-ring); }\n");
        css.append(".focus\\:ring-primary:focus { --tw-ring-color: var(--color-primary-600); }\n");
        css.append(".focus\\:ring-offset-2:focus { --tw-ring-offset-width: 2px; }\n\n");

        // Active states
        css.append(".active\\:scale-95:active { transform: scale(.95); }\n\n");

        // Disabled states
        css.append(".disabled\\:opacity-50:disabled { opacity: 0.5; }\n");
        css.append(".disabled\\:cursor-not-allowed:disabled { cursor: not-allowed; }\n\n");

        return css.toString();
    }
}
