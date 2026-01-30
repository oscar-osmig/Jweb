[← Back to README](./../README.md)

# HTML DSL

## Imports

```java
// The ONE import you need for HTML elements
import static com.osmig.Jweb.framework.elements.El.*;

// Additional imports for styling
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
```

## Elements

The `El` class is the single entry point that re-exports all element factory methods (24 modules):

```java
import static com.osmig.Jweb.framework.elements.El.*;

// Simple elements
div(class_("container"),
    h1("Welcome"),
    p("Hello, World!")
)

// Nested structure
nav(class_("navbar"),
    ul(
        li(a("/", "Home")),
        li(a("/about", "About")),
        li(a("/contact", "Contact"))
    )
)

// Tables
table(class_("data-table"),
    thead(
        tr(th("Name"), th("Email"), th("Role"))
    ),
    tbody(
        tr(td("John"), td("john@example.com"), td("Admin")),
        tr(td("Jane"), td("jane@example.com"), td("User"))
    )
)
```

**Available Elements (24 modules):**

| Category | Elements |
|----------|----------|
| **Document** | `html`, `head`, `body`, `title`, `meta`, `link`, `script`, `style`, `css`, `inlineScript`, `icon`, `appleIcon` |
| **Semantic** | `header`, `footer`, `nav`, `main`, `section`, `article`, `aside`, `hgroup`, `search`, `address` |
| **Text** | `h1`-`h6`, `p`, `span`, `div`, `strong`, `em`, `code`, `pre`, `small`, `a`, `br`, `time`, `wbr` |
| **Lists** | `ul`, `ol`, `li` |
| **Tables** | `table`, `thead`, `tbody`, `tfoot`, `tr`, `th`, `td` |
| **Forms** | `form`, `input`, `textarea`, `select`, `option`, `button`, `label` |
| **Media** | `img`, `video`, `audio`, `canvas`, `svg`, `iframe` |
| **SVG** | `svg`, `path`, `circle`, `rect`, `line`, `polyline`, `polygon`, `g` |
| **Modern HTML5** | `dialog`, `details`, `summary`, `meter`, `progress`, `template`, `slot`, `output`, `data`, `bdi`, `bdo`, `ruby`, `rt`, `rp` |
| **Popover** | `autoPopover`, `manualPopover`, `popoverToggleButton`, `popoverShowButton`, `popoverHideButton` |
| **Responsive Images** | `picture`, `source`, `responsiveImg`, `lazyImg` |
| **Figure** | `figure`, `figcaption` |
| **Definition** | `dl`, `dt`, `dd` |
| **Interactive Text** | `abbr`, `dfn`, `cite`, `q`, `blockquote`, `kbd`, `samp`, `var_`, `mark`, `sub`, `sup`, `ins`, `del`, `s` |
| **Form Enhancements** | `datalist`, `optgroup`, `fieldset`, `legend`, `colorInput`, `dateInput`, `timeInput`, `datetimeInput`, `monthInput`, `weekInput`, `rangeInput` |
| **Helpers** | `text`, `raw`, `fragment`, `each`, `tag` |

## Attributes

```java
// Simple attributes (shortcuts)
div(class_("card"), id("main"),
    h2("Title"),
    p("Content")
)

// Fluent attribute builder
div(attrs()
    .class_("card")
    .id("main")
    .data("user-id", "123")
    .aria("label", "User card"),
    content
)

// Form elements with attributes
form(attrs().action("/submit").method("POST"),
    label(for_("email"), "Email:"),
    input(attrs()
        .type("email")
        .name("email")
        .placeholder("you@example.com")
        .required()),
    button(type("submit"), "Subscribe")
)

// Conditional class shortcuts
div(attrs()
    .class_("btn")
    .classIf("active", isActive)           // adds "active" if isActive is true
    .classToggle(isOpen, "open", "closed") // adds "open" or "closed"
)

// Layout shortcuts (no style builder needed!)
div(attrs().flexCenter(), ...)              // centered flexbox
div(attrs().flexColumn("1rem"), ...)        // column with gap
div(attrs().flexRow("0.5rem"), ...)         // row with gap
div(attrs().flexBetween(), ...)             // space-between
div(attrs().gridCols(3, "1rem"), ...)       // 3-column grid with gap
```

## Modern HTML5 Elements

```java
import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.elements.DialogHelper.*;
import static com.osmig.Jweb.framework.elements.DetailsHelper.*;

// Dialog (modal)
dialog(attrs().id("confirm-dialog"),
    h2("Confirm Action"),
    p("Are you sure?"),
    button(attrs().onclick(close("confirm-dialog")), "Cancel"),
    button(attrs().onclick(close("confirm-dialog", "confirmed")), "Confirm")
)
button(attrs().onclick(showModal("confirm-dialog")), "Open Dialog")

// Details/Summary (accordion)
details(attrs().name("faq"),  // name attribute creates exclusive accordion
    summary("Question 1"),
    p("Answer 1")
)
details(attrs().name("faq"),
    summary("Question 2"),
    p("Answer 2")
)

// Progress and Meter
progress(70, 100)  // Determinate progress bar
progressIndeterminate()  // Loading spinner
meter(0.6, 0, 1)  // Scalar measurement

// Time with datetime
timeWithDatetime("2026-01-29", "January 29, 2026")

// Data element for machine-readable values
data("SKU-123", "Product Widget")
```

## Popover API

