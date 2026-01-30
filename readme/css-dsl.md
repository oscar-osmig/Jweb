[← Back to README](./../README.md)

# CSS DSL

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

// Style shortcuts
div(attrs().style(s -> s
    .size(px(100))           // width: 100px; height: 100px;
    .shadow()                // default shadow preset
    .roundedLg()             // border-radius: 8px
))
```

**Style Shortcuts:**

| Method | Output |
|--------|--------|
| `size(px(100))` | `width: 100px; height: 100px` |
| `minSize(px(50))` | `min-width: 50px; min-height: 50px` |
| `maxSize(px(500))` | `max-width: 500px; max-height: 500px` |
| `widthRange(px(200), px(800))` | `min-width: 200px; max-width: 800px` |
| `fullWidth()` | `width: 100%` |
| `fullViewportHeight()` | `height: 100vh` |

**Shadow Presets:** `shadowXs()`, `shadowSm()`, `shadow()`, `shadowMd()`, `shadowLg()`, `shadowXl()`, `shadowInner()`, `shadowNone()`

**Border Radius Presets:** `roundedNone()`, `roundedXs()`, `roundedSm()`, `roundedMd()`, `roundedLg()`, `roundedXl()`, `rounded2xl()`, `rounded3xl()`, `roundedFull()`

**Style Mixin Interfaces:**

| Mixin Interface | Properties |
|-----------------|------------|
| `StyleBoxModel` | `margin`, `padding`, `border`, `width`, `height`, `boxSizing`, `size()`, `widthRange()` |
| `StyleFlex` | `display(flex)`, `flexDirection`, `justifyContent`, `alignItems`, `gap`, `flexWrap` |
| `StyleGrid` | `gridTemplateColumns`, `gridTemplateRows`, `gridArea`, `gridGap` |
| `StyleTypography` | `color`, `fontSize`, `fontWeight`, `lineHeight`, `textAlign` |
| `StyleEffects` | `transform`, `transition`, `animation`, `boxShadow`, `filter`, `opacity` |
| `StylePosition` | `position`, `top`, `right`, `bottom`, `left`, `inset`, `zIndex` |

## CSS Rules

```java
import static com.osmig.Jweb.framework.styles.CSS.*;

String styles = styles(
    rule(".container")
        .maxWidth(px(1200))
        .margin(zero, auto)
        .padding(px(20)),

    rule(".button")
        .display(inlineBlock)
        .padding(px(10), px(20))
        .backgroundColor(blue(500))
        .color(white)
        .borderRadius(px(4)),

    rule(".button:hover")
        .backgroundColor(blue(600))
);
```

## CSS Units

```java
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

px(16), pt(12)              // Absolute units
rem(1.5), em(1.2)           // Relative units
percent(50), ch(60)         // Relative units
vh(100), vw(50), dvh(100)   // Viewport units
fr(1)                       // Grid units
ms(300), s(0.5)             // Time units
auto, zero, none, inherit   // Special values
calc("100% - 20px")         // CSS functions
clamp(rem(1), vw(4), rem(2))
```

## CSS Colors

```java
import static com.osmig.Jweb.framework.styles.CSSColors.*;

white, black, transparent, currentColor   // Named colors
red(500), blue(300), gray(100)            // Color palette (50-900 shades)
hex("#3b82f6")                            // Hex color
rgb(59, 130, 246)                         // RGB
rgba(59, 130, 246, 0.5)                   // RGBA
hsl(217, 91, 60)                          // HSL
oklch(0.7, 0.15, 200)                     // OKLCH
colorMix("srgb", red(500), blue(500), 50) // Color mixing
lightDark(white, black)                   // Theme-aware color
```

## Media Queries

```java
import static com.osmig.Jweb.framework.styles.MediaQuery.*;

media().minWidth(px(768)).rule(".container", style().maxWidth(px(720)))
md().rule(".sidebar", style().display(block))
media().prefersDark()
    .rule("body", style().backgroundColor(hex("#1a1a1a")).color(white))
media().prefersReducedMotion()
    .rule("*", style().animationDuration(ms(0)).transitionDuration(ms(0)))
```

## Keyframes

```java
import static com.osmig.Jweb.framework.styles.Keyframes.*;

// Define keyframe animations
keyframes("fadeIn")
    .from(style().opacity(0))
    .to(style().opacity(1))
    .build();
```

## Feature Queries (@supports)

```java
import static com.osmig.Jweb.framework.styles.Supports.*;

supports("display", "grid").rule(".container", style().display(grid)).build();
supportsGrid()
supportsFlexbox()
supportsCustomProperties()
supportsBackdropFilter()
supportsContainerQueries()
```

## Nested CSS

```java
import static com.osmig.Jweb.framework.styles.CSSNested.*;

// Native CSS nesting syntax support
rule(".card")
    .padding(px(20))
    .nested("&:hover", style().boxShadow("0 2px 8px rgba(0,0,0,0.1)"))
    .nested("& .title", style().fontSize(rem(1.5)));
```

## CSS Variables

```java
import static com.osmig.Jweb.framework.styles.CSSVariables.*;

