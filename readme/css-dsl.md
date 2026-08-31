[← Back to README](./../README.md)

# CSS DSL

~30 modules, ~15,700 lines. The core is `jweb.Style<T>` (the fluent property builder, ~400
methods) plus the `CSS` facade (selectors, keyword constants, functions). Everything that can
appear on the right-hand side of a CSS declaration implements the one-method interface
`CSSValue`.

## Imports

```java
import static jweb.Css.*;   // style(), rule(), units, colors, grid, animations,
                            // variables, media(), keyframes(), stylesheet()
import jweb.Style;          // the type your style helpers return
```

`jweb.Css` aggregates the legacy `CSS`, `CSSUnits`, `CSSColors`, `CSSGrid`,
`CSSAnimations` and `CSSVariables` modules plus the `media()`/`keyframes()`/
`stylesheet()` factories, so one import replaces the old three-to-six. The old
duplicate names (`colorMix`, `lightDark`, `var`, `env` existed in two modules each)
are resolved inside the facade — no more ambiguous-reference errors from combining
those wildcards.

Specialty modules keep their own imports but now live at `jweb.css.*` (same class
names): `Selectors`, `Supports`, `MediaQuery`, `Keyframes`, `Stylesheet`, `FontFace`,
`ContainerQuery`, `CSSNested`, `CSSMasking`, `CSSScrollSnap`, `CSSLogicalProperties`,
`CSSAnchorPositioning`, `CSSTextWrap`, `CSSSubgrid`, `CSSScope`, `CSSLayer`,
`Theme`, `Utility`, `Styles`, `CSSProperty` — e.g.
`import static jweb.css.Selectors.*;`.

> The legacy imports still compile — the old entry classes are `@Deprecated`
> aliases feeding the same methods.

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
    .size(px(100))                          // width + height: 100px
    .boxShadow("0 4px 6px rgba(0,0,0,.1)")
    .borderRadius(px(8))
))
```

**Kept, because they express a multi-property pattern rather than one value:** `size`,
`full`, `fullWidth`, `fullHeight`, `flexCenter`, `flexCol`, `flexRow`, `flexBetween`,
`grid(n)`, `grid(n, gap)`, `truncate`, `srOnly`, `borderMask`, `absolute`, `centerX`.

**Deprecated, because each was one value under an invented name:** the `shadowXs`…`shadowXl`
and `roundedNone`…`roundedFull` scales, plus `bold`, `clickable`, `textCenter`, `noSelect`,
`relative`, `fixed`, `sticky`, `minSize`, `maxSize`, `widthRange`, `heightRange`,
`fullViewportWidth`, `fullViewportHeight`. Write the property: `borderRadius(px(8))`,
`fontWeight(700)`, `cursor("pointer")`, `height("100vh")`.

`Style` covers ~55 property sections: box model, flexbox, grid, typography, backgrounds,
borders, transforms, transitions, animations, filters, positioning, overflow, columns,
scroll behavior, containment, and more. If a CSS property exists, it's very likely a method.

### Composing style fragments

Define a style once, reuse it everywhere with `.apply(fragment)` — the composition
primitive for design systems:

```java
// In your Theme:
public static Style<?> brandFlow() {
    return style().background(BRAND_GRADIENT)
                  .backgroundSize(percent(300), percent(100))
                  .animation(anim("gradientShift"), s(3), linear, s(0), infinite);
}

// Anywhere:
button(attrs().style().padding(SP_3).apply(brandFlow()).color(white).done(), ...)
```

Related helpers that kill common `prop("...")` strings:

```java
style().content()        // content: '' — for ::before/::after rules
style().inset(zero)      // top/right/bottom/left in one call
style().borderMask()     // mask so only the padding ring shows — gradient borders:
style().position(absolute).inset(zero)
       .borderRadius(px(12)).padding(px(2))     // 2px border thickness
       .apply(brandFlow()).borderMask()

// Transition shorthands (Tailwind-style)
style().transitionAll(s(0.2))          // transition: all 0.2s
style().transitionColors(s(0.15))      // color + background-color + border-color
style().transitionBackground(s(0.2))   // also: transitionTransform, transitionOpacity

// Accessibility + cross-browser
style().srOnly()                       // full screen-reader-only pattern (9 props)
style().backdropFilter(blur(px(10)))   // emits -webkit- prefix automatically
style().backgroundPosition(percent(0), percent(50))
```

Every keyword has a constant: `display(flex)`, `border(none)`, `alignItems(center)`,
`justifyContent(spaceBetween)`, `border(px(1), solid, hex("#e5e7eb"))`,
`gridTemplateColumns(repeat(autoFit(), minmax(px(250), fr(1))))`.

**Every property also takes a plain String**, so anything you can write in CSS you can
write here without hunting for the constant — and the CSS you already know transfers
directly:

```java
style().display("flex").cursor("copy").margin("0 auto")
       .transition("color .2s ease, transform .3s ease-out")
       .gridTemplateColumns("repeat(3, 1fr)")
