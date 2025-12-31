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

### String-based Handlers

```java
button(
    onClick("handleClick()"),
    onChange("handleChange(event)"),
    onSubmit("return validateForm()")
)
```

### Action-based Handlers

Using the Actions DSL for type-safe event handling:

```java
import static com.osmig.Jweb.framework.js.Actions.*;

// Click with action
button(
    onClick(call("doSomething")),
    "Click Me"
)

// Multiple actions
button(
    onClick(all(
        call("showLoading"),
        fetch("/api/data").ok(call("updateUI")),
        call("hideLoading")
    )),
    "Load Data"
)

// Toggle visibility
button(
    onClick(toggle("menu-panel")),
    "Toggle Menu"
)

// Form submission with validation
form(
    onSubmit(all(
        preventDefault(),
        validate("myForm"),
        fetch("/api/submit").post().ok(call("onSuccess"))
    )),
    // form fields...
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

## Form Input Builders

Type-safe form input builders for common input types:

```java
import static com.osmig.Jweb.framework.elements.Elements.*;

// Text inputs
textInput("username")                    // <input type="text" name="username">
textInput("search", "Search...")         // With placeholder
emailInput("email")                      // <input type="email" name="email">
passwordInput("password")                // <input type="password" name="password">
numberInput("age")                       // <input type="number" name="age">
telInput("phone")                        // <input type="tel" name="phone">
urlInput("website")                      // <input type="url" name="website">
searchInput("q")                         // <input type="search" name="q">

// Selection inputs
checkbox("remember", "Remember me")       // Checkbox with label
radio("plan", "basic", "Basic Plan")      // Radio with value and label

// Date/time inputs
dateInput("birthdate")                    // <input type="date" name="birthdate">
timeInput("startTime")                    // <input type="time" name="startTime">
datetimeInput("appointment")              // <input type="datetime-local" name="appointment">

// Other inputs
hiddenInput("csrf", token)                // <input type="hidden" name="csrf" value="...">
fileInput("document")                     // <input type="file" name="document">
colorInput("theme")                       // <input type="color" name="theme">
rangeInput("volume", 0, 100)              // <input type="range" name="volume" min="0" max="100">

// Field wrapper (label + input + error)
field("Email", emailInput("email").required())
field("Password", passwordInput("pwd"), "Must be 8+ characters")
```

### Chaining Input Methods

```java
textInput("username")
    .id("user-input")
    .required()
    .minLength(3)
    .maxLength(50)
    .pattern("[a-z]+")
    .autocomplete("username")
    .build()

numberInput("quantity")
    .min(1)
    .max(100)
    .step(1)
    .value("1")
    .build()
```

## Batch Class Application

Apply multiple classes at once using `classes()`:

```java
// Multiple classes
div(classes("card", "featured", "animate"),
    h2("Featured Item")
)

// Conditional classes
div(classes("btn", isActive ? "active" : null, isPrimary ? "primary" : null),
    "Click me"
)

// Using class_() with condition
div(
    class_("card"),
    class_("featured", isFeatured),  // Only added if isFeatured is true
    class_("disabled", isDisabled),
    text("Card content")
)
```

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
