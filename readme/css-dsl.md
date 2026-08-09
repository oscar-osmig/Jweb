[← Back to README](./../README.md)

# CSS DSL

35 modules, ~15,700 lines. The core is `Style<T>` (the fluent property builder, ~700 methods)
plus the `CSS` facade (selectors, keyword constants, functions). Everything that can appear on
the right-hand side of a CSS declaration implements the one-method interface `CSSValue`.

## Imports

```java
import static com.osmig.Jweb.framework.styles.CSS.*;        // style(), rule(), styles(), selectors, keywords
import static com.osmig.Jweb.framework.styles.CSSUnits.*;   // px, rem, vh, fr, ms, calc, clamp, ...
import static com.osmig.Jweb.framework.styles.CSSColors.*;  // white, black, hex(), rgb(), hsl(), ...
```

> ⚠️ **Import collision warning:** several names exist in more than one module —
> `lightDark` and `colorMix` (both `CSSColors` and `CSSUnits`), `var` (both `CSS` and
> `CSSVariables`), `all` (`CSS` keyword vs `MediaQuery.all()`). Wildcard-importing colliding
> modules together produces "ambiguous reference" compile errors — qualify one side
> (e.g. `CSSUnits.colorMix(...)`) when it happens.

## Inline Styles

```java
// Lambda style (recommended)
div(attrs()
    .class_("card")
    .style(s -> s
        .display(flex)
        .padding(px(20))
        .backgroundColor(white)),
    content
)

// Fluent chain with .done()
div(attrs()
    .style()
        .display(flex)
        .padding(px(20))
    .done()
    .class_("card"),
    content
)

// Shortcuts
div(attrs().style(s -> s
    .size(px(100))           // width + height: 100px
    .shadow()                // default shadow preset
    .roundedLg()             // border-radius preset
))
```

**Sizing shortcuts:** `size`, `minSize`, `maxSize`, `widthRange(min,max)`, `fullWidth()`,
`fullViewportHeight()`.
**Shadow presets:** `shadowXs` … `shadowXl`, `shadowInner`, `shadowNone`.
**Radius presets:** `roundedNone` … `rounded3xl`, `roundedFull`.

`Style` covers ~55 property sections: box model, flexbox, grid, typography, backgrounds,
borders, transforms, transitions, animations, filters, positioning, overflow, columns,
scroll behavior, containment, and more. If a CSS property exists, it's very likely a method.

## CSS Rules and Stylesheets

Three mechanisms, from ad-hoc to global:

### 1. `CSS.styles(...)` — quick rule strings

```java
String css = styles(
    rule(".container")
        .maxWidth(px(1200))
        .margin(zero, auto)
        .padding(px(20)),

    rule(".button")
        .display(inlineBlock)
        .padding(px(10), px(20))
        .backgroundColor(hex("#3b82f6"))
        .color(white)
        .borderRadius(px(4)),

    rule(".button:hover")
        .backgroundColor(hex("#2563eb"))
);
// place it: style(css)  — emits a <style> tag
```

`rule(...)` returns a `StyleBuilder extends Style<StyleBuilder>` — the full property API plus
`toRule()`. Calling `toRule()` without a selector throws `IllegalStateException`.

### 2. `Stylesheet` — the global stylesheet accumulator

```java
import com.osmig.Jweb.framework.styles.Stylesheet;

Stylesheet sheet = Stylesheet.stylesheet()
    .variables("--primary", "#6366f1", "--radius", "8px")
    .rule("body", style().margin(zero).fontFamily("system-ui, sans-serif"))
    .rule(".hero", style().padding(rem(4)).textAlign(center))
    .keyframes(Keyframes.keyframes("gradientShift")
        .from(style().backgroundPosition("0% 50%"))
        .to(style().backgroundPosition("100% 50%")))
    .mediaQuery(MediaQuery.md(), new Stylesheet.Rule(".sidebar", style().display(block)));

// Emit:
sheet.build();          // formatted CSS
sheet.buildMinified();  // whitespace-squeezed
sheet.toStyleTag();     // "<style>…</style>" string — used by the sample app's Head.java
```

Also accepts `fontFace(FontFace)`, `supports(Supports)`, `raw(css)`, `comment(text)`.

