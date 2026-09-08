# Elements (HTML DSL)

JWeb provides a type-safe DSL for building HTML.

## Basic Usage

```java
import static jweb.El.*;

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
import static jweb.Js.*;

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

## Forms

A record is the form: it declares the fields, their types and their rules,
and the same record renders the form and binds the submission back to a typed
value. See [templates.md](./templates.md) and
[validation.md](./validation.md) for the full picture; in short:

```java
public record SignupForm(
    @Form.Required @Form.Email String email,
    @Form.Required @Form.Password @Form.Length(min = 8) String password) {}

form(SignupForm.class)
    .action("/signup")
    .submit("Sign up")

// app.post("/signup", req -> { Form.Bound<SignupForm> bound = Form.bind(SignupForm.class, req); ... });
```

An input outside a `<form>` is just the element with its attributes — no
builder needed:

```java
input(type("email"), name("email"), id("email"), placeholder("you@example.com"), required())
input(type("checkbox"), name("remember"), id("remember"), value("yes")), label(for_("remember"), "Remember me")
input(type("radio"), name("plan"), id("plan-basic"), value("basic")), label(for_("plan-basic"), "Basic Plan")
input(type("hidden"), name("csrf"), value(token))
input(type("range"), name("volume"), min("0"), max("100"))
```

## Batch Class Application

Apply multiple classes at once using `classes()`, which joins parts and skips
nulls, blanks, and non-matching `when(...)` branches:

```java
// Multiple classes
div(classes("card", "featured", "animate"),
    h2("Featured Item")
)

// Conditional classes — a part may be a String, null, or a when(...) branch
div(classes("btn", when(isActive, "active"), when(isPrimary, "primary")),
    "Click me"
)

// The same, built with attrs().classIf(condition, name) — condition first
div(
    attrs().cls("card").classIf(isFeatured, "featured").classIf(isDisabled, "disabled"),
    "Card content"
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

// Either/or, both branches lazy
div(
    when(isLoggedIn,
        () -> span("Welcome, " + username),
        () -> a(href("/login"), "Sign In")
    )
)

// The chained form, for a branch too long to read as one expression
when(isLoggedIn)
    .then(span("Welcome, " + username))
    .otherwise(a(href("/login"), "Sign In"))
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

A bare String child is escaped text — there is no separate function to call:

```java
// Escaped text (safe)
p("User input: <script>alert('xss')</script>")
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
