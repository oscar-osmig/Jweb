# CSS DSL

JWeb provides a type-safe DSL for writing CSS in Java.

## Basic Usage

```java
import static jweb.Css.*;

// Single rule
String css = rule(".btn")
    .padding(px(10), px(20))
    .backgroundColor(blue)
    .color(white)
    .borderRadius(px(4))
    .build();
```

## Multiple Rules

```java
String css = styles(
    rule("*")
        .boxSizing("border-box"),
    rule("body")
        .margin(zero)
        .fontFamily("system-ui, sans-serif"),
    rule(".container")
        .maxWidth(px(1200))
        .margin(zero, auto)
        .padding(px(20))
);
```

## Units

```java
// Pixels
px(10)          // 10px

// Relative units
rem(1.5)        // 1.5rem
em(1)           // 1em
percent(100)    // 100%

// Viewport units
vh(100)         // 100vh
vw(50)          // 50vw
vmin(10)        // 10vmin
vmax(10)        // 10vmax

// Time
ms(200)         // 200ms
s(1)            // 1s

// Zero
zero            // 0
```

## Colors

### Named Colors

```java
red, blue, green, yellow, orange, purple, pink
white, black, gray, darkGray, lightGray
transparent, currentColor
```

### Custom Colors

```java
hex("#ff5733")           // Hex color
rgb(255, 87, 51)         // RGB
rgba(255, 87, 51, 0.5)   // RGBA with alpha
hsl(14, 100, 60)         // HSL
hsla(14, 100, 60, 0.5)   // HSLA with alpha
```

## Common Properties

### Layout

```java
rule(".container")
    .display("flex")
    .flexDirection("column")
    .justifyContent("center")
    .alignItems("center")
    .gap(px(20))
```

### Spacing

```java
rule(".box")
    .margin(px(10))                    // all sides
    .margin(px(10), px(20))            // vertical, horizontal
    .margin(px(10), px(20), px(30))    // top, horizontal, bottom
    .margin(px(10), px(20), px(30), px(40))  // top, right, bottom, left
    .padding(px(20))
```

### Typography

```java
rule("p")
    .fontSize(px(16))
    .fontWeight("bold")
    .lineHeight("1.5")
    .textAlign("center")
    .color(gray)
```

### Borders

```java
rule(".card")
    .border(px(1), solid, gray)
    .borderRadius(px(8))
    .boxShadow("0 2px 4px rgba(0,0,0,0.1)")
```

### Sizing

```java
rule(".box")
    .width(percent(100))
    .maxWidth(px(600))
    .height(vh(100))
    .minHeight(px(300))
```

### Positioning

```java
rule(".overlay")
    .position("fixed")
    .top(zero)
    .left(zero)
    .right(zero)
    .bottom(zero)
    .zIndex(1000)
```

### SVG Presentation

CSS properties, not attributes — they can live in a stylesheet, respond to `:hover`,
and (unlike the `fill(...)`/`stroke(...)` attributes) participate in transitions:

```java
rule("path.route")
    .fill("none")
    .stroke("currentColor")
    .strokeWidth(px(2))
    .strokeLinecap("round")
    .strokeLinejoin("round")
    .strokeDasharray("240")
    .strokeDashoffset("240")   // animate to "0" for the classic "line draw" effect
    .transformBox("fill-box")  // so transform-origin pivots on the shape, not the SVG viewport
```

### Escape Hatch

For anything with no typed method yet — vendor-prefixed, brand-new, or otherwise uncovered:

```java
rule(".card").property("interpolate-size", "allow-keywords")
```

`property(name, value)` is the general escape hatch (`var(name, value)` is only for
`--custom-properties`).

## Media Queries

```java
Stylesheet sheet = stylesheet()
    .add(Rule.of(".container", style().padding(px(10))))
    .add(media().minWidth(px(768)).rule(".container", style().padding(px(20))))
    .add(media().minWidth(px(1024)).rule(".container", style().padding(px(40))));

String css = sheet.build();
```

### Predefined Breakpoints

```java
MediaQuery.mobile()      // max-width: 767px
MediaQuery.tablet()      // min-width: 768px
MediaQuery.desktop()     // min-width: 1024px
MediaQuery.xl()          // min-width: 1200px
```

## Keyframes (Animations)

