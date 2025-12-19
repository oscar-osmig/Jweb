# JWeb Framework

A pure Java web framework with type-safe DSLs for HTML, CSS, and JavaScript. Write your entire frontend in Java with compile-time safety and IDE support.

## Features

- **Type-safe HTML** - Build HTML with Java methods, no string templates
- **Type-safe CSS** - CSS properties as methods with unit validation
- **Type-safe JavaScript** - Generate JS code from Java with full IDE support
- **Component-based** - Create reusable components with the `Template` interface
- **No build tools** - Just Maven and Java, no webpack/npm required
- **Pure client-side** - Works without WebSocket for simple use cases

## Quick Start

```java
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

public class HelloWorld implements Template {
    @Override
    public Element render() {
        return div()
            .id("app")
            .class_("container")
            .children(
                h1().text("Hello, JWeb!"),
                p().text("Built with pure Java")
            );
    }
}
```

## DSL Syntax

### HTML DSL

Create HTML elements with a fluent builder pattern:

```java
// Basic elements
div().id("main").class_("container").text("Hello")

// Nested children
div()
    .id("card")
    .children(
        h1().text("Title"),
        p().text("Content"),
        button()
            .onclick("handleClick()")
            .text("Click me")
    )

// Attributes
input()
    .type("text")
    .name("username")
    .placeholder("Enter name")
    .required()

// Conditional rendering
div()
    .when(isAdmin, () -> button().text("Delete"))
    .ifElse(isLoggedIn,
        () -> span().text("Welcome!"),
        () -> a().href("/login").text("Sign In"))

// Loop over items
ul().each(users, user -> li().text(user.getName()))
```

### CSS DSL

Write CSS with type-safe properties and units:

```java
import static com.osmig.Jweb.framework.styles.CSS2.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

// Single rule
String css = rule(".btn")
    .padding(px(10), px(20))
    .backgroundColor(blue)
    .color(white)
    .borderRadius(px(4))
    .build();

// Multiple rules
String css = styles(
    rule("*")
        .boxSizing(borderBox),
    rule("body")
        .margin(zero)
        .fontFamily("system-ui"),
    rule(".container")
        .maxWidth(px(1200))
        .margin(zero, auto),
    rule("a")
        .color(blue)
        .textDecoration(none),
    rule("a:hover")
        .textDecoration(underline)
);
```

**Available units:**
- `px(10)` - pixels
- `rem(1.5)` - root em
- `em(1)` - em
- `percent(100)` - percentage
- `vh(100)`, `vw(100)` - viewport units
- `ms(200)`, `s(1)` - time units
- `zero` - 0

**Available colors:**
- Named: `red`, `blue`, `green`, `white`, `black`, `gray`, `transparent`
- Hex: `hex("#ff5733")`
- RGB: `rgb(255, 87, 51)`
- RGBA: `rgba(255, 87, 51, 0.5)`

### JavaScript DSL

Generate JavaScript from Java with full type safety:

```java
import static com.osmig.Jweb.framework.js.JS.*;

// Define functions
Func increment = func("increment")
    .inc("count")
    .set(el("display").text(), variable("count"));

Func reset = func("reset")
    .set("count", 0)
    .call("updateDisplay");

// Build script
String js = script()
    .var_("count", 0)
    .add(increment)
    .add(reset)
    .build();
```

**Variables and values:**
```java
variable("count")     // Variable reference: count
str("hello")          // String literal: 'hello'
null_()               // null
this_()               // this
array(1, 2, 3)        // Array: [1, 2, 3]
```

**DOM access:**
```java
el("myId")            // document.getElementById('myId')
query(".selector")    // document.querySelector('.selector')
el("id").text()       // .textContent
el("id").value()      // .value
el("id").html()       // .innerHTML
```

**Operations:**
```java
variable("x").plus(1)           // (x + 1)
variable("x").minus(1)          // (x - 1)
variable("x").times(2)          // (x * 2)
variable("x").div(2)            // (x / 2)
variable("x").mod(2)            // (x % 2)

variable("x").eq(5)             // (x === 5)
variable("x").neq(5)            // (x !== 5)
variable("x").gt(5)             // (x > 5)
variable("x").lt(5)             // (x < 5)

variable("a").and(variable("b")) // (a && b)
variable("a").or(variable("b"))  // (a || b)
variable("x").not()              // (!x)

variable("x").ternary(a, b)      // (x ? a : b)
```

**Control flow:**
```java
func("example")
    .var_("x", 10)
    .if_(variable("x").gt(5), ret(str("big")))
    .ifElse(condition,
        new Object[]{ /* then */ },
        new Object[]{ /* else */ })
    .ret(variable("result"))
```

**Timers:**
```java
setInterval(callback().call("tick"), 1000)
setTimeout(callback().call("done"), 5000)
clearInterval(variable("timerId"))
```

## Components

Create reusable components by implementing `Template`:

```java
public class Card implements Template {
    private final String title;
    private final String content;

    public Card(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Element render() {
        return div()
            .class_("card")
            .children(
                h2().text(title),
                p().text(content)
            );
    }
}

// Usage
new Card("Welcome", "Hello world!").render()
```

## Inline Styles

Apply styles directly to elements:

```java
import static com.osmig.Jweb.framework.styles.Styles.style;

div()
    .style(style()
        .display(flex)
        .gap(px(16))
        .padding(rem(1))
        .backgroundColor(hex("#f5f5f5")))
    .text("Styled div")
```

## Complete Example

Here's a counter component with HTML, CSS, and JS:

```java
public class Counter implements Template {

    @Override
    public Element render() {
        return div()
            .children(
                Elements.style(counterStyles()),
                span().id("count").class_("display").text("0"),
                div()
                    .class_("buttons")
                    .children(
                        button().onclick("decrement()").text("-"),
                        button().onclick("reset()").text("Reset"),
                        button().onclick("increment()").text("+")
                    ),
                inlineScript(counterScript())
            );
    }

    private String counterStyles() {
        return styles(
            rule(".display")
                .fontSize(rem(3))
                .fontWeight(700),
            rule(".buttons")
                .display(flex)
                .gap(px(8))
        );
    }

    private String counterScript() {
        Func updateDisplay = func("updateDisplay")
            .set(el("count").text(), variable("count"));

        Func increment = func("increment")
            .inc("count")
            .call("updateDisplay");

        Func decrement = func("decrement")
            .dec("count")
            .call("updateDisplay");

        Func reset = func("reset")
            .set("count", 0)
            .call("updateDisplay");

        return script()
            .var_("count", 0)
            .add(updateDisplay)
            .add(increment)
            .add(decrement)
            .add(reset)
            .build();
    }
}
```

## Project Structure

```
src/main/java/com/osmig/Jweb/
├── app/
│   ├── pages/          # Page components
│   ├── partials/       # Reusable UI components
│   │   └── demo/       # Demo components
│   ├── Routes.java     # Route definitions
│   └── Theme.java      # Design tokens
└── framework/
    ├── core/           # Core interfaces (Element, Renderable)
    ├── elements/       # HTML element builders (Tag, Elements)
    ├── styles/         # CSS DSL (Style, CSS2, CSSUnits, CSSColors)
    ├── js/             # JavaScript DSL (JS)
    ├── vdom/           # Virtual DOM implementation
    ├── server/         # HTTP server
    └── template/       # Template interface
```

## Running the Application

```bash
mvn compile exec:java -Dexec.mainClass="com.osmig.Jweb.Main"
```

Then open `http://localhost:8080` in your browser.

## Requirements

- Java 17+
- Maven 3.6+

## License

MIT
