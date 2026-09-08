[← Back to README](./../README.md)

# CSS DSL

The core is `jweb.Style<T>` — the fluent property builder, ~400 methods, and the carrier for
pseudo-classes and media blocks too — plus the `jweb.Css` facade, which is one import for
keyword constants, units, colours, functions, [layout mixins](#layout-mixins) and the
modern-CSS modules. Everything that can appear on the right-hand side of a declaration
implements the one-method interface `CSSValue`; a component's class rules come back from
its [`styles()` hook](#page-owned-stylesheets); design tokens live in
[`jweb.css.Theme`](#theme--the-token-system).

## Imports

```java
import static jweb.Css.*;   // style(), rule(), units, colors, grid, animations,
                            // variables, media(), keyframes(), stylesheet()
import jweb.Style;          // the type your style helpers return
```

`jweb.Css` is the whole property/value/at-rule surface in one import. It aggregates
the property builder and keyword constants, units, colors, grid and animations, the
`var()`/`env()` references, the `media()`/`keyframes()`/`stylesheet()` factories, the
[layout mixins](#layout-mixins), and — new in 3.0 — the ten "modern CSS" modules that
used to need an import each: anchor positioning, text wrap, subgrid, logical properties,
masking, scroll snap, cascade layers, `@scope`, nesting and `@property`. Duplicate names
across the old modules (`colorMix`, `lightDark`, `var`, `env`) are resolved inside the
facade.

Builder **types** live at `jweb.css.*`: `Stylesheet`, `Theme`, `MediaQuery`,
`ContainerQuery`, `Keyframes`, `FontFace`, `Supports`, `Rule`, `Selector`. Two things
still want their own static import, because their starters would collide:

```java
import static jweb.css.Selectors.*;   // select() all() tag() cls() id() — id() is El's
import jweb.css.Theme;                // the token store; Theme.color("primary")
```

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
                  .animation("gradientShift", s(3), linear, s(0), infinite);
}

// Anywhere:
button(attrs().style(s -> s.padding(SP_3).apply(brandFlow()).color(white)), ...)
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

`prop(name, value)` writes any property by name — a vendor-prefixed one, or a spec so new
the DSL has not caught up. There is nothing unsafe or second-class about it: a `CSSValue`
is itself a `() -> String`, so the typed overload is exactly as expressive. `property(name,
value)` is the same call under a more discoverable name; `var(name, value)` is the one for
custom properties (`--name`). (`unsafeProp` is deprecated — there was never anything
unsafe about it.)

### SVG presentation properties

`fill`, `stroke`, `strokeWidth`, `strokeDasharray`, `strokeDashoffset`, `strokeLinecap`,
`strokeLinejoin` and `transformBox` are CSS properties, distinct from the same-named
`fill(...)`/`stroke(...)` **attributes** (`Attr`/`HtmlAttributes`) — as properties they can
live in a stylesheet, respond to `:hover`, and participate in transitions:

```java
rule("path.route")
    .fill("none").stroke(hex("#6366f1")).strokeWidth(px(2))
    .strokeLinecap("round").strokeLinejoin("round")
    .strokeDasharray("240").strokeDashoffset("240")
    .transition("stroke-dashoffset 1.2s ease-out")

rule("path.route.drawn").strokeDashoffset("0")   // add/remove the class to animate the draw-in
```

Also new: `font("italic bold 14px/1.5 Georgia, serif")` (the `font` shorthand), `justifySelf`,
`textRendering`, and the legacy `clip("rect(0, 0, 0, 0)")` — still the standard
`position(absolute).clip(...).overflow("hidden")` screen-reader-only pattern.

### Bare styles as element arguments

When an element only needs styling, pass `style()` directly — it composes with `Attr`
shortcuts, and it is now the only spelling: the `attrs().style()` starter and the
`.done()` that ended it are gone in 3.0.

```java
// Before (2.2.3)
div(attrs().style().padding(SP_4).color(TEXT).done(), text("hi"))

// After
div(style().padding(SP_4).color(TEXT), "hi")
div(cls("card"), id("hero"), style().margin(zero), p("content"))
```

Inside an `attrs()` chain the style is a lambda, which keeps the chain going:

```java
div(attrs().cls("card").style(s -> s.display(flex).gap(rem(1))).id("main"), content)
```

Use `attrs()` when you need chained event handlers or many attributes.

## Page-owned stylesheets

A component owns the class rules it needs. Override `styles()` and the render collects it
— the page's, its layout's, and every `Template` rendered inside them — dedupes by
content hash, and emits **one** nonce-stamped `<style>` at the end of `<head>`. Ten
instances of a card contribute one copy.

```java
public class Card implements Template {

    @Override
    public Element render() { return div(class_("card"), h3(title), p(body)); }

    @Override
    public Stylesheet styles() {
        return stylesheet()
            .rule(".card", style().padding(rem(1)).borderRadius(px(12)))
            .rule(".card:hover", style().boxShadow(zero, px(4), px(12), rgba(0, 0, 0, 0.1)))
            .add(md().rule(".card", style().padding(rem(1.5))));
    }
}
```

A layout's stylesheet is where the reset and the design tokens belong, so no page has to
remember to include them:

```java
public class Layout implements Template {
    @Override
    public Stylesheet styles() {
        return stylesheet()
            .add(Theme.TOKENS)
            .rule("*, *::before, *::after", style().boxSizing(borderBox).margin(zero))
            .rule("body", style().color(TEXT).backgroundColor(BG));
    }
}
```

A swap fragment, a streamed Suspense block and a WebSocket DOM patch each carry the CSS
their own render introduced — a `<style>` inserted through `innerHTML` applies, unlike a
script. Nothing is sent twice: each render path drains only what it has not delivered.

Outside a page render (a bare `toHtml()`, a static export) there is nowhere to deliver
rules, so a conditional style keeps only its plain declarations.

## Pseudo-classes and media on an inline style

An inline style can carry the things a `style=` attribute cannot express. They get a
generated class named after a content hash of the rules, the rules go into the page's
collected stylesheet, and the plain declarations still ride the attribute.

```java
a(href("/docs"), style()
        .color(PRIMARY).padding(SP_2, SP_4).borderRadius(ROUNDED)
        .hover(style().backgroundColor(hex("#eef2ff")))
        .focusVisible(style().outline(px(2), solid, PRIMARY))
        .at(md(), style().padding(SP_3, SP_6))
        .dark(style().color(hex("#a5b4fc"))),
    "Documentation")

// <a href="/docs" class="j-3f9a1c" style="color: var(--color-primary); …">
// .j-3f9a1c:hover{background-color:#eef2ff}
// .j-3f9a1c:focus-visible{outline:2px solid var(--color-primary)}
// @media (min-width: 768px){.j-3f9a1c{padding:0.75rem 1.5rem}}
// @media (prefers-color-scheme: dark){.j-3f9a1c{color:#a5b4fc}}
```

Because the class name is the hash, fifty buttons with the same hover rule share one
class and one rule block, and the name is identical in every render — a swap fragment
naming a class the page already carries is a no-op by construction.

| method | emits |
| --- | --- |
| `hover` `focus` `focusVisible` `focusWithin` `active` `visited` `disabled` `checked` | the matching pseudo-class |
| `placeholder` | `::placeholder` |
| `popoverOpen` | `:popover-open` |
| `before(content, style)` / `after(content, style)` | `::before` / `::after`, with `content` quoted for you |
| `on(":nth-child(2n)", style)` | anything else — write the colons |
| `at(md(), style)` / `at(container("card")…, style)` | inside that query |
| `dark(style)` | `@media (prefers-color-scheme: dark)` |
| `reducedMotion(style)` | `@media (prefers-reduced-motion: reduce)` |
| `startingStyle(style)` | `@starting-style` |

They nest — a `hover` inside an `at(...)` is wrapped by that query too — and `apply()`
carries them, so a mixin can bring its own hover state:

```java
static Style<?> chip() {
    return row(SP_2).padding(SP_1, SP_3).borderRadius(ROUNDED)
                    .hover(style().backgroundColor(hex("#eef2ff")));
}

span(chip().color(TEXT), "Java 21")      // the hover comes along
```

## Layout mixins

Every app writes `display: flex; align-items: center` a hundred times. These are that,
named. Each returns a `Style`, so it composes with `apply()`, with tokens, and with the
conditional rules above.

```java
row()                  // flex, items centred
row(SP_4)              // …with a gap
stack()                // flex column
stack(SP_4)            // …with a gap
center()               // centred on both axes
cluster(SP_2)          // a row that wraps — chips, tags, a toolbar
container(px(1200))    // full width up to a maximum, then centred
card()                 // surface: background, hairline border, radius, padding
truncate()             // one line, ellipsis
truncate(3)            // three lines, ellipsis
srOnly()               // visually hidden, still read aloud
fullBleed()            // break out of the container to the viewport width
aspect(16, 9)          // a fixed ratio
cover() / contain()    // fill or fit a box — images and video
grid(3, SP_4)          // three equal columns
autoGrid(px(240))      // as many columns as fit — no breakpoints needed
autoGrid(px(240), SP_4)
```

```java
// Before
div(style().display(flex).flexDirection(column).gap(SP_4)
           .maxWidth(px(500)).margin(zero, auto), …)

// After
div(stack(SP_4).maxWidth(px(500)).marginInline(auto), …)
```

`card()` reads `--color-surface`, `--color-border`, `--radius-lg` and `--space-4` with
neutral fallbacks, so it picks up a `Theme` when one is defined and still works when it
is not.

## CSS Rules and Stylesheets

Two more mechanisms, for CSS that is not attached to a component:

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

### 2. `Stylesheet` — the accumulator

```java
import jweb.css.Stylesheet;

Stylesheet sheet = Stylesheet.stylesheet()
    .add(TOKENS)                                  // a Theme
    .rule("body", style().margin(zero).fontFamily("system-ui, sans-serif"))
    .rule(".hero", style().padding(rem(4)).textAlign(center))
    .startingStyle(".toast", style().opacity(0))
    .add(keyframes("gradientShift")
        .from(style().backgroundPosition(percent(0), percent(50)))
        .to(style().backgroundPosition(percent(100), percent(50))))
    .add(md().rule(".sidebar", style().display(block)));

// Emit:
sheet.build();          // formatted CSS — what styles() hands back
sheet.buildMinified();  // whitespace-squeezed
sheet.toStyleTag();     // "<style>…</style>" string
```

`add(...)` is the one verb for every at-rule builder — a `media()`/`md()` query that
already carries its rules, `container(...)`, `keyframes(...)`, `fontFace(...)`,
`supports(...)`, a `Theme`. `raw(css)` and `comment(text)` append text.

Returning the stylesheet from `styles()` is almost always better than placing it by hand:
it is deduped, it lands in `<head>`, and it carries the CSP nonce.

## CSS Units (`CSSUnits`)

```java
px(16), rem(1.5), em(1.2), percent(50), ch(60), ex(2), lh(1), rlh(1)
vh(100), vw(50), vmin(10), vmax(10)
dvh(100), dvw(50), svh(100), lvh(100)         // dynamic/small/large viewport
cqw(10), cqh(10), cqi(10), cqb(10), cqmin(5), cqmax(5)   // container query units
fr(1), num(1.5)
ms(300), s(0.5), deg(45), rad(1.57), turn(0.25)
auto, zero, none, inherit, initial, unset
CSSValue.of("anything")                        // typed escape hatch — rarely needed, since
                                                // every property also takes a plain String

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

// query() is the condition line without the rules — what style().at(md(), …) wraps
md().query()                        // "@media (min-width: 768px)"
```

A breakpoint that only changes one element does not need a rule at all:

```java
h1(style().fontSize(rem(1.9)).at(md(), style().fontSize(rem(2.5))), "Title")
```

## Container Queries (`ContainerQuery`)

```java
import static jweb.css.ContainerQuery.*;

container().minWidth(px(400)).rule(".card", style().display(flex)).build()
container("sidebar").maxWidth(px(300)).rule(".nav", style().flexDirection(column)).build()

// Style queries — match on a custom property, not a size
container("card").style("variant", "featured")
    .rule(".title", style().fontWeight(700)).build()
// @container card style(--variant: featured) { … }

container("card").minWidth(px(400)).query()   // the condition line, for style().at(…)
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
// @layer — cascade layers. All of these reach through `import static jweb.Css.*`
CSSLayer.order("reset", "base", "components", "utilities")
CSSLayer.layer("components", rule(".btn").padding(px(8)))

// @scope
CSSScope.scope(".card").to(".card-footer").rule(rule("p").margin(zero))

// @property — typed custom properties. The per-type shortcuts are named
// *Property (colorProperty, lengthProperty, …) so they don't collide with the
// unit and colour statics.
CSSProperty.register("--angle").syntax("<angle>").inherits(false).initialValue("0deg")
colorProperty("--brand").initialValue("#4f46e5")

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

// @view-transition — opts this document into cross-document View Transitions
// (a plain navigation, not the SPA-style Navigation.startViewTransition()).
// Without it, ::view-transition-* rules never fire on a full page navigation.
stylesheet().add(viewTransitions())     // @view-transition{navigation:auto}
```

## Selectors

Three ways in, in the order you should reach for them:

```java
// 1. A plain string, when you already know the selector — full CSS parity
rule(".card:hover")
rule("li:nth-child(2n+1)")

// 2. The Selector builder, when you are composing one — chainable, keeps its type
import static jweb.css.Selectors.*;          // the starters live here, not in Css:
                                             // id/tag/select are also HTML DSL names
rule(cls("card").hover())                    // .card:hover
rule(cls("input").focusVisible())
rule(tag("li").nthChild("2n+1"))
rule(cls("form").has("input:invalid"))       // :has()
// starters: select() tag() cls() id() all(); then pseudo-classes, pseudo-elements,
// attribute matches, and the combinators child()/descendant()/adjacent()/sibling()

// 3. The static Selectors helpers (deprecated) returned raw strings that you
//    concatenated with `+`. The builder above covers the same ground and composes.
```

## CSS Variables

```java
import static jweb.Css.*;

var("primary-color")             // var(--primary-color)   ← named var(), not var_()
var("spacing", "1rem")           // with fallback
varChain("a", "b", rem(1))       // var(--a, var(--b, 1rem)) — final fallback is a CSSValue
env("safe-area-inset-bottom")    // env(), same shapes
```

3.0 removed the three competing token builders that also lived here —
`designSystem()`, `theme()`/`ThemeBuilder`, and the `scoped()`/`component()`/`theme(name)`
string-concatenation helpers. `jweb.css.Theme` below is the one that stayed.

## `Theme` — the token system

Design tokens are CSS custom properties. Write the set once, emit it through a page's
stylesheet, and read tokens back as `CSSValue`s — so the value lives in the document
(devtools shows it, a toggle can change it) instead of being inlined at every use site.

```java
import jweb.css.Theme;

public static final Theme TOKENS = Theme.light()
    .color("primary", hex("#4f46e5"))
    .color("text",    hex("#1e293b"))
    .color("bg",      hex("#ffffff"))
    .space("4",       rem(1))
    .radius("md",     px(6))
    .text("lg",       rem(1.125))
    .dark(Theme.dark()
        .color("text", hex("#e2e8f0"))
        .color("bg",   hex("#0f172a")));
```

Groups and their prefixes: `color` → `--color-*`, `space` → `--space-*`,
`radius` → `--radius-*`, `font` → `--font-*`, `text` → `--text-*` (the type scale),
`shadow` → `--shadow-*`, `gradient` → `--gradient-*`. `token(name, value)` defines any
other name verbatim.

```java
// emit — a layout's stylesheet is the natural home
public Stylesheet styles() { return stylesheet().add(TOKENS); }

// read
Theme.color("primary")          // var(--color-primary)
Theme.space("4")                // var(--space-4)
Theme.var("gap", rem(1))        // var(--gap, 1rem)
TOKENS.valueOf("color-primary") // "#4f46e5" — the literal, for a <meta theme-color>
```

`css()` emits three blocks: the light values on `:root`, the dark values under
`prefers-color-scheme` (guarded with `:root:not([data-theme=light])` so an explicit light
choice still wins), and the same dark values under `:root[data-theme=dark]` so a toggle
overrides the system setting in both directions.

An app keeps its own constant names — the demo app's `Theme` is `TOKENS` plus
`PRIMARY`, `SP_4`, `ROUNDED` and friends, each a one-line `var()` reference:

```java
public static final CSSValue PRIMARY = Theme.color("primary");
public static final CSSValue SP_4    = Theme.space("4");
```

## `Utility` — Tailwind-style class generator (deprecated)

Generates utility-class CSS and provides ~400 class-name builder methods.

Deprecated: it is a design opinion rather than CSS parity, and its coverage was uneven —
the `hover:`, `dark:` and responsive variant builders emitted class names for which
`generateCss` produced no CSS at all, so those classes silently did nothing. Use the
`Style` builder and the [layout mixins](#layout-mixins), with `Theme` for tokens.

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

## The modern-CSS modules, folded in

Ten modules — anchor positioning, text wrap, subgrid, logical properties, masking,
scroll snap, cascade layers, `@scope`, nesting, `@property` — used to be ten separate
static imports whose methods returned raw `"property:value"` **strings**, so they only
composed through `.prop(...)`. In 3.0 they are part of the `Css` chain and they are typed:
a method that produces a **declaration** returns a `Style`, one that produces a **value**
returns a `CSSValue`.

```java
import static jweb.Css.*;    // that is the whole import

// Declarations compose with apply(...)
style().apply(textWrapBalance())            // text-wrap: balance
style().apply(hyphensAuto())
style().apply(snapAlignStart()).apply(snapStopAlways())
style().apply(subgridColumns())             // grid-template-columns: subgrid
style().apply(clipCircle("50%"))
style().apply(marginBlock(rem(1)))          // logical properties, RTL-safe
style().apply(maskImage("linear-gradient(black 60%, transparent)"))

// Values slot into any property
style().top(anchor("--menu", "bottom"))     // anchor(--menu bottom)
style().width(anchorSize("--menu", "width"))
style().clipPath(clipHexagon())
```

Anchor positioning and `@property`:

```java
rule(".trigger").apply(anchorName("--menu"))
rule(".tooltip")
    .apply(positionAnchor("--menu"))
    .apply(positionArea("top"))
    .position(absolute)
    .top(anchor("--menu", "bottom"))

// @property registration — the type methods are *Property to keep out of the
// way of the unit and colour statics of the same name
colorProperty("--brand").initialValue("#4f46e5").inherits(true)
lengthProperty("--gap").initialValue("1rem")
```

Cascade layers, `@scope` and nesting:

```java
CSSLayer.order("reset", "base", "components", "utilities")
CSSLayer.layer("components", rule(".btn").padding(px(8)))
CSSScope.scope(".card").rule("h2", style().fontSize(rem(1.5)))

// nest(...) — it was CSSNested.rule(...), which collided with Css.rule(...)
nest(".card")
    .style(style().padding(rem(1)))
    .nest("&:hover").style(style().transform(scale(1.02)))
    .parent()
    .build()
```

`CSSMasking`'s preset shapes (`clipDiamond`, `clipPentagon`, `clipHexagon`, `clipStar`,
`clipTriangle*`) encode the polygon coordinates so you don't have to.

Most of these properties are *also* plain `Style` methods (`textWrap(balance)`,
`marginBlock(rem(1))`, `clipPath(...)`, `scrollSnapType("x mandatory")`) — use whichever
reads better; the module functions earn their keep for the presets and the value
functions.

## Properties added in 3.0

```java
style().fieldSizing(content)                       // an input that grows as you type
style().scrollbarColor(hex("#94a3b8"), transparent)
style().scrollbarWidth(thin)
style().scrollbarGutter(stable)                    // or stableBothEdges
style().textBoxTrim(trimBoth).textBoxEdge(capAlphabetic)
style().textBox(trimBoth, capAlphabetic)           // the shorthand
style().interpolateSize(allowKeywords)             // animate to/from auto, fit-content
style().anchorScope("all")                         // confine anchor names to a subtree
style().viewTransitionClass("card")                // one rule for every card
style().borderRight(none)                          // one-value border sides (also L/T/B)
```

At-rules and selectors:

```java
// @starting-style — the values a transition animates FROM on first render
div(style().opacity(1).transition(propAll, ms(200), ease)
           .startingStyle(style().opacity(0)), "I fade in")

stylesheet().rule(".toast", style().opacity(1).transition(propAll, ms(200), ease))
            .startingStyle(".toast", style().opacity(0))

// :popover-open / :open
cls("menu").popoverOpen()          // .menu:popover-open
tag("details").open()              // details:open
div(popover("auto"), style().opacity(0).popoverOpen(style().opacity(1)), "menu")
```

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

## Nested CSS

```java
import static jweb.Css.*;    // nest() lives on the facade like everything else

nest(".card")
    .style(rule(".card").padding(px(20)))     // base declarations via a StyleBuilder
    .nest("&:hover").prop("box-shadow", "0 2px 8px rgba(0,0,0,0.1)").parent()
    .nest("& .title").prop("font-size", "1.5rem").root()
    .build();
// Also: .media(...)/.supports(...)/.container(...) nesting, and a BEM helper
// (block("card").element("title")...)
```