### 3. Per-element styles with pseudo-classes — `Tag.styled()`

```java
div().styled(style().padding(px(16)).backgroundColor(white))
     .hover(style().backgroundColor(hex("#f5f5f5")))
     .focus(style().outline("2px solid blue"))
```

Generates a unique class (`jweb-1`, `jweb-2`, …) and renders an adjacent
`<style>.jweb-1{...}.jweb-1:hover{...}</style>` block. Note there is no deduplication or
hoisting into `<head>` — each styled element emits its own style block inline.

## CSS Units (`CSSUnits`)

```java
px(16), rem(1.5), em(1.2), percent(50), ch(60), ex(2), lh(1), rlh(1)
vh(100), vw(50), vmin(10), vmax(10)
dvh(100), dvw(50), svh(100), lvh(100)         // dynamic/small/large viewport
cqw(10), cqh(10), cqi(10), cqb(10), cqmin(5), cqmax(5)   // container query units
fr(1), num(1.5)
ms(300), s(0.5), deg(45), rad(1.57), turn(0.25)
auto, zero, none, inherit, initial, unset
raw("anything")                                 // escape hatch

// Math
calc("100% - 20px"), min(...), max(...), clamp(rem(1), vw(4), rem(2))
round(...), mod(...), abs(...), pow(...), sqrt(...), hypot(...), sin/cos/tan(...)

// Modern color spaces (these live in CSSUnits, not CSSColors)
oklch(0.7, 0.15, 200), oklab(...), lab(...), lch(...), hwb(...)
colorMix("srgb", colorA, colorB, 50)           // 4-arg form with color space
lightDark(lightColor, darkColor)

// Misc
env("safe-area-inset-top"), imageSet("a.png 1x", "a@2x.png 2x"), steps(4, "end")
repeatingLinearGradient(...), repeatingRadialGradient(...), repeatingConicGradient(...)
```

> Note: there is **no `pt()`** unit function.

## CSS Colors (`CSSColors`)

```java
white, black, transparent, currentColor
red, green, blue, yellow, cyan, magenta, gray, silver     // flat constants
orange, purple, pink, navy, teal, coral, crimson, gold,
indigo, violet, salmon, turquoise, skyBlue, slateGray, ...

hex("#3b82f6")
rgb(59, 130, 246)
rgba(59, 130, 246, 0.5)
hsl(217, 91, 60), hsla(217, 91, 60, 0.5)

colorMix(colorA, colorB, 50)      // 3-arg srgb mix
lighten(color, 20), darken(color, 20)
lightDark(white, black)           // theme-aware
```

> Note: there is **no shade palette** like `blue(500)` — colors are flat constants or
> functions. Build palettes with `CSSVariables.colorPalette(...)` / a `Theme` instead.

## Media Queries (`MediaQuery`)

```java
import static com.osmig.Jweb.framework.styles.MediaQuery.*;

media().minWidth(px(768)).rule(".container", style().maxWidth(px(720))).build()
md().rule(".sidebar", style().display(block)).build()      // presets: xs sm md lg xl xxl
mobile(); tablet(); desktop();

media().prefersDark()
    .rule("body", style().backgroundColor(hex("#1a1a1a")).color(white)).build()
media().prefersReducedMotion()
    .rule("*", style().animationDuration(ms(0)).transitionDuration(ms(0))).build()

// Also: portrait()/landscape(), retina(), hover()/coarsePointer(), print(),
// displayMode/standalone(), and()/not()/only(), condition("raw")
```

## Container Queries (`ContainerQuery`)

```java
import static com.osmig.Jweb.framework.styles.ContainerQuery.*;

container().minWidth(px(400)).rule(".card", style().display(flex)).build()
container("sidebar").maxWidth(px(300)).rule(".nav", style().flexDirection(column)).build()
```

## Feature Queries (`Supports`)

```java
import static com.osmig.Jweb.framework.styles.Supports.*;

supports("display", "grid").rule(".container", style().display(grid)).build()
supportsGrid(); supportsFlexbox(); supportsCustomProperties();
supportsBackdropFilter(); supportsContainerQueries();     // 17 named presets total
```

## Other At-Rules