```java
String animation = keyframes("fadeIn")
    .from(style().opacity("0").transform(CSSValue.of("translateY(-10px)")))
    .to(style().opacity("1").transform(CSSValue.of("translateY(0)")))
    .build();

String css = animation + "\n" + styles(
    rule(".fade-in")
        .animation("fadeIn 0.3s ease-out")
);
```

## View Transitions

Opt a document into cross-document View Transitions (a plain multi-page navigation,
not the SPA-style `document.startViewTransition()`):

```java
import jweb.css.Stylesheet;
import static jweb.Css.*;

Stylesheet.stylesheet().add(viewTransitions())
// @view-transition{navigation:auto}
```

## Pseudo-selectors

```java
String css = styles(
    rule("a")
        .color(blue)
        .textDecoration("none"),
    rule("a:hover")
        .textDecoration("underline"),
    rule("a:focus")
        .outline("2px solid blue"),
    rule("button:disabled")
        .opacity("0.5")
        .cursor("not-allowed")
);
```

## Pseudo-elements

```java
String css = styles(
    rule(".required::before")
        .content("'*'")
        .color(red)
        .marginRight(px(4)),
    rule(".clearfix::after")
        .content("''")
        .display("table")
        .clear("both")
);
```

## Inline Styles

For element-specific styles:

```java
div(
    style().backgroundColor(blue).color(white).padding(px(10)),
    "Styled div"
)
```

## Stylesheet

Combine multiple rules into a stylesheet:

```java
Stylesheet sheet = stylesheet()
    .add(Rule.of("body", style().margin(zero)))
    .add(Rule.of(".container", style().maxWidth(px(1200))))
    .add(media().minWidth(px(768)).rule(".container", style().padding(px(40))));

String css = sheet.build();
```

## Feature Queries (@supports)

Use `@supports` for progressive enhancement:

```java
import static jweb.css.Supports.*;

// Simple property check
String css1 = supports("display", "grid")
    .rule(".container", style().display(grid))
    .build();

// Multiple conditions
String css2 = supports()
    .property("display", "grid")
    .and()
    .property("gap", "1rem")
    .rule(".grid", style().display(grid).gap(rem(1)))
    .build();

// NOT condition (fallback)
String css3 = supports()
    .not()
    .property("display", "grid")
    .rule(".fallback", style().display(flex))
    .build();

// Selector support check
String css4 = supportsSelector(":has(> img)")
    .rule(".card:has(> img)", style().padding(zero))
    .build();

// Convenience methods
supportsGrid()              // display: grid
supportsFlexbox()           // display: flex
supportsCustomProperties()  // CSS variables
supportsBackdropFilter()    // backdrop-filter
supportsHasSelector()       // :has() selector
supportsContainerQueries()  // container queries
supportsSticky()            // position: sticky
supportsClamp()             // clamp() function
```

## Nested CSS

Build CSS with native nesting syntax:

Kept qualified (`CSSNested.rule()`) because it clashes with `Css.rule()`. Child
rules open with `.nest("...")` — use `&` for the parent — and close with
`.parent()` (or `.root()` to jump all the way back to the top):

```java
import jweb.css.CSSNested;

String css = CSSNested.nest(".card")
    .prop("padding", "1rem")
    .prop("background", "#fff")

    .nest("&:hover")
        .prop("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
    .parent()

    .nest("&:focus")
        .prop("outline", "2px solid blue")
    .parent()

    .nest("& .title")                  // & .title
        .prop("font-size", "1.5rem")
    .parent()

    .nest("& > .icon")                 // & > .icon
        .prop("width", "24px")
    .parent()

    .nest("&.active")                  // &.active
        .prop("border-color", "green")
    .parent()

    .build();
```

## Using in Templates

```java
public class Layout implements Template {
    private final Element children;

    public Layout(Element children) {
        this.children = children;
    }

    @Override
    public Element render() {
        return html(
            head(
                style(getStyles())
            ),
            body(
                div(class_("container"),
                    children
                )
            )
        );
    }

    private String getStyles() {
        // Qualified: Template declares its own no-arg styles(), which shadows
        // the free Css.styles(StyleBuilder...) combiner by simple name here.
        return Css.styles(
            rule("body")
                .margin(zero)
                .fontFamily("system-ui"),
            rule(".container")
                .maxWidth(px(1200))
                .margin(zero, auto)
        );
    }
}
```
