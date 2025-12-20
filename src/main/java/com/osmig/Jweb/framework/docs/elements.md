# Elements (HTML DSL)

JWeb provides a type-safe DSL for building HTML.

## Basic Usage

```java
import static com.osmig.Jweb.framework.elements.Elements.*;

// Simple elements
div()
h1("Hello World")
p("Some text")

// With attributes
div(class_("container"), id("main"))
a(href("/about"), "About Us")
img(src("/logo.png"), alt("Logo"))
```

## Nesting Elements

```java
div(class_("card"),
    h2("Card Title"),
    p("Card content goes here"),
    button(class_("btn"), "Click Me")
)
```

## Attributes

### Common Attributes

```java
div(
    id("myDiv"),
    class_("container"),
    style("color: red"),
    title("Tooltip text")
)
```

### Form Attributes

```java
input(
    type("text"),
    name("username"),
    placeholder("Enter username"),
    required(),
    disabled(),
    readonly()
)
```

### Data Attributes

```java
div(
    data("user-id", "123"),
    data("role", "admin")
)
// Renders: <div data-user-id="123" data-role="admin"></div>
```

### ARIA Attributes

```java
button(
    aria("label", "Close dialog"),
    aria("expanded", "false")
)
```

## Event Handlers

```java
button(
    onClick("handleClick()"),
    onChange("handleChange(event)"),
    onSubmit("return validateForm()")
)
```

## Available Elements

### Document Structure
- `html()`, `head()`, `body()`, `title()`, `meta()`, `link()`, `script()`, `style()`

### Semantic Elements
- `header()`, `nav()`, `main()`, `section()`, `article()`, `aside()`, `footer()`

### Headings
- `h1()`, `h2()`, `h3()`, `h4()`, `h5()`, `h6()`

### Text Elements
- `p()`, `span()`, `div()`, `a()`, `strong()`, `em()`, `code()`, `pre()`, `blockquote()`

### Lists
- `ul()`, `ol()`, `li()`, `dl()`, `dt()`, `dd()`

### Tables
- `table()`, `thead()`, `tbody()`, `tfoot()`, `tr()`, `th()`, `td()`

### Forms
- `form()`, `input()`, `textarea()`, `select()`, `option()`, `button()`, `label()`, `fieldset()`, `legend()`

### Media
- `img()`, `video()`, `audio()`, `source()`, `iframe()`

## Conditional Rendering

```java
// Using when()
div(
    h1("Dashboard"),
    when(isAdmin, () -> button("Admin Panel"))
)

// Using ifElse()
div(
    ifElse(isLoggedIn,
        () -> span("Welcome, " + username),
        () -> a(href("/login"), "Sign In")
    )
)
```

## Loops

```java
// Render a list of items
ul(
    each(users, user -> li(user.getName()))
)

// With index
ul(
    eachIndexed(items, (item, index) ->
        li(class_(index % 2 == 0 ? "even" : "odd"), item.getName())
    )
)
```

## Raw HTML

For trusted HTML content:

```java
div(
    raw("<strong>Bold text</strong>")
)
```

**Warning:** Only use `raw()` with trusted content to prevent XSS.

## Text Content

```java
// Escaped text (safe)
p(text("User input: <script>alert('xss')</script>"))
// Renders: <p>User input: &lt;script&gt;alert('xss')&lt;/script&gt;</p>

// Simple text
p("Hello World")
```

## Components (Templates)

Create reusable components:

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
        return div(class_("card"),
            h3(class_("card-title"), title),
            p(class_("card-content"), content)
        );
    }
}

// Usage
div(
    new Card("Welcome", "Hello World!"),
    new Card("About", "Learn more...")
)
```

## Fragments

Group elements without a wrapper:

```java
fragment(
    h1("Title"),
    p("Paragraph 1"),
    p("Paragraph 2")
)
```
