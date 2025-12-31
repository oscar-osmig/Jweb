# CSS DSL

JWeb provides a type-safe DSL for writing CSS in Java.

## Basic Usage

```java
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

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
    .border(px(1), "solid", gray)
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

## Media Queries

```java
String css = styles(
    rule(".container")
        .padding(px(10)),

    media("(min-width: 768px)",
        rule(".container")
            .padding(px(20))
    ),

    media("(min-width: 1024px)",
        rule(".container")
            .padding(px(40))
    )
);
```

### Predefined Breakpoints

```java
MediaQuery.mobile()      // max-width: 767px
MediaQuery.tablet()      // min-width: 768px
MediaQuery.desktop()     // min-width: 1024px
MediaQuery.largeDesktop() // min-width: 1280px
```

## Keyframes (Animations)

```java
String animation = keyframes("fadeIn",
    frame("0%")
        .opacity("0")
        .transform("translateY(-10px)"),
    frame("100%")
        .opacity("1")
        .transform("translateY(0)")
);

String css = styles(
    animation,
    rule(".fade-in")
        .animation("fadeIn 0.3s ease-out")
);
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
    style(CSS.style()
        .backgroundColor(blue)
        .color(white)
        .padding(px(10))
    ),
    text("Styled div")
)
```

## Stylesheet

Combine multiple rules into a stylesheet:

```java
Stylesheet sheet = Stylesheet.create()
    .add(rule("body").margin(zero))
    .add(rule(".container").maxWidth(px(1200)))
    .add(media("(min-width: 768px)",
        rule(".container").padding(px(40))
    ));

String css = sheet.build();
```

## Feature Queries (@supports)

Use `@supports` for progressive enhancement:

```java
import static com.osmig.Jweb.framework.styles.Supports.*;

// Simple property check
String css = supports("display", "grid")
    .rule(".container", style().display(grid))
    .build();

// Multiple conditions
String css = supports()
    .property("display", "grid")
    .and()
    .property("gap", "1rem")
    .rule(".grid", style().display(grid).gap(rem(1)))
    .build();

// NOT condition (fallback)
String css = supports()
    .not()
    .property("display", "grid")
    .rule(".fallback", style().display(flex))
    .build();

// Selector support check
String css = supportsSelector(":has(> img)")
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

```java
import static com.osmig.Jweb.framework.styles.CSS.*;

String css = nested(".card")
    .prop("padding", "1rem")
    .prop("background", "#fff")
    .hover()                           // &:hover
        .prop("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
    .end()
    .focus()                           // &:focus
        .prop("outline", "2px solid blue")
    .end()
    .child(".title")                   // & .title
        .prop("font-size", "1.5rem")
    .end()
    .direct(".icon")                   // & > .icon
        .prop("width", "24px")
    .end()
    .and(".active")                    // &.active
        .prop("border-color", "green")
    .end()
    .build();

// Pseudo-class shortcuts
.hover()           // &:hover
.focus()           // &:focus
.active()          // &:active
.disabled()        // &:disabled
.firstChild()      // &:first-child
.lastChild()       // &:last-child
.before()          // &::before
.after()           // &::after
.placeholder()     // &::placeholder
```

## Using in Templates

```java
public class Layout implements Template {
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
        return styles(
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
