# JWeb Framework

**Version 1.0.0**

A pure Java web framework that lets you build full-stack web applications entirely in Java. No HTML templates, no JSP, no Thymeleaf—just type-safe Java code with compile-time safety and full IDE support.

JWeb brings modern frontend concepts (component model, reactive state, virtual DOM) to server-side Java development, giving you the productivity of React/Vue with the power and safety of Java.

---

## Table of Contents

- [Philosophy](#philosophy)
- [Features](#features)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
  - [Core Rendering System](#core-rendering-system)
  - [Virtual DOM](#virtual-dom)
- [HTML DSL](#html-dsl)
  - [Elements](#elements)
  - [Attributes](#attributes)
  - [Conditional Rendering](#conditional-rendering)
  - [Collection Iteration](#collection-iteration)
  - [Fragments](#fragments)
  - [Error Boundaries](#error-boundaries)
- [CSS DSL](#css-dsl)
  - [Inline Styles](#inline-styles)
  - [CSS Rules](#css-rules)
  - [CSS Units](#css-units)
  - [CSS Colors](#css-colors)
  - [Media Queries](#media-queries)
  - [Keyframes](#keyframes)
- [JavaScript DSL](#javascript-dsl)
  - [Form Handlers](#form-handlers)
  - [External Service Forms](#external-service-forms)
  - [Click Handlers](#click-handlers)
  - [Actions](#actions)
  - [Script Builder](#script-builder)
- [Components & Templates](#components--templates)
  - [Template Interface](#template-interface)
  - [Page Components](#page-components)
  - [Layouts](#layouts)
  - [Partials](#partials)
- [Reactive State](#reactive-state)
  - [Creating State](#creating-state)
  - [State Updates](#state-updates)
  - [State Binding](#state-binding)
- [Event Handling](#event-handling)
  - [Event Types](#event-types)
  - [Event Object](#event-object)
  - [Form Handling](#form-handling)
- [Routing](#routing)
  - [Page Routes](#page-routes)
  - [API Routes](#api-routes)
  - [Route Parameters](#route-parameters)
- [Middleware](#middleware)
  - [Built-in Middleware](#built-in-middleware)
  - [Custom Middleware](#custom-middleware)
- [REST API](#rest-api)
  - [REST Annotations](#rest-annotations)
  - [Request Handling](#request-handling)
- [Database Integration](#database-integration)
  - [MongoDB](#mongodb)
  - [Schema Definition](#schema-definition)
- [Security](#security)
  - [JWT Authentication](#jwt-authentication)
  - [CSRF Protection](#csrf-protection)
  - [Password Hashing](#password-hashing)
- [Validation](#validation)
  - [Built-in Validators](#built-in-validators)
  - [Chaining Validators](#chaining-validators)
  - [Form Validation](#form-validation)
- [Additional Features](#additional-features)
- [Development Tools](#development-tools)
  - [Hot Reload](#hot-reload)
  - [Debug Mode](#debug-mode)
  - [Prefetch](#prefetch)
- [Configuration](#configuration)
  - [Application YAML](#application-yaml)
- [Project Structure](#project-structure)
- [Running the Application](#running-the-application)
- [Requirements](#requirements)
- [License](#license)

---

## Philosophy

JWeb is built on these core principles:

1. **Pure Java** - Your entire frontend is Java code. No context switching between languages.
2. **Type Safety** - Catch errors at compile time, not runtime. Your IDE becomes your best friend.
3. **Fluent DSL** - Expressive, readable APIs that feel natural to write.
4. **Component-Based** - Build UIs from composable, reusable pieces.
5. **Reactive** - State changes automatically propagate to the UI.
6. **Minimal Dependencies** - Built on Spring Boot, no additional JavaScript toolchain required.

---

## Features

- **Type-safe HTML** - Build HTML with Java methods, no string templates
- **Type-safe CSS** - CSS properties as methods with unit validation
- **Type-safe JavaScript** - Generate JS code from Java with full IDE support
- **Component-based** - Create reusable components with the `Template` interface
- **Reactive State** - React-like state management with `State<T>`
- **Virtual DOM** - Efficient rendering with VNode diffing
- **Routing System** - Fluent route definitions with path parameters
- **Middleware System** - Request processing pipeline with built-in middleware
- **REST API** - Annotation-based REST controllers
- **MongoDB Integration** - Fluent DSL for MongoDB operations
- **JWT Authentication** - Token-based auth with middleware
- **Validation Framework** - Fluent API for input validation
- **CSRF Protection** - Built-in cross-site request forgery prevention
- **Internationalization** - i18n support with locale detection
- **File Uploads** - Easy multipart file handling with validation
- **Email** - Fluent email builder with templates
- **Background Jobs** - Async task execution with scheduling
- **Testing Utilities** - Mock requests and assertions for testing
- **Accessibility** - WCAG 2.1 compliance helpers
- **Hot Reload** - Development mode with automatic reloading
- **No build tools** - Just Maven and Java, no webpack/npm required

---

## Quick Start

```java
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

public class HelloWorld implements Template {
    @Override
    public Element render() {
        return div(class_("container"),
            h1("Hello, JWeb!"),
            p("Built with pure Java")
        );
    }
}
```

### Define Routes

```java
@Component
public class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        // Middleware
        app.use(Middlewares.logging())
           .use(Middlewares.cors());

        // Pages with layout
        app.layout(MainLayout.class)
           .pages(
               "/", HomePage.class,
               "/about", AboutPage.class,
               "/contact", ContactPage.class
           );

        // API endpoints
        app.get("/api/users", req -> Response.json(userService.findAll()))
           .post("/api/users", req -> {
               User user = req.bodyAs(User.class);
               return Response.json(userService.save(user));
           });
    }
}
```

---

## Architecture

### Core Rendering System

JWeb uses a hierarchical rendering system with these core interfaces:

| Interface | Purpose |
|-----------|---------|
| `Element` | Base interface for anything that can be rendered to HTML |
| `Renderable` | Elements that produce output |
| `Template` | Reusable UI components with a `render()` method |
| `Page` | Full-page templates that define `head()` and `body()` |
| `Component` | Stateful components with lifecycle |

```
Element (interface)
    └── Template (interface) - render() returns Element
            ├── Page - Full page with head/body
            ├── Layout - Wraps pages with common structure
            └── (your components)
```

### Virtual DOM

JWeb uses a Virtual DOM (VNode) for efficient rendering:

| VNode Type | Purpose |
|------------|---------|
| `VElement` | Represents an HTML element with tag, attributes, children |
| `VText` | Represents escaped text content |
| `VRaw` | Represents raw/unescaped HTML |
| `VFragment` | Groups multiple nodes without a wrapper element |

The Virtual DOM enables:
- Efficient HTML generation
- Server-side rendering with proper escaping
- Future client-side diffing capabilities

---

## HTML DSL

### Elements

Import the static factory methods to build HTML:

```java
import static com.osmig.Jweb.framework.elements.Elements.*;

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

**Available Elements:**

| Category | Elements |
|----------|----------|
| **Document** | `html`, `head`, `body`, `title`, `meta`, `link`, `script`, `style` |
| **Semantic** | `header`, `footer`, `nav`, `main`, `section`, `article`, `aside`, `figure`, `figcaption` |
| **Headings** | `h1`, `h2`, `h3`, `h4`, `h5`, `h6` |
| **Text** | `p`, `span`, `div`, `strong`, `em`, `code`, `pre`, `blockquote`, `small`, `mark` |
| **Links** | `a` (with shorthand for href) |
| **Lists** | `ul`, `ol`, `li`, `dl`, `dt`, `dd` |
| **Tables** | `table`, `thead`, `tbody`, `tfoot`, `tr`, `th`, `td`, `caption` |
| **Forms** | `form`, `input`, `textarea`, `select`, `option`, `button`, `label`, `fieldset`, `legend` |
| **Media** | `img`, `video`, `audio`, `canvas`, `svg`, `iframe`, `picture` |
| **Interactive** | `details`, `summary`, `dialog`, `menu` |

### Attributes

Use the `attrs()` builder for complex attribute combinations:

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

// Links with security
a(attrs()
    .href("https://external.com")
    .targetBlank(), // Adds target="_blank" rel="noopener noreferrer"
    text("External Link")
)
```

**Attribute Categories:**

| Category | Attributes |
|----------|------------|
| **Core** | `id`, `class_`, `style`, `title_`, `data`, `aria` |
| **Links** | `href`, `target`, `targetBlank`, `rel`, `download` |
| **Forms** | `type`, `name`, `value`, `placeholder`, `action`, `method`, `for_` |
| **Validation** | `required`, `pattern`, `min`, `max`, `minlength`, `maxlength` |
| **Boolean** | `disabled`, `checked`, `readonly`, `hidden`, `autofocus` |
| **Media** | `src`, `alt`, `width`, `height`, `controls`, `autoplay` |
| **ARIA** | `role`, `aria(name, value)` |
| **Table** | `colspan`, `rowspan`, `scope` |
| **SVG** | `viewBox`, `fill`, `stroke`, `d`, `cx`, `cy`, `r` |

### Conditional Rendering

```java
// Simple conditional
when(isLoggedIn, () -> span("Welcome, " + user.getName()))

// Eager evaluation
when(showBanner, div(class_("banner"), "Special Offer!"))

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

### Collection Iteration

```java
// Map a collection to elements
ul(each(users, user ->
    li(class_("user-item"),
        strong(user.getName()),
        span(" - " + user.getEmail())
    )
))

// Table rows from data
tbody(each(products, product ->
    tr(
        td(product.getName()),
        td(product.getPrice().toString()),
        td(button(attrs().onClick(e -> addToCart(product)), "Add"))
    )
))
```

### Fragments

Group multiple elements without a wrapper:

```java
fragment(
    h1("Title"),
    p("First paragraph"),
    p("Second paragraph")
)
```

### Error Boundaries

Handle rendering errors gracefully:

```java
// With error handler
errorBoundary(
    () -> riskyComponent.render(),
    error -> p("Error: " + error.getMessage())
)

// With static fallback
errorBoundary(
    () -> riskyComponent.render(),
    p("Something went wrong")
)

// Silent failure (renders nothing on error)
tryCatch(() -> mayFailComponent.render())
```

---

## CSS DSL

### Inline Styles

Three ways to add inline styles:

```java
// 1. Lambda style (no .done() needed)
div(attrs()
    .class_("card")
    .style(s -> s
        .display(flex)
        .padding(px(20))
        .backgroundColor(white)),
    content
)

// 2. Fluent chain with .done()
div(attrs()
    .style()
        .display(flex)
        .padding(px(20))
    .done()
    .class_("card"),
    content
)

// 3. Direct use (auto-finalizes)
div(attrs().style()
        .display(flex)
        .padding(px(20)),
    content
)
```

**Available CSS Properties:**

| Category | Properties |
|----------|------------|
| **Box Model** | `margin`, `padding`, `border`, `width`, `height`, `minWidth`, `maxWidth` |
| **Flexbox** | `display(flex)`, `flexDirection`, `justifyContent`, `alignItems`, `gap`, `flexWrap` |
| **Grid** | `gridTemplateColumns`, `gridTemplateRows`, `gridArea`, `gridGap` |
| **Positioning** | `position`, `top`, `right`, `bottom`, `left`, `inset`, `zIndex` |
| **Typography** | `color`, `fontSize`, `fontWeight`, `lineHeight`, `textAlign`, `fontFamily` |
| **Background** | `background`, `backgroundColor`, `backgroundImage`, `backgroundSize` |
| **Effects** | `transform`, `transition`, `animation`, `boxShadow`, `filter`, `opacity` |
| **Borders** | `borderRadius`, `borderWidth`, `borderStyle`, `borderColor` |
| **Logical** | `marginInline`, `paddingBlock`, `insetInline` |

### CSS Rules

Create reusable CSS rules:

```java
import static com.osmig.Jweb.framework.styles.CSS.*;

// Single rule
String cardStyle = rule(".card")
    .display(flex)
    .flexDirection(column)
    .padding(px(20))
    .borderRadius(px(8))
    .boxShadow("0 2px 4px rgba(0,0,0,0.1)")
    .build();

// Multiple rules
String styles = rules(
    rule(".container")
        .maxWidth(px(1200))
        .margin(zero, auto)
        .padding(px(20)),

    rule(".button")
        .display(inlineBlock)
        .padding(px(10), px(20))
        .backgroundColor(blue(500))
        .color(white)
        .borderRadius(px(4))
        .cursor(pointer),

    rule(".button:hover")
        .backgroundColor(blue(600))
);
```

### CSS Units

All CSS units are type-safe:

```java
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

// Absolute units
px(16)          // 16px
pt(12)          // 12pt

// Relative units
rem(1.5)        // 1.5rem (relative to root font size)
em(1.2)         // 1.2em (relative to parent font size)
percent(50)     // 50%
ch(60)          // 60ch (width of "0" character)

// Viewport units
vh(100)         // 100vh (viewport height)
vw(50)          // 50vw (viewport width)
vmin(10)        // 10vmin
vmax(10)        // 10vmax
dvh(100)        // 100dvh (dynamic viewport height)

// Grid units
fr(1)           // 1fr (fraction of available space)

// Time units
ms(300)         // 300ms
s(0.5)          // 0.5s

// Angle units
deg(45)         // 45deg
turn(0.5)       // 0.5turn

// Special values
auto            // auto
zero            // 0
none            // none
inherit         // inherit

// CSS functions
calc("100% - 20px")
min(px(300), percent(100))
max(px(200), percent(50))
clamp(rem(1), vw(4), rem(2))
```

### CSS Colors

Full color palette with shades:

```java
import static com.osmig.Jweb.framework.styles.CSSColors.*;

// Named colors
white           // #ffffff
black           // #000000
transparent     // transparent
currentColor    // currentColor

// Color palette with shades (50-900)
red(500)        // Standard red
blue(300)       // Light blue
green(700)      // Dark green
gray(100)       // Very light gray

// Custom colors
hex("#3b82f6")
rgb(59, 130, 246)
rgba(59, 130, 246, 0.5)
hsl(217, 91, 60)
hsla(217, 91, 60, 0.8)

// Modern color functions
oklch(0.7, 0.15, 200)
colorMix("srgb", red(500), blue(500), 50)
lightDark(white, black)  // Theme-aware
```

**Available Color Palettes:**
`red`, `orange`, `amber`, `yellow`, `lime`, `green`, `emerald`, `teal`, `cyan`, `sky`, `blue`, `indigo`, `violet`, `purple`, `fuchsia`, `pink`, `rose`, `slate`, `gray`, `zinc`, `neutral`, `stone`

### Media Queries

```java
import static com.osmig.Jweb.framework.styles.MediaQuery.*;

// Breakpoints
media().minWidth(px(768)).rule(".container",
    new Style<>().maxWidth(px(720))
)

// Predefined breakpoints
md().rule(".sidebar", new Style<>().display(block))
lg().rule(".container", new Style<>().maxWidth(px(960)))
mobile().rule(".nav", new Style<>().flexDirection(column))

// Combined conditions
media()
    .screen()
    .minWidth(px(1024))
    .maxWidth(px(1280))
    .rule(".content", new Style<>().padding(px(40)))

// Print styles
media().print().rule("body", new Style<>().fontSize(pt(12)))

// Dark mode
media().prefersDark()
    .rule("body", new Style<>()
        .backgroundColor(hex("#1a1a1a"))
        .color(white))

// Reduced motion (accessibility)
media().prefersReducedMotion()
    .rule("*", new Style<>()
        .animationDuration(ms(0))
        .transitionDuration(ms(0)))

// Device capabilities
media().hover().rule(".button", new Style<>().cursor(pointer))
media().coarsePointer().rule(".button", new Style<>().padding(px(20)))
```

**Predefined Breakpoints:**
| Method | Width |
|--------|-------|
| `xs()` | max-width: 575px |
| `sm()` | min-width: 576px |
| `md()` | min-width: 768px |
| `lg()` | min-width: 992px |
| `xl()` | min-width: 1200px |
| `xxl()` | min-width: 1400px |
| `mobile()` | max-width: 767px |
| `tablet()` | 768px - 1023px |
| `desktop()` | min-width: 1024px |

### Keyframes

```java
import static com.osmig.Jweb.framework.styles.Keyframes.*;

String fadeIn = keyframes("fadeIn")
    .from(new Style<>().opacity(num(0)))
    .to(new Style<>().opacity(num(1)))
    .build();

String pulse = keyframes("pulse")
    .frame(0, new Style<>().transform(scale(1)))
    .frame(50, new Style<>().transform(scale(1.1)))
    .frame(100, new Style<>().transform(scale(1)))
    .build();
```

---

## JavaScript DSL

JWeb provides a type-safe JavaScript DSL for client-side interactions without writing raw JavaScript.

### Form Handlers

Handle form submissions with built-in loading states and error handling:

```java
import static com.osmig.Jweb.framework.js.Actions.*;

// Form submission to API endpoint
script()
    .withHelpers()
    .add(onSubmit("login-form")
        .loading("Logging in...")
        .post("/api/login")
        .withFormData()
        .ok(all(
            showMessage("status").success("Login successful!"),
            navigateTo("/dashboard")))
        .fail(showMessage("status").error("Invalid credentials")))
    .build();
```

### External Service Forms

Handle forms that call external services (e.g., EmailJS, Stripe):

```java
// External service form handler (e.g., EmailJS)
script()
    .withHelpers()
    .add(onSubmitExternal("contact-form")
        .loading("Sending...")
        .service("emailjs")
        .call("send", "'service_id'", "'template_id'",
            "{name:$_('name').value,email:$_('email').value,message:$_('message').value}")
        .ok(all(
            showMessage("form-status").success("Message sent successfully!"),
            resetForm("contact-form")))
        .fail(showMessage("form-status").error("Failed to send. Please try again."))
        .notAvailable(showMessage("form-status").error("Email service not available.")))
    .build();
```

### Click Handlers

Handle button clicks and element interactions:

```java
// Simple click handler
onClick("delete-btn")
    .confirm("Are you sure you want to delete?")
    .post("/api/delete")
    .ok(reload())
    .fail(showMessage("error").error("Delete failed"))

// Toggle visibility
onClick("toggle-btn").toggle("panel")

// Show/hide elements
onClick("show-btn").show("modal")
onClick("close-btn").hide("modal")
```

### Actions

Built-in actions for common UI operations:

| Action | Description |
|--------|-------------|
| `showMessage(id).success(text)` | Show success message with green styling |
| `showMessage(id).error(text)` | Show error message with red styling |
| `showMessage(id).warning(text)` | Show warning message with yellow styling |
| `resetForm(id)` | Reset form fields |
| `navigateTo(url)` | Navigate to URL |
| `reload()` | Reload current page |
| `show(id)` | Show element |
| `hide(id)` | Hide element |
| `toggle(id)` | Toggle element visibility |
| `setText(id, text)` | Set element text content |
| `addClass(id, class)` | Add CSS class |
| `removeClass(id, class)` | Remove CSS class |
| `focus(id)` | Focus element |
| `copyToClipboard(id)` | Copy input value to clipboard |
| `all(actions...)` | Combine multiple actions |

### Script Builder

Build complete scripts with multiple handlers:

```java
script()
    .withHelpers()                           // Add $_ helper and utilities
    .state(state()                           // Define state variables
        .var("isLoggedIn", false)
        .var("username", ""))
    .refs(refs()                             // Define element references
        .add("form", "login-form")
        .add("status", "status-message"))
    .add(onSubmit("login-form")              // Form handler
        .loading("...")
        .post("/api/login")
        .withFormData()
        .ok(navigateTo("/dashboard")))
    .add(onClick("logout-btn")               // Click handler
        .post("/api/logout")
        .ok(navigateTo("/")))
    .build();
```

---

## Components & Templates

### Template Interface

The `Template` interface is the foundation for building reusable UI components:

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
div(class_("container"),
    new Card("Welcome", "Hello, World!"),
    new Card("Features", "Build apps in pure Java")
)
```

### Page Components

Pages define the full HTML document structure:

```java
public class HomePage extends Page {

    @Override
    protected String title() {
        return "Home - My App";
    }

    @Override
    protected Element head() {
        return fragment(
            meta(attrs().charset("UTF-8")),
            meta(attrs().name("viewport").content("width=device-width, initial-scale=1")),
            css("/styles/main.css")
        );
    }

    @Override
    protected Element body() {
        return div(class_("page"),
            new Header(),
            main(class_("content"),
                h1("Welcome to JWeb"),
                p("Build web apps in pure Java")
            ),
            new Footer()
        );
    }
}
```

### Layouts

Layouts wrap pages with common structure:

```java
public class MainLayout implements Template {
    private final Element content;

    public MainLayout(Element content) {
        this.content = content;
    }

    @Override
    public Element render() {
        return div(class_("layout"),
            new Navbar(),
            main(class_("main-content"), content),
            new Footer()
        );
    }
}

// Register with layout
app.layout(MainLayout.class)
   .pages(
       "/", HomePage.class,
       "/about", AboutPage.class
   );
```

### Partials

Small, reusable UI pieces:

```java
public class Button implements Template {
    private final String text;
    private final String variant;
    private final Consumer<Event> onClick;

    public Button(String text, String variant, Consumer<Event> onClick) {
        this.text = text;
        this.variant = variant;
        this.onClick = onClick;
    }

    @Override
    public Element render() {
        return button(attrs()
            .class_("btn btn-" + variant)
            .onClick(onClick),
            text(text)
        );
    }
}

// Usage
new Button("Submit", "primary", e -> handleSubmit())
new Button("Cancel", "secondary", e -> handleCancel())
```

---

## Reactive State

### Creating State

```java
import com.osmig.Jweb.framework.state.State;

// Simple state
State<Integer> count = State.of(0);
State<String> name = State.of("");
State<Boolean> isVisible = State.of(true);

// Complex state
State<List<User>> users = State.of(new ArrayList<>());
State<Map<String, Object>> formData = State.of(new HashMap<>());
```

### State Updates

```java
// Direct set
count.set(5);
name.set("John");

// Update based on current value
count.update(n -> n + 1);
users.update(list -> {
    list.add(newUser);
    return list;
});

// Get current value
int currentCount = count.get();
String currentName = name.get();
```

### State Binding

State automatically updates the UI when changed:

```java
public class Counter implements Template {
    private final State<Integer> count = State.of(0);

    @Override
    public Element render() {
        return div(class_("counter"),
            p("Count: " + count.get()),
            button(attrs().onClick(e -> count.update(n -> n + 1)),
                text("Increment")),
            button(attrs().onClick(e -> count.update(n -> n - 1)),
                text("Decrement")),
            button(attrs().onClick(e -> count.set(0)),
                text("Reset"))
        );
    }
}
```

---

## Event Handling

### Event Types

```java
// Mouse events
button(attrs().onClick(e -> handleClick(e)), text("Click"))
div(attrs().onDoubleClick(e -> handleDblClick(e)), content)
div(attrs().onMouseEnter(e -> showTooltip()), content)
div(attrs().onMouseLeave(e -> hideTooltip()), content)

// Keyboard events
input(attrs().onKeyDown(e -> {
    if ("Enter".equals(e.key())) submit();
}))
input(attrs().onKeyUp(e -> search(e.value())))

// Form events
input(attrs().onChange(e -> name.set(e.value())))
input(attrs().onInput(e -> searchTerm.set(e.value())))  // Every keystroke
input(attrs().onFocus(e -> showHelp()))
input(attrs().onBlur(e -> validate()))

// Form submission
form(attrs().onSubmit(e -> {
    e.preventDefault();
    submitForm(e.formData());
}), formContent)

// Drag & drop
div(attrs()
    .onDragStart(e -> startDrag(e))
    .onDragOver(e -> allowDrop(e))
    .onDrop(e -> handleDrop(e)),
    content)

// Touch events
div(attrs()
    .onTouchStart(e -> startTouch(e))
    .onTouchMove(e -> moveTouch(e))
    .onTouchEnd(e -> endTouch(e)),
    content)

// Scroll
div(attrs().onScroll(e -> handleScroll(e)), content)

// Clipboard
div(attrs()
    .onCopy(e -> trackCopy())
    .onPaste(e -> handlePaste(e)),
    content)
```

### Event Object

The `Event` interface provides access to event data:

```java
button(attrs().onClick(e -> {
    // Event info
    String type = e.type();           // "click"
    String targetId = e.targetId();   // Element ID

    // Input value
    String value = e.value();         // For inputs
    boolean checked = e.checked();    // For checkboxes

    // Keyboard
    String key = e.key();             // "Enter", "Escape", etc.
    int keyCode = e.keyCode();
    boolean ctrlKey = e.ctrlKey();
    boolean shiftKey = e.shiftKey();

    // Mouse position
    int x = e.clientX();
    int y = e.clientY();

    // Form data
    Map<String, String> formData = e.formData();

    // Data attributes
    String userId = e.data("user-id");
    Map<String, String> allData = e.dataset();

    // Control
    e.preventDefault();
    e.stopPropagation();
}), text("Click"))
```

### Form Handling

```java
public class LoginForm implements Template {
    private final State<String> email = State.of("");
    private final State<String> password = State.of("");
    private final State<String> error = State.of("");

    @Override
    public Element render() {
        return form(attrs().onSubmit(this::handleSubmit),
            div(class_("form-group"),
                label(for_("email"), "Email"),
                input(attrs()
                    .type("email")
                    .id("email")
                    .name("email")
                    .value(email.get())
                    .onChange(e -> email.set(e.value()))
                    .required())
            ),
            div(class_("form-group"),
                label(for_("password"), "Password"),
                input(attrs()
                    .type("password")
                    .id("password")
                    .name("password")
                    .value(password.get())
                    .onChange(e -> password.set(e.value()))
                    .required())
            ),
            when(!error.get().isEmpty(),
                div(class_("error"), error.get())),
            button(type("submit"), "Login")
        );
    }

    private void handleSubmit(Event e) {
        e.preventDefault();
        // Validate and submit
        if (email.get().isEmpty()) {
            error.set("Email is required");
            return;
        }
        // Submit form...
    }
}
```

---

## Routing

### Page Routes

```java
@Component
public class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        // Simple page routes
        app.get("/", () -> new HomePage());
        app.get("/about", () -> new AboutPage());

        // With layout
        app.layout(MainLayout.class)
           .pages(
               "/", HomePage.class,
               "/about", AboutPage.class,
               "/contact", ContactPage.class
           );

        // Route with request access
        app.get("/profile", req -> {
            User user = getCurrentUser(req);
            return new ProfilePage(user);
        });
    }
}
```

### API Routes

```java
app.get("/api/users", req -> {
    List<User> users = userService.findAll();
    return Response.json(users);
});

app.post("/api/users", req -> {
    User user = req.bodyAs(User.class);
    User saved = userService.save(user);
    return Response.json(saved).status(201);
});

app.put("/api/users/:id", req -> {
    String id = req.param("id");
    User user = req.bodyAs(User.class);
    return Response.json(userService.update(id, user));
});

app.delete("/api/users/:id", req -> {
    String id = req.param("id");
    userService.delete(id);
    return Response.ok();
});
```

### Route Parameters

```java
// Path parameters
app.get("/users/:id", req -> {
    String userId = req.param("id");
    return new UserPage(userId);
});

app.get("/posts/:postId/comments/:commentId", req -> {
    String postId = req.param("postId");
    String commentId = req.param("commentId");
    return new CommentPage(postId, commentId);
});

// Query parameters
app.get("/search", req -> {
    String query = req.query("q");
    int page = Integer.parseInt(req.query("page", "1"));
    return new SearchResults(query, page);
});
```

---

## Middleware

### Built-in Middleware

```java
app.use(Middlewares.logging())                    // Request/response logging
   .use(Middlewares.cors())                       // CORS with default settings
   .use(Middlewares.cors("https://example.com"))  // CORS with specific origin
   .use(Middlewares.csrf())                       // CSRF protection
   .use(Middlewares.rateLimit(100, 60000))        // 100 requests per minute
   .use(Middlewares.securityHeaders())            // Security headers
   .use(Middlewares.timing())                     // X-Response-Time header
   .use(Middlewares.requestId())                  // X-Request-ID header
   .use(Middlewares.cacheControl(3600))           // Cache-Control header
   .use(Middlewares.noCache())                    // Disable caching
   .use(Middlewares.etag())                       // ETag support
   .use(Middlewares.compressionHeaders());        // Compression hints
```

### Custom Middleware

```java
// Simple middleware
Middleware authMiddleware = (req, chain) -> {
    String token = req.header("Authorization");
    if (token == null) {
        return ResponseEntity.status(401)
            .body(Map.of("error", "Unauthorized"));
    }
    return chain.next();
};

// Middleware with path filtering
app.use("/api/**", authMiddleware);
app.use("/admin/**", adminOnlyMiddleware);

// Conditional middleware
app.useIf(isDevelopment(), Middlewares.logging());

// Method-specific middleware
app.useForMethods(List.of("POST", "PUT", "DELETE"), csrfMiddleware);
```

---

## REST API

### REST Annotations

```java
@REST("/api/users")
public class UserApi {

    private final UserService userService;

    public UserApi(UserService userService) {
        this.userService = userService;
    }

    @GET
    public List<User> getAll() {
        return userService.findAll();
    }

    @GET("/{id}")
    public User getById(@PathVariable String id) {
        return userService.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @POST
    public ResponseEntity<User> create(@RequestBody User user) {
        User saved = userService.save(user);
        return ResponseEntity.status(201).body(saved);
    }

    @UPDATE("/{id}")  // or @PUT
    public User update(@PathVariable String id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DEL("/{id}")  // or @DELETE
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }

    @PATCH("/{id}")
    public User patch(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        return userService.patch(id, updates);
    }
}
```

### Request Handling

```java
// Access request data
String body = req.body();
User user = req.bodyAs(User.class);
String header = req.header("Content-Type");
String param = req.param("id");
String query = req.query("search");
Map<String, String> allParams = req.params();
Map<String, String> allQueries = req.queries();

// Session
req.session().set("user", currentUser);
User user = req.session().get("user", User.class);

// Cookies
String token = req.cookie("auth_token");
```

---

## Database Integration

### MongoDB

```java
import static com.osmig.Jweb.framework.db.mongo.Mongo.*;

// Connect
Mongo.connect("mongodb://localhost:27017", "mydb");
// Or use environment variables
Mongo.connect(); // Uses MONGO_URI and MONGO_DB

// Create documents
Doc user = Doc.of("users")
    .set("name", "John Doe")
    .set("email", "john@example.com")
    .set("age", 30);
Mongo.save(user);

// Read
Doc found = Mongo.findById("users", id);
String name = found.getString("name");
int age = found.getInt("age");

// Query with fluent API
List<Doc> adults = Mongo.find("users")
    .where("age").gte(18)
    .where("status").eq("active")
    .sort("name", 1)
    .limit(10)
    .toList();

// Update
Mongo.update("users")
    .where("id", id)
    .set("name", "Jane Doe")
    .set("updatedAt", System.currentTimeMillis())
    .execute();

// Delete
Mongo.delete("users")
    .where("status").eq("inactive")
    .execute();

Mongo.deleteById("users", id);

// POJO mapping
User user = Mongo.findById("users", id, User.class);
String savedId = Mongo.save("users", userPojo);
```

### Schema Definition

```java
Schema.collection("users")
    .id("id")
    .string("name").required()
    .string("email").required().unique()
    .number("age").min(0)
    .bool("active").default_(true)
    .timestamps()  // Adds createdAt and updatedAt
    .register();
```

---

## Security

### JWT Authentication

```java
import com.osmig.Jweb.framework.security.Jwt;

// Initialize
Jwt.init("your-256-bit-secret-key-minimum-32-chars");
// Or use environment variable
Jwt.init(); // Uses JWT_SECRET

// Create token
String token = Jwt.create()
    .subject("user123")
    .claim("role", "ADMIN")
    .claim("email", "user@example.com")
    .expiresIn(Duration.ofHours(24))
    .sign();

// Validate and parse
if (Jwt.isValid(token)) {
    Jwt.Token parsed = Jwt.parse(token);
    String userId = parsed.subject();
    String role = parsed.claim("role");
    List<String> roles = parsed.roles();
    boolean isAdmin = parsed.hasRole("ADMIN");
}

// Middleware protection
app.use("/api/**", Jwt.protect());           // Require valid JWT
app.use("/api/**", Jwt.optional());          // Parse JWT if present

// Access token in handler
app.get("/api/profile", req -> {
    Jwt.Token token = Jwt.getToken(req).orElseThrow();
    String userId = token.subject();
    return userService.findById(userId);
});
```

### CSRF Protection

```java
import com.osmig.Jweb.framework.security.Csrf;

// Enable CSRF middleware
app.use(Middlewares.csrf());

// Exclude certain paths
app.use(Middlewares.csrf(Set.of("/api/webhook", "/api/public")));

// Get token for forms
form(attrs().method("POST"),
    input(attrs().type("hidden").name("_csrf").value(Csrf.token())),
    // ... form fields
    button(type("submit"), "Submit")
)
```

### Password Hashing

```java
import com.osmig.Jweb.framework.security.Password;

// Hash password
String hashed = Password.hash("userPassword123");

// Verify password
boolean isValid = Password.verify("userPassword123", hashed);

// Check password strength
boolean isStrong = Password.isStrong("MyP@ssw0rd!");
```

---

## Validation

### Built-in Validators

```java
import static com.osmig.Jweb.framework.validation.Validators.*;

// String validators
required().validate(name, "name");
email().validate(email, "email");
minLength(3).validate(username, "username");
maxLength(100).validate(bio, "bio");
pattern(Pattern.compile("^[A-Z].*"), "Must start with capital").validate(name, "name");
alphanumeric().validate(code, "code");
url().validate(website, "website");
phone().validate(phoneNumber, "phone");

// Number validators
min(0).validate(age, "age");
max(150).validate(age, "age");
range(1, 100).validate(quantity, "quantity");
positive().validate(price, "price");

// Collection validators
notEmpty().validate(items, "items");
minSize(1).validate(tags, "tags");
maxSize(10).validate(attachments, "attachments");

// Boolean validators
isTrue().validate(termsAccepted, "terms");

// Object validators
requiredObject().validate(user, "user");
oneOf("ADMIN", "USER", "GUEST").validate(role, "role");
```

### Chaining Validators

```java
Validator<String> usernameValidator = required()
    .and(minLength(3))
    .and(maxLength(20))
    .and(alphanumeric());

ValidationResult result = usernameValidator.validate(username, "username");

if (!result.isValid()) {
    List<String> errors = result.errors();
    // Handle errors
}
```

### Form Validation

```java
import com.osmig.Jweb.framework.validation.FormValidator;

FormValidator validator = new FormValidator()
    .field("email", required().and(email()))
    .field("password", required().and(minLength(8)))
    .field("age", requiredNumber().and(min(18)));

ValidationResult result = validator.validate(formData);

if (!result.isValid()) {
    Map<String, List<String>> fieldErrors = result.fieldErrors();
    // Display errors next to fields
}
```

---

## Additional Features

### Internationalization (i18n)

```java
import com.osmig.Jweb.framework.i18n.I18n;
import com.osmig.Jweb.framework.i18n.Messages;

// Load messages
Messages messages = I18n.load("en");

// Get translated string
String greeting = messages.get("greeting");
String welcome = messages.get("welcome", userName);  // With parameter
```

### File Uploads

```java
import com.osmig.Jweb.framework.upload.FileUpload;

app.post("/upload", req -> {
    UploadedFile file = FileUpload.single(req, "file");

    // Validate
    if (!file.isImage()) {
        return Response.error("Only images allowed");
    }
    if (file.size() > 5_000_000) {
        return Response.error("File too large");
    }

    // Save
    Path saved = file.saveTo(Paths.get("uploads"));
    return Response.ok();
});
```

### Email

```java
import com.osmig.Jweb.framework.email.Email;
import com.osmig.Jweb.framework.email.Mailer;

Email email = Email.create()
    .to("user@example.com")
    .subject("Welcome!")
    .html(new WelcomeEmailTemplate(user).render())
    .attach("report.pdf", pdfBytes);

Mailer.send(email);
```

### Background Jobs

```java
import com.osmig.Jweb.framework.async.Jobs;
import com.osmig.Jweb.framework.async.Scheduler;

// Run async task
Jobs.run(() -> processOrder(orderId));

// Schedule recurring task
Scheduler.every(Duration.ofMinutes(5), () -> cleanupExpiredSessions());
Scheduler.daily("02:00", () -> generateDailyReport());
```

### Health Checks

```java
import com.osmig.Jweb.framework.health.Health;

Health.register("database", () -> {
    // Check database connection
    return database.isConnected()
        ? HealthStatus.healthy()
        : HealthStatus.unhealthy("Database unavailable");
});

// Endpoint: GET /health
{
  "status": "healthy",
  "checks": {
    "database": { "status": "healthy" },
    "redis": { "status": "healthy" }
  }
}
```

### Testing

```java
import com.osmig.Jweb.framework.testing.JWebTest;
import com.osmig.Jweb.framework.testing.TestClient;

class UserApiTest extends JWebTest {

    @Test
    void shouldGetUsers() {
        TestClient client = testClient();

        var response = client.get("/api/users");

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.json().asList()).isNotEmpty();
    }

    @Test
    void shouldCreateUser() {
        var response = testClient()
            .post("/api/users")
            .body(Map.of("name", "John", "email", "john@example.com"))
            .send();

        assertThat(response.status()).isEqualTo(201);
    }
}
```

### Accessibility (A11y)

```java
import com.osmig.Jweb.framework.accessibility.A11y;

// Add ARIA attributes
button(attrs()
    .aria("label", "Close dialog")
    .aria("expanded", "false")
    .role("button"),
    text("X")
)

// A11y utilities
A11y.srOnly("Screen reader only text")  // Visually hidden
A11y.skipLink("/main", "Skip to main content")
```

---

## Development Tools

JWeb includes built-in development tools for a better developer experience.

### Hot Reload

Automatic browser refresh when files change:

```yaml
jweb:
  dev:
    hot-reload: true
    watch-paths: src/main/java,src/main/resources
    debounce-ms: 10
```

Add the hot reload script to your layout:

```java
import com.osmig.Jweb.framework.dev.DevServer;

body(
    // ... your content ...
    DevServer.script()  // Only active when hot-reload is enabled
)
```

### Debug Mode

Control console logging in the browser. Set to `true` during development to see JWeb logs:

```yaml
jweb:
  dev:
    debug: false  # Set to true for development console logs
```

When enabled, you'll see logs like:
- `[JWeb] Hot reload connected`
- `[JWeb] Prefetch enabled`
- `[JWeb] Reloading...`

**Important:** Set `debug: false` for production to keep the browser console clean.

### Prefetch

Automatic link prefetching for instant navigation. When users hover over links, JWeb preloads the target page in the background:

```yaml
jweb:
  performance:
    prefetch:
      enabled: true
      cache-ttl: 300000      # Cache for 5 minutes
      hover-delay: 300       # Wait 300ms before prefetching
```

Add the prefetch script to your layout:

```java
import com.osmig.Jweb.framework.performance.Prefetch;

body(
    nav(...),
    main(...),
    footer(...),
    Prefetch.script()  // Add prefetching
)
```

Prefetch works automatically for:
- All `<a href="...">` links
- Elements with `data-prefetch-url` attribute
- Buttons and divs with prefetch URLs

Disable prefetch for specific elements:

```java
// Disable for logout link
a(attrs().href("/logout").data("no-prefetch", "true"), text("Logout"))
```

---

## Configuration

### Application YAML

Full configuration reference for `application.yaml`:

```yaml
server:
  port: 8081
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024

spring:
  application:
    name: MyApp

# JWeb configuration
jweb:
  # Admin configuration
  admin:
    token: ${JWEB_ADMIN_TOKEN}
    email: ${JWEB_ADMIN_EMAIL}

  # Email configuration (multiple providers supported)
  mail:
    enabled: true
    from: ${JWEB_MAIL_FROM}
    resend:
      api-key: ${RESEND_API_KEY}
    sendgrid:
      api-key: ${SENDGRID_API_KEY:}
    brevo:
      api-key: ${BREVO_API_KEY:}

  # API configuration
  api:
    base: /api/v1

  # Database configuration
  data:
    enabled: false  # Set to true to enable MongoDB

  # Development settings
  dev:
    hot-reload: true                              # Enable hot reload
    watch-paths: src/main/java,src/main/resources # Paths to watch
    debounce-ms: 10                               # Debounce delay (min 10)
    debug: false                                  # Browser console logs

  # Performance settings
  performance:
    minify-css: true      # Minify CSS output
    minify-html: false    # Minify HTML output
    prefetch:
      enabled: true       # Enable link prefetching
      cache-ttl: 300000   # Cache TTL in ms (5 minutes)
      hover-delay: 300    # Hover delay before prefetch (ms)
```

**Environment Variables:**

| Variable | Description |
|----------|-------------|
| `JWEB_ADMIN_TOKEN` | Admin authentication token |
| `JWEB_ADMIN_EMAIL` | Admin email address |
| `JWEB_MAIL_FROM` | Default sender email |
| `RESEND_API_KEY` | Resend.com API key |
| `SENDGRID_API_KEY` | SendGrid API key |
| `BREVO_API_KEY` | Brevo API key |
| `JWT_SECRET` | JWT signing secret (min 32 chars) |
| `MONGO_URI` | MongoDB connection URI |
| `MONGO_DB` | MongoDB database name |

---

## Project Structure

```
src/main/java/com/yourapp/
├── app/                    # Your application code
│   ├── layouts/            # Page layouts (MainLayout, AdminLayout)
│   ├── pages/              # Page components (HomePage, AboutPage)
│   ├── partials/           # Reusable components (Card, Nav, Footer)
│   ├── api/                # REST API controllers
│   ├── services/           # Business logic
│   ├── models/             # Data models
│   ├── Routes.java         # Route definitions
│   └── App.java            # Application entry point
│
└── framework/              # JWeb framework (provided)
    ├── core/               # Element, Renderable interfaces
    ├── elements/           # HTML elements (Tag, Elements)
    ├── attributes/         # Attribute builders
    ├── styles/             # CSS DSL
    ├── vdom/               # Virtual DOM (VNode, VElement)
    ├── state/              # Reactive state management
    ├── events/             # Event handling
    ├── routing/            # Router and routes
    ├── middleware/         # Middleware system
    ├── server/             # Request, Response, Controller
    ├── template/           # Template interface
    ├── api/                # REST annotations
    ├── db/                 # Database (MongoDB)
    ├── security/           # Auth, JWT, CSRF
    ├── validation/         # Validation framework
    ├── i18n/               # Internationalization
    ├── upload/             # File uploads
    ├── email/              # Email sending
    ├── async/              # Background jobs
    ├── health/             # Health checks
    ├── testing/            # Test utilities
    ├── accessibility/      # A11y helpers
    ├── error/              # Error handling
    └── util/               # JSON, logging utilities
```

---

## Running the Application

```bash
# Development (with hot reload)
mvn spring-boot:run

# Production build
mvn clean package
java -jar target/your-app.jar

# With specific profile
java -jar target/your-app.jar --spring.profiles.active=prod
```

Then open `http://localhost:8080` in your browser.

---

## Requirements

- **Java 21+** (uses modern Java features like records, pattern matching)
- **Maven 3.6+**
- **Spring Boot 3.x**

---

## License

MIT

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. email: the.jweb.team@gmail.com

---

Built with Java by developers who love Java!