```java
import static com.osmig.Jweb.framework.elements.PopoverElements.*;

// Auto popover (closes when clicking outside)
div(autoPopover("my-popover"),
    p("Popover content here")
)
button(popoverTarget("my-popover"), "Toggle")

// Manual popover (requires explicit close)
div(manualPopover("my-tooltip"),
    p("This stays open until explicitly closed")
)

// Popover toggle/show/hide buttons
popoverToggleButton("my-popover", "Toggle Popover")
popoverShowButton("my-popover", "Show Popover")
popoverHideButton("my-popover", "Hide Popover")

// JS helpers for programmatic control
showPopover("my-popover")    // JS: document.getElementById('my-popover').showPopover()
hidePopover("my-popover")    // JS: document.getElementById('my-popover').hidePopover()
togglePopover("my-popover")  // JS: document.getElementById('my-popover').togglePopover()
```

## Responsive Images

```java
import static com.osmig.Jweb.framework.elements.PictureElements.*;

// Picture element with multiple sources
picture(
    source(srcset("image.avif"), type("image/avif")),
    source(srcset("image.webp"), type("image/webp")),
    img("image.jpg", "Fallback description")
)

// Responsive image with srcset
responsiveImg("image.jpg", "Description",
    "image-320.jpg 320w",
    "image-640.jpg 640w",
    "image-1024.jpg 1024w"
)

// Lazy-loaded image
lazyImg("image.jpg", "Description")
```

## Definition Lists

```java
import static com.osmig.Jweb.framework.elements.DefinitionElements.*;

// Glossary
dl(
    dt("HTML"), dd("HyperText Markup Language"),
    dt("CSS"), dd("Cascading Style Sheets"),
    dt("JS"), dd("JavaScript")
)

// Metadata key-value pairs
dl(class_("metadata"),
    dt("Author"), dd("Jane Doe"),
    dt("Published"), dd("2026-01-29"),
    dt("Category"), dd("Technology")
)
```

## Figure and Caption

```java
import static com.osmig.Jweb.framework.elements.FigureElements.*;

// Image with caption
figure(
    img(attrs().src("chart.png").attr("alt", "Sales chart")),
    figcaption("Figure 1: Quarterly sales data")
)

// Code listing with caption
figure(class_("code-example"),
    pre(code("const x = 42;")),
    figcaption("Example: Variable declaration")
)
```

## Interactive Text Elements

```java
import static com.osmig.Jweb.framework.elements.InteractiveElements.*;

// Abbreviation with expansion
p("The ", abbr("HTML", "HyperText Markup Language"), " specification")

// Citation
p("As described in ", cite("The Art of Programming"), "...")

// Keyboard input
p("Press ", kbd("Ctrl"), "+", kbd("C"), " to copy.")

// Highlighted text
p("Search results for: ", mark("JWeb framework"))

// Inline quotation (browser adds quotes automatically)
p("She said, ", q("Hello World"), ", and the program ran.")

// Subscript and superscript
p("H", sub("2"), "O")  // H2O
p("E = mc", sup("2"))  // E = mc2

// Inserted and deleted text (for showing edits)
p(del("old price: $20"), " ", ins("new price: $15"))
```

## Form Enhancements

```java
import static com.osmig.Jweb.framework.elements.FormEnhancements.*;

// Datalist for autocomplete suggestions
input(attrs().attr("list", "browsers")),
datalist("browsers",
    option("Chrome"),
    option("Firefox"),
    option("Safari"),
    option("Edge")
)

// Option groups in select
select(attrs().name("car"),
    optgroup("Swedish Cars",
        option(attrs().value("volvo"), "Volvo"),
        option(attrs().value("saab"), "Saab")
    ),
    optgroup("German Cars",
        option(attrs().value("bmw"), "BMW"),
        option(attrs().value("audi"), "Audi")
    )
)

// Fieldset with legend for form grouping
fieldset(
    legend("Personal Information"),
    label(attrs().for_("name"), "Name:"),
    input(attrs().type("text").name("name").id("name")),
    label(attrs().for_("email"), "Email:"),
    input(attrs().type("email").name("email").id("email"))
)

// Specialized input helpers
colorInput("theme-color", "#3b82f6")       // Color picker
dateInput("birthday")                       // Date input
dateInput("event", "2026-01-01", "2026-12-31")  // Date with min/max
timeInput("meeting-time")                   // Time input
datetimeInput("appointment")                // Date+time input
monthInput("birth-month")                   // Month picker
weekInput("work-week")                      // Week picker
rangeInput("volume", 0, 100, 50)            // Range slider
rangeInput("opacity", 0, 100, 50, 5)        // Range with step
```

## Form Input Builders

```java
// Fluent input builders via attrs() pattern
input(attrs().type("email").name("email").placeholder("you@example.com").required())
textarea(attrs().name("message").rows(5).placeholder("Your message"))
select(attrs().name("role"), option("User"), option("Admin"))
```

## Conditional Rendering

```java
// Simple conditional
when(isLoggedIn, () -> span("Welcome, " + user.getName()))

// If/elif/else chain
when(isAdmin)
    .then(adminPanel())
    .elif(isModerator, modPanel())
    .elif(isUser, userPanel())
    .otherwise(loginPrompt())

// Pattern matching style
match(
    cond(isAdmin, adminPanel()),
    cond(isModerator, modPanel()),
    cond(isUser, userPanel()),
    otherwise(loginPrompt())
)
```

## Collection Iteration

```java
// Map a collection to elements
ul(each(users, user ->
    li(class_("user-item"),
        strong(user.getName()),
        span(" - " + user.getEmail())
    )
))
```

## Fragments

```java
// Group multiple elements without a wrapper
fragment(
    h1("Title"),
    p("First paragraph"),
    p("Second paragraph")
)
```

## Error Boundaries

```java
errorBoundary(
    () -> riskyComponent.render(),
    error -> p("Error: " + error.getMessage())
)
```