```java
// @layer — cascade layers (CSSLayer)
CSSLayer.order("reset", "base", "components", "utilities")
CSSLayer.layer("components", rule(".btn").padding(px(8)))

// @scope (CSSScope)
CSSScope.scope(".card").to(".card-footer").rule("p", style().margin(zero))

// @property — typed custom properties (CSSProperty)
CSSProperty.register("--angle").syntax("<angle>").inherits(false).initialValue("0deg")

// @font-face (FontFace)
FontFace.fontFace().family("Inter").src("/fonts/inter.woff2", "woff2")
    .weightRange(100, 900).display("swap")

// @keyframes (Keyframes)
keyframes("fadeIn")
    .from(style().opacity(0))
    .to(style().opacity(1))
    .build();
keyframes("pulse").at(0, style().opacity(1)).at(50, style().opacity(0.5)).at(100, style().opacity(1))
// 12 presets: fadeIn/fadeOut/slideIn*/pulse/bounce/shake/spin/zoomIn/zoomOut
```

## Selectors

Two APIs — the fluent `CSS.Selector` builder and static `Selectors` strings:

```java
// Fluent (CSS facade): select()/tag()/cls()/id() starters, ~80 chainable methods
rule(cls("card").hover())                    // .card:hover
rule(cls("input").focusVisible())
rule(tag("li").nthChild("2n+1"))
rule(cls("form").has("input:invalid"))       // :has()

// Static strings (Selectors)
import static com.osmig.Jweb.framework.styles.Selectors.*;
has(".open"), is(".a", ".b"), where(...), not(...)
attrEquals("type", "text"), attrStartsWith("href", "https")
viewTransitionOld("hero"), viewTransitionNew("hero")     // view-transition pseudo-elements
// webkit scrollbar selectors, combinators: descendant/child/adjacent/sibling
```

## CSS Variables & Design Tokens (`CSSVariables`)

```java
import static com.osmig.Jweb.framework.styles.CSSVariables.*;

var("primary-color")             // var(--primary-color)   ← named var(), not var_()
var("spacing", "1rem")           // with fallback
varChain("a", "b", "1rem")       // var(--a, var(--b, 1rem))

designSystem()
    .prefix("app")
    .spacing(rem(0.25), rem(0.5), rem(1), rem(2))         // positional scale
    .colors("primary", "#3b82f6", "secondary", "#10b981") // name/value pairs
    .colorPalette("blue", hex("#eff6ff"), hex("#3b82f6"), hex("#1e3a8a"))
    .build();

theme()                          // ThemeBuilder (not themeBuilder())
    .light("bg", "white", "text", "black")
    .dark("bg", "#0f172a", "text", "#e2e8f0")
    .buildBoth();                // or buildLight()/buildDark()
```

## `Theme` — token store with dark mode

The class the sample app's `Theme.java` wraps:

```java
import com.osmig.Jweb.framework.styles.Theme;

Theme theme = new Theme()
    .color("primary", "#6366f1")
    .darkColor("primary", "#818cf8")
    .spacing("md", "1rem")
    .font("sans", "system-ui, sans-serif");

theme.toCss();            // :root { --color-primary: ... }
theme.toFullCss();        // + @media (prefers-color-scheme: dark) overrides
theme.toStyleElement();   // ready-to-place <style> Element
theme.color("primary");   // CSSValue reference: var(--color-primary)
```

## `Utility` — Tailwind-style class generator

Generates utility-class CSS from a `Theme` (`Utility.generateCss(theme)`) and provides ~500
class-name builder methods for markup that prefers utility classes over inline styles.

## CSS Animations (`CSSAnimations`)

```java
import static com.osmig.Jweb.framework.styles.CSSAnimations.*;

// 40 presets: fadeIn/Out, fadeInUp/Down/Left/Right, slideIn*/slideOut*, zoomIn/Out,
// scaleIn/Out, pulse, heartbeat, bounce, rotate360, flipX/Y, shake, wobble, jello,
// swing, rubberBand, flash, tada, headShake, ...
style().animation(fadeIn(s(1)))
style().animation(slideInLeft(s(0.6)))
style().animation(pulse(s(1.5)).iterationCount(iterationInfinite))
style().animation(rotate360(s(2)).timing(timingLinear))

// Builder chain: .timing() .delay() .iterationCount() .direction() .fillMode() .playState()

// Scroll-driven animations
style().animation(fadeIn(s(1)))
       .animationTimeline(scrollTimeline())
       .animationRange("entry", "exit")

// Composition
composeAnimations(fadeIn(s(1)).css(), slideInLeft(s(1)).css())
staggerDelay(index, 100)
```