var_("primary-color")            // var(--primary-color)
var_("spacing", "1rem")          // var(--spacing, 1rem)

designSystem()
    .spacing("xs", "0.25rem").spacing("md", "1rem")
    .color("primary", "#3b82f6").color("secondary", "#10b981")
    .fontFamily("sans", "system-ui, sans-serif")
    .build();

themeBuilder()
    .light("bg", "white").light("text", "black")
    .dark("bg", "black").dark("text", "white")
    .build();
```

## CSS Animations

```java
import static com.osmig.Jweb.framework.styles.CSSAnimations.*;

// Pre-built animations (40+)
style().animation(fadeIn(s(1)))
style().animation(slideInLeft(s(0.6)))
style().animation(pulse(s(1.5)).iterationCount(iterationInfinite))
style().animation(bounce(s(1)))
style().animation(rotate360(s(2)).timing(timingLinear))

// Scroll-driven animations
style().animation(fadeIn(s(1)))
       .animationTimeline(scrollTimeline())
       .animationRange("entry", "exit")
```

## Anchor Positioning

```java
import static com.osmig.Jweb.framework.styles.CSSAnchorPositioning.*;

// Define an anchor
rule(".trigger").prop(anchorName("--my-trigger"))

// Position element relative to anchor
rule(".tooltip")
    .prop(positionAnchor("--my-trigger"))
    .position("absolute")
    .prop(top(anchor("--my-trigger", "bottom")))
    .prop(left(anchor("--my-trigger", "left")))

// Simpler positioning with position-area
rule(".tooltip")
    .prop(positionAnchor("--my-trigger"))
    .prop(positionArea("top"))

// Fallback positioning when space runs out
positionFallback("--tooltip-fallback",
    tryTactic("flip-block"),
    tryTactic("flip-inline"),
    tryTactic("flip-block", "flip-inline"))
```

## Scroll Snap

```java
import static com.osmig.Jweb.framework.styles.CSSScrollSnap.*;

// Horizontal carousel with snap
rule(".carousel")
    .prop(snapTypeX("mandatory"))
    .prop(snapPadding("0 20px"))
    .display("flex").overflowX("auto")

rule(".carousel > .slide")
    .prop(snapAlign("start"))
    .prop(snapStop("always"))

// Vertical page snap
rule(".pages")
    .prop(snapTypeY("mandatory"))
    .height("100vh").overflowY("auto")

rule(".pages > section")
    .prop(snapAlign("start"))
    .height("100vh")
```

## Text Wrapping

```java
import static com.osmig.Jweb.framework.styles.CSSTextWrap.*;

// Balanced headlines (even line lengths)
rule("h1, h2, h3").prop(textWrapBalance())

// Pretty paragraph wrapping (avoids orphans)
rule("p").prop(textWrapPretty())

// Prevent wrapping
rule(".nowrap").prop(textWrapNowrap())

// Line clamping (truncate to N lines with ellipsis)
rule(".excerpt").prop(lineClamp(3))

// Word break control
rule(".break-all").prop(wordBreakAll())
```

## Subgrid

```java
import static com.osmig.Jweb.framework.styles.CSSSubgrid.*;

// Parent grid
rule(".grid")
    .display("grid")
    .prop(gridTemplateColumns("1fr 2fr 1fr"))

// Child inherits parent columns
rule(".grid > .item")
    .display("grid")
    .gridColumn("1 / -1")
    .prop(subgridColumns())

// Child inherits both axes
rule(".grid > .item")
    .display("grid")
    .prop(subgridBoth())
```

## Masking and Clipping

```java
import static com.osmig.Jweb.framework.styles.CSSMasking.*;

// Gradient mask (fade out at bottom)
rule(".fade-bottom")
    .prop(maskImage("linear-gradient(black 60%, transparent)"))

// Clip path shapes
rule(".circle").prop(clipCircle("50%"))
rule(".triangle").prop(clipPolygon("50% 0%", "0% 100%", "100% 100%"))
rule(".rounded-clip").prop(clipInset("10px", "8px"))

// Preset shapes
rule(".diamond").prop(clipDiamond())
rule(".pentagon").prop(clipPentagon())
rule(".hexagon").prop(clipHexagon())
rule(".star").prop(clipStar())
```

## Logical Properties

```java
import static com.osmig.Jweb.framework.styles.CSSLogicalProperties.*;

// Flow-relative properties (work in LTR and RTL)
rule(".card")
    .prop(marginBlock("1rem"))       // top/bottom margin
    .prop(marginInline("auto"))      // left/right margin
    .prop(paddingBlock("2rem"))
    .prop(paddingInline("1rem"))

// Logical sizing
rule(".container")
    .prop(inlineSize("100%"))        // width in horizontal, height in vertical
    .prop(maxInlineSize("1200px"))
    .prop(blockSize("auto"))

// Logical borders
rule(".item")
    .prop(borderInlineStart("2px solid blue"))  // left border in LTR, right in RTL
    .prop(borderBlockEnd("1px solid gray"))

// Logical positioning
rule(".overlay")
    .position("absolute")
    .prop(insetBlock("0"))
    .prop(insetInline("0"))
```