```

`prop(name, value)` remains the escape hatch for anything with no method at all. (It is no
longer deprecated; `unsafeProp` is, since there was never anything unsafe about it.)

### Bare styles as element arguments

When an element only needs styling, pass `style()` directly — no
`attrs().style()....done()` ceremony, and it composes with `Attr` shortcuts:

```java
// Before
div(attrs().style().padding(SP_4).color(TEXT).done(), text("hi"))

// After
div(style().padding(SP_4).color(TEXT), text("hi"))
div(class_("card"), id("hero"), style().margin(zero), p("content"))
```

Use `attrs()` when you need chained event handlers or many attributes.

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
import jweb.css.Stylesheet;

Stylesheet sheet = Stylesheet.stylesheet()
    .variables("--primary", "#6366f1", "--radius", "8px")
    .rule("body", style().margin(zero).fontFamily("system-ui, sans-serif"))
    .rule(".hero", style().padding(rem(4)).textAlign(center))
    .keyframes(Keyframes.keyframes("gradientShift")
        .from(style().backgroundPosition(percent(0), percent(50)))
        .to(style().backgroundPosition(percent(100), percent(50))))
    .mediaQuery(MediaQuery.md(), new Stylesheet.Rule(".sidebar", style().display(block)));

// Emit:
sheet.build();          // formatted CSS
sheet.buildMinified();  // whitespace-squeezed
sheet.toStyleTag();     // "<style>…</style>" string (the sample app's Head.java
                        // uses build() inside style(...) instead)
```

Also accepts `fontFace(FontFace)`, `supports(Supports)`, `raw(css)`, `comment(text)`.

### 3. Per-element styles with pseudo-classes — `Tag.styled()`

```java
div().styled(style().padding(px(16)).backgroundColor(white))
     .hover(style().backgroundColor(hex("#f5f5f5")))
     .focus(style().outline(px(2), solid, blue))
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
import static jweb.css.MediaQuery.*;

media().minWidth(px(768)).rule(".container", style().maxWidth(px(720))).build()
md().rule(".sidebar", style().display(block)).build()      // presets: xs sm md lg xl xxl

// mobile()/tablet()/desktop() are deprecated: they were a second breakpoint set
// that overlapped xs()-xxl() with different pixel values.

media().prefersDark()
    .rule("body", style().backgroundColor(hex("#1a1a1a")).color(white)).build()
media().prefersReducedMotion()
    .rule("*", style().animationDuration(ms(0)).transitionDuration(ms(0))).build()

// Also: portrait()/landscape(), retina(), hover()/coarsePointer(), print(),
// displayMode/standalone(), and()/not()/only(), condition("raw")
```

## Container Queries (`ContainerQuery`)

```java
import static jweb.css.ContainerQuery.*;

container().minWidth(px(400)).rule(".card", style().display(flex)).build()
container("sidebar").maxWidth(px(300)).rule(".nav", style().flexDirection(column)).build()
```

## Feature Queries (`Supports`)

```java
import static jweb.css.Supports.*;

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
CSSScope.scope(".card").to(".card-footer").rule(rule("p").margin(zero))

// @property — typed custom properties (CSSProperty)
CSSProperty.register("--angle").syntax("<angle>").inherits(false).initialValue("0deg")

// @font-face (FontFace)
FontFace.fontFace("Inter").src("/fonts/inter.woff2", "woff2")
    .fontWeight(100, 900).fontDisplay("swap")

// @keyframes (Keyframes)
keyframes("fadeIn")
    .from(style().opacity(0))
    .to(style().opacity(1))
    .build();
keyframes("pulse").at(0, style().opacity(1)).at(50, style().opacity(0.5)).at(100, style().opacity(1))
// 12 presets: fadeIn/fadeOut/slideIn*/pulse/bounce/shake/spin/zoomIn/zoomOut
```

## Selectors

Three ways in, in the order you should reach for them:

```java
// 1. A plain string, when you already know the selector — full CSS parity
rule(".card:hover")
rule("li:nth-child(2n+1)")

// 2. The Selector builder, when you are composing one — chainable, keeps its type
rule(cls("card").hover())                    // .card:hover
rule(cls("input").focusVisible())
rule(tag("li").nthChild("2n+1"))
rule(cls("form").has("input:invalid"))       // :has()
// starters: select() tag() cls() id() all(); then pseudo-classes, pseudo-elements,
// attribute matches, and the combinators child()/descendant()/adjacent()/sibling()

// 3. The static Selectors helpers (deprecated) returned raw strings that you
//    concatenated with `+`. The builder above covers the same ground and composes.
```

## CSS Variables & Design Tokens (`CSSVariables`)

```java
import static jweb.Css.*;   // CSSVariables is folded into the Css facade

var("primary-color")             // var(--primary-color)   ← named var(), not var_()
var("spacing", "1rem")           // with fallback
varChain("a", "b", rem(1))       // var(--a, var(--b, 1rem)) — final fallback is a CSSValue

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

A framework-level token store (the sample app's `Theme.java` takes a simpler route —
plain `CSSValue` constants — but this class adds CSS-variable emission and dark mode):

```java
import jweb.css.Theme;