## Grid helpers (`CSSGrid`)

```java
import static com.osmig.Jweb.framework.styles.CSSGrid.*;

style().gridTemplateColumns(repeat(3, fr(1)))
style().gridTemplateColumns(repeat(autoFill(), minmax(px(200), fr(1))))
style().gridTemplateRows(masonry())
minContent(), maxContent(), fitContent(px(300)), span(2), subgrid()
templateAreas("header header", "sidebar main", "footer footer")
```

## The Six Property-String Modules (anchor positioning, scroll snap, text wrap, subgrid, masking, logical properties)

These modules (`CSSAnchorPositioning`, `CSSScrollSnap`, `CSSTextWrap`, `CSSSubgrid`,
`CSSMasking`, `CSSLogicalProperties`) return complete `"property:value"` **strings**.

> Use them with the single-argument `prop(String)` overload, which splits the string at the
> first colon: `.prop(anchorName("--x"))`. The two-arg
> `prop(name, value)` form also still works:

```java
// Anchor positioning
rule(".trigger").prop("anchor-name", "--my-trigger")
rule(".tooltip")
    .prop("position-anchor", "--my-trigger")
    .prop("position-area", "top")
    .position(absolute)
    .prop("top", "anchor(--my-trigger bottom)")

// Scroll snap
rule(".carousel")
    .prop("scroll-snap-type", "x mandatory")
    .prop("scroll-padding", "0 20px")
    .display(flex)
    .overflowX(auto)
rule(".carousel > .slide")
    .prop("scroll-snap-align", "start")
    .prop("scroll-snap-stop", "always")

// Text wrapping
rule("h1, h2, h3").prop("text-wrap", "balance")
rule("p").prop("text-wrap", "pretty")
rule(".excerpt").prop("-webkit-line-clamp", "3")     // see CSSTextWrap.lineClamp for full recipe

// Subgrid
rule(".grid").display(grid).prop("grid-template-columns", "1fr 2fr 1fr")
rule(".grid > .item").display(grid).gridColumn("1 / -1")
    .prop("grid-template-columns", "subgrid")

// Masking / clipping
rule(".fade-bottom").prop("mask-image", "linear-gradient(black 60%, transparent)")
rule(".circle").prop("clip-path", "circle(50%)")
rule(".hexagon").prop("clip-path", CSSMasking.clipHexagon().split(":", 2)[1])

// Logical properties (RTL-safe)
rule(".card").prop("margin-block", "1rem").prop("margin-inline", "auto")
rule(".container").prop("inline-size", "100%").prop("max-inline-size", "1200px")
```

The module methods remain useful as documented references for exact property names/values, and
`CSSMasking`'s preset shapes (`clipDiamond`, `clipPentagon`, `clipHexagon`, `clipStar`,
`clipTriangle*`) encode the polygon coordinates so you don't have to.

## Transitions

```java
// On Style
style().transition("opacity 0.3s ease")
style().transitionProperty("transform").transitionDuration(ms(300))

// CSS facade helpers
style().transition(trans(propTransform, ms(300), timingEaseOut))
style().transition(transitions(trans(propOpacity, ms(200)), trans(propColor, ms(150))))

// Attribute-level builder (attrs().transition())
div(attrs().class_("box")
    .transition().property("opacity", "transform").duration(300).easeInOut().done(),
    content)
button(attrs().transition().fade().done(), "Hover me")
```

## Nested CSS (`CSSNested`)

```java
import static com.osmig.Jweb.framework.styles.CSSNested.*;

nested(".card")
    .style(rule(".card").padding(px(20)))     // base declarations via a StyleBuilder
    .nest("&:hover").prop("box-shadow", "0 2px 8px rgba(0,0,0,0.1)").parent()
    .nest("& .title").prop("font-size", "1.5rem").root()
    .build();
// Also: .media(...)/.supports(...)/.container(...) nesting, and a BEM helper (BEMBlock)
```