Theme theme = Theme.create()          // constructor is protected — use create()
    .color("primary", "#6366f1")
    .spacing("md", "1rem")
    .fontSize("base", "1rem")         // also: fontWeight, lineHeight, radius, shadow,
                                      // breakpoint, transition, zIndex, custom
    .dark()                           // dark-mode overrides sub-builder
        .color("primary", "#818cf8")
    .build();

theme.toCss();            // :root { --color-primary: ... }
theme.toFullCss();        // + @media (prefers-color-scheme: dark) overrides
theme.toStyleElement();   // ready-to-place <style> Element
theme.color("primary");   // CSSValue reference: var(--color-primary)
```

## `Utility` — Tailwind-style class generator (deprecated)

Generates utility-class CSS from a `Theme` and provides ~400 class-name builder methods.

Deprecated: it is a design opinion rather than CSS parity, and its coverage was uneven —
the `hover:`, `dark:` and responsive variant builders emitted class names for which
`generateCss` produced no CSS at all, so those classes silently did nothing. Use the
`Style` builder, or `Theme.create()` for design tokens.

## CSS Animations (`CSSAnimations`)

```java
import static jweb.Css.*;   // CSSAnimations is folded into the Css facade

// 11 presets, each with matching @keyframes in `Keyframes`: fadeIn, fadeOut,
// slideInLeft, slideInRight, zoomIn, zoomOut, pulse, bounce, spin, shake, plus
// rotate360 (deprecated — it emits `spin`).
//
// 29 further presets (fadeInUp, flipX, jello, tada, ...) were deleted: they had no
// keyframes behind them, so they animated nothing at all.
//
// The preset builders implement CSSValue; Style.animation(...) has the multi-arg
// (name, duration, timing, ...) forms, so presets go through prop():
style().prop("animation", fadeIn(s(1)))
style().prop("animation", slideInLeft(s(0.6)))
style().prop("animation", pulse(s(1.5)).iterationCount(infinite))
style().prop("animation", spin(s(2)).timing(linear))

// Ship the matching keyframes:
stylesheet().add(Keyframes.fadeIn()).add(Keyframes.spin())

// Builder chain: .timing() .delay() .iterationCount() .direction() .fillMode() .playState()

// Scroll-driven animations
style().prop("animation", fadeIn(s(1)))
       .animationTimeline(scrollTimeline())
       .animationRange("entry", "exit")

// Composition
composeAnimations(fadeIn(s(1)).css(), slideInLeft(s(1)).css())
staggerDelay(index, 100)
```

## Grid helpers (`CSSGrid`)

```java
import static jweb.Css.*;

style().gridTemplateColumns(repeat(3, fr(1)))
style().gridTemplateColumns(repeat(autoFill(), minmax(px(200), fr(1))))
style().gridTemplateRows(masonry())
minContent(), maxContent(), fitContent(px(300)), span(2), subgrid()
templateAreas("header header", "sidebar main", "footer footer")
```

## The Six Property-String Modules (anchor positioning, scroll snap, text wrap, subgrid, masking, logical properties)

These six modules (`CSSAnchorPositioning`, `CSSScrollSnap`, `CSSTextWrap`, `CSSSubgrid`,
`CSSMasking`, `CSSLogicalProperties`) returned complete `"property:value"` **strings** that
had to be wrapped in `.prop(...)`, and took raw strings so typed units did not fit. They are
now deprecated: every property they covered is a method on `Style`, taking a `CSSValue` or a
plain `String` like every other property.

```java
// Anchor positioning
// Anchor positioning, scroll-snap, masking, logical properties and text-wrap are
// now first-class Style methods — the string-property modules that required
// prop(...) wrapping are deprecated.
rule(".trigger").anchorName("--my-trigger")
rule(".tooltip")
    .positionAnchor("--my-trigger")
    .positionArea("top")
    .position(absolute)
    .top("anchor(--my-trigger bottom)")

// Scroll snap — note these are scroll-padding/scroll-margin in CSS, which is why
// the old snapPadding()/snapMargin() names were dropped
rule(".carousel")
    .scrollSnapType("x mandatory")
    .scrollPadding("0 20px")
    .display(flex)
    .overflowX(auto)
rule(".carousel > .slide")
    .scrollSnapAlign("start")
    .scrollSnapStop("always")

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
import jweb.css.CSSNested;   // CSSNested.rule() clashes with CSS.rule() — keep it qualified

CSSNested.rule(".card")
    .style(rule(".card").padding(px(20)))     // base declarations via a StyleBuilder
    .nest("&:hover").prop("box-shadow", "0 2px 8px rgba(0,0,0,0.1)").parent()
    .nest("& .title").prop("font-size", "1.5rem").root()
    .build();
// Also: .media(...)/.supports(...)/.container(...) nesting, and a BEM helper
// (CSSNested.block("card").element("title")...)
```
