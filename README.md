# JWeb Framework

**Version 1.0.0** | **Last Updated: 2026-01-23 14:30 UTC**

A pure Java web framework that lets you build full-stack web applications entirely in Java. No HTML templates, no JSP, no Thymeleaf - just type-safe Java code with compile-time safety and full IDE support.

JWeb brings modern frontend concepts (component model, reactive state, virtual DOM) to server-side Java development, giving you the productivity of React/Vue with the power and safety of Java.

---

## Table of Contents

- [Philosophy](#philosophy)
- [Features](#features)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
  - [Core Rendering System](#core-rendering-system)
  - [Virtual DOM](#virtual-dom)
  - [Framework Structure](#framework-structure)
- [HTML DSL](#html-dsl)
  - [Elements](#elements)
  - [Attributes](#attributes)
  - [Modern HTML5 Elements](#modern-html5-elements)
  - [Form Input Builders](#form-input-builders)
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
  - [Feature Queries (@supports)](#feature-queries-supports)
  - [Nested CSS](#nested-css)
  - [CSS Variables](#css-variables)
  - [CSS Animations](#css-animations)
- [JavaScript DSL](#javascript-dsl)
  - [Core JS Module](#core-js-module)
  - [Actions](#actions)
  - [Form Handlers](#form-handlers)
  - [Click Handlers](#click-handlers)
  - [Async/Await](#asyncawait)
  - [Fetch Builder](#fetch-builder)
  - [DOM Query Builder](#dom-query-builder)
  - [Advanced JS Modules](#advanced-js-modules)
- [Components and Templates](#components-and-templates)
  - [Template Interface](#template-interface)
  - [Lifecycle Hooks](#lifecycle-hooks)
  - [Page Components](#page-components)
  - [Layouts](#layouts)
- [Reactive State](#reactive-state)
- [Event Handling](#event-handling)
- [Routing](#routing)
  - [Page Routes](#page-routes)
  - [API Routes](#api-routes)
  - [Route Parameters](#route-parameters)
- [Middleware](#middleware)
- [REST API](#rest-api)
- [Database Integration](#database-integration)
- [Security](#security)
- [Validation](#validation)
- [AI Integration](#ai-integration)
- [Additional Features](#additional-features)
- [Development Tools](#development-tools)
- [Configuration](#configuration)
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
7. **Modular Architecture** - Clean separation with focused files. Single entry points (`El.*` for HTML, `CSS.*` for CSS, `JS.*` for JavaScript) hide complexity while keeping internals maintainable.

---

## Features

### Core DSL Features (85 modules)
- **Type-safe HTML** - Build HTML with Java methods, no string templates (18 modules)
- **Type-safe CSS** - CSS properties as methods with unit validation (29 modules)
- **Type-safe JavaScript** - Generate JS code from Java with full IDE support (38 modules)
- **Modular Elements DSL** - Single `El.*` entry point with category-based organization
- **Modular Styles DSL** - Mixin interfaces for clean separation (BoxModel, Flex, Grid, Typography, Effects, Position)
- **Lambda-based Validation** - Immutable, functional form validation with `FieldValidator`
- **DSL Shortcuts** - Simplified syntax for common patterns (shadow presets, flex layouts, ternary builders)

### Architecture
- **Component-based** - Create reusable components with the `Template` interface
- **Lifecycle Hooks** - `beforeRender`, `onMount`, `onUnmount` for component lifecycle
- **Reactive State** - React-like state management with `State<T>`
- **Virtual DOM** - Efficient rendering with VNode diffing
- **Routing System** - Fluent route definitions with typed path parameters
- **Middleware System** - Request processing pipeline with built-in middleware
- **REST API** - Annotation-based REST controllers

### CSS Features (29 Modules)
- **CSS Feature Queries** - `@supports` for progressive enhancement
- **CSS Nesting** - Native CSS nesting syntax support
- **CSS Variables** - Custom properties with design system builders
- **CSS Animations** - 40+ pre-built animations with scroll-driven animation support
- **Media Queries** - Responsive breakpoints with fluent API
- **Keyframes** - Animation definitions
- **Container Queries** - `@container` support
- **CSS Layers** - `@layer` support
- **CSS Scope** - `@scope` for DOM-scoped styles
- **CSS Property** - `@property` for registered custom properties

### JavaScript Features (38 Modules)
- **Form Input Builders** - Convenient shortcuts using `attrs()` pattern
- **Async/Await DSL** - Type-safe async JavaScript generation
- **Fetch Builder** - Fluent HTTP request builder with status handling
- **DOM Query Builder** - jQuery-like DOM manipulation
- **Promise Utilities** - Retry logic, cancellable promises, combinators
- **Web Workers** - Dedicated workers, SharedWorker, BroadcastChannel
- **Service Workers** - Registration, Cache API, Push API
- **Canvas API** - Complete 2D drawing API
- **WebRTC** - Peer connections, media streams, data channels
- **Web Crypto** - Encryption, hashing, key management
- **Performance API** - Timing, observers, metrics

### Backend Features
- **MongoDB Integration** - Fluent DSL for MongoDB operations
- **JWT Authentication** - Token-based auth with middleware
- **Validation Framework** - Fluent API for input validation
- **CSRF Protection** - Built-in cross-site request forgery prevention
- **Internationalization** - i18n support with locale detection
- **File Uploads** - Easy multipart file handling with validation
- **Email** - Fluent email builder with templates
- **Background Jobs** - Async task execution with scheduling
- **AI Integration** - Spring AI with fluent DSL for OpenAI and local LLMs (Ollama in Docker)

### Developer Experience
- **Testing Utilities** - Mock requests and assertions for testing
- **Accessibility** - WCAG 2.1 compliance helpers
- **Hot Reload** - Development mode with automatic reloading
- **No build tools** - Just Maven and Java, no webpack/npm required
- **Clean Imports** - Single `El.*` import for all HTML elements

---

## Quick Start

```java
import static com.osmig.Jweb.framework.elements.El.*;
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
    |-- Template (interface) - render() returns Element
            |-- Page - Full page with head/body
            |-- Layout - Wraps pages with common structure
            |-- (your components)
```

### Virtual DOM

JWeb uses a Virtual DOM (VNode) for efficient rendering:

| VNode Type | Purpose |
|------------|---------|
| `VElement` | Represents an HTML element with tag, attributes, children |
| `VText` | Represents escaped text content |
| `VRaw` | Represents raw/unescaped HTML |
| `VFragment` | Groups multiple nodes without a wrapper element |

### Framework Structure (150+ modules across 35+ packages)

```
framework/
├── accessibility/      # A11y helpers (WCAG 2.1 compliance)
├── api/                # REST annotations (@REST, @GET, @POST, @DEL, @PATCH, @UPDATE)
├── async/              # Background jobs (Jobs, Scheduler, BackgroundTask)
├── attributes/         # HTML attribute builders (Attr, Attributes)
├── cache/              # Caching utilities
├── cli/                # CLI tools (JWebCli, Templates)
├── config/             # Configuration (JWebConfiguration)
├── context/            # Request context management
├── core/               # Core interfaces (Element, Renderable, Component, Page, RawContent)
├── db/mongo/           # MongoDB integration (Mongo, Doc, Schema, MongoQuery)
├── dev/                # Development tools (HotReload, DevController)
├── docs/               # Framework documentation
├── elements/           # HTML elements DSL (18 modules)
├── email/              # Email sending (Email, Mailer, EmailTemplate)
├── error/              # Error handling (ErrorHandler, JWebException)
├── events/             # Event handling (Event, DomEvent, EventHandler)
├── forms/              # Form utilities (Form, FormModel)
├── health/             # Health checks (Health, HealthCheck, HealthStatus)
├── http/               # HTTP utilities
├── hydration/          # Client-side hydration
├── i18n/               # Internationalization (I18n, Messages)
├── js/                 # JavaScript DSL (38 modules)
├── layout/             # Layout management
├── metrics/            # Application metrics
├── middleware/         # Middleware system (4 modules)
├── navigation/         # Navigation utilities
├── openapi/            # OpenAPI documentation
├── performance/        # Performance utilities
├── portal/             # Portal rendering
├── ref/                # Reference utilities
├── routing/            # Router and routes (Router, PageRegistry)
├── security/           # Security (Jwt, Password, Cors, Csrf, Auth, OAuth2, RateLimit)
├── server/             # Request/Response handling
├── sse/                # Server-Sent Events
├── state/              # Reactive state (State<T>, StateBinding)
├── styles/             # CSS DSL (29 modules)
├── template/           # Template interface
├── testing/            # Test utilities (JWebTest, MockRequest, TestClient)
├── transition/         # View transitions
├── ui/                 # UI components (UI, Toast)
├── upload/             # File uploads (FileUpload, UploadedFile)
├── util/               # Utilities (Json, Log)
├── validation/         # Validation framework
├── vdom/               # Virtual DOM (VNode, VElement, VText, VRaw, VFragment)
└── websocket/          # WebSocket support
```

---

## HTML DSL

### Imports

```java
// The ONE import you need for HTML elements
import static com.osmig.Jweb.framework.elements.El.*;

// Additional imports for styling
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
```

### Elements

The `El` class is the single entry point that re-exports all element factory methods (18 modules):

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

**Available Elements:**

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
| **Helpers** | `text`, `raw`, `fragment`, `each`, `tag` |

### Attributes

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

### Modern HTML5 Elements

JWeb includes modern HTML5 interactive elements:

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
timeWithDatetime("2026-01-23", "January 23, 2026")

// Data element for machine-readable values
data("SKU-123", "Product Widget")
```

### Conditional Rendering

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

### Collection Iteration

```java
// Map a collection to elements
ul(each(users, user ->
    li(class_("user-item"),
        strong(user.getName()),
        span(" - " + user.getEmail())
    )
))
```

### Fragments

```java
// Group multiple elements without a wrapper
fragment(
    h1("Title"),
    p("First paragraph"),
    p("Second paragraph")
)
```

### Error Boundaries

```java
errorBoundary(
    () -> riskyComponent.render(),
    error -> p("Error: " + error.getMessage())
)
```

---

## CSS DSL

### Inline Styles

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

// Style shortcuts - no verbose parameters needed!
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

**Shadow Presets:**

| Method | Description |
|--------|-------------|
| `shadowXs()` | Extra small shadow |
| `shadowSm()` | Small shadow |
| `shadow()` | Default shadow |
| `shadowMd()` | Medium shadow |
| `shadowLg()` | Large shadow |
| `shadowXl()` | Extra large shadow |
| `shadowInner()` | Inner (inset) shadow |
| `shadowNone()` | Remove shadow |

**Border Radius Presets:**

| Method | Value |
|--------|-------|
| `roundedNone()` | 0 |
| `roundedXs()` | 2px |
| `roundedSm()` | 4px |
| `roundedMd()` | 6px |
| `roundedLg()` | 8px |
| `roundedXl()` | 12px |
| `rounded2xl()` | 16px |
| `rounded3xl()` | 24px |
| `roundedFull()` | 9999px (pill/circle) |

**Style Mixin Interfaces:**

| Mixin Interface | Properties |
|-----------------|------------|
| `StyleBoxModel` | `margin`, `padding`, `border`, `width`, `height`, `boxSizing` |
| `StyleFlex` | `display(flex)`, `flexDirection`, `justifyContent`, `alignItems`, `gap` |
| `StyleGrid` | `gridTemplateColumns`, `gridTemplateRows`, `gridArea`, `gridGap` |
| `StyleTypography` | `color`, `fontSize`, `fontWeight`, `lineHeight`, `textAlign` |
| `StyleEffects` | `transform`, `transition`, `animation`, `boxShadow`, `filter` |
| `StylePosition` | `position`, `top`, `right`, `bottom`, `left`, `inset`, `zIndex` |

### CSS Rules

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

### CSS Units

```java
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

// Absolute units
px(16)              // 16px
pt(12)              // 12pt

// Relative units
rem(1.5)            // 1.5rem
em(1.2)             // 1.2em
percent(50)         // 50%
ch(60)              // 60ch

// Viewport units
vh(100)             // 100vh
vw(50)              // 50vw
dvh(100)            // 100dvh (dynamic)

// Grid units
fr(1)               // 1fr

// Time units
ms(300)             // 300ms
s(0.5)              // 0.5s

// Special values
auto, zero, none, inherit

// CSS functions
calc("100% - 20px")
min(px(300), percent(100))
max(px(200), percent(50))
clamp(rem(1), vw(4), rem(2))
```

### CSS Colors

```java
import static com.osmig.Jweb.framework.styles.CSSColors.*;

// Named colors
white, black, transparent, currentColor

// Color palette with shades (50-900)
red(500)            // Standard red
blue(300)           // Light blue
gray(100)           // Very light gray

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

### Media Queries

```java
import static com.osmig.Jweb.framework.styles.MediaQuery.*;

// Breakpoints
media().minWidth(px(768)).rule(".container", style().maxWidth(px(720)))

// Predefined breakpoints
md().rule(".sidebar", style().display(block))
lg().rule(".container", style().maxWidth(px(960)))
mobile().rule(".nav", style().flexDirection(column))

// Dark mode
media().prefersDark()
    .rule("body", style().backgroundColor(hex("#1a1a1a")).color(white))

// Reduced motion (accessibility)
media().prefersReducedMotion()
    .rule("*", style().animationDuration(ms(0)).transitionDuration(ms(0)))
```

### Feature Queries (@supports)

```java
import static com.osmig.Jweb.framework.styles.Supports.*;

// Check for feature support
supports("display", "grid")
    .rule(".container", style().display(grid))
    .build();

// Convenience methods
supportsGrid()
supportsFlexbox()
supportsCustomProperties()
supportsBackdropFilter()
supportsContainerQueries()
```

### CSS Variables

```java
import static com.osmig.Jweb.framework.styles.CSSVariables.*;

// Define and use variables
var_("primary-color")           // var(--primary-color)
var_("spacing", "1rem")         // var(--spacing, 1rem)

// Design system builder
designSystem()
    .spacing("xs", "0.25rem")
    .spacing("sm", "0.5rem")
    .spacing("md", "1rem")
    .color("primary", "#3b82f6")
    .color("secondary", "#10b981")
    .fontFamily("sans", "system-ui, sans-serif")
    .build();

// Theme builder
themeBuilder()
    .light("bg", "white")
    .light("text", "black")
    .dark("bg", "black")
    .dark("text", "white")
    .build();
```

### CSS Animations

```java
import static com.osmig.Jweb.framework.styles.CSSAnimations.*;

// Pre-built animations (40+)
style().animation(fadeIn(s(1)))
style().animation(slideInLeft(s(0.6)))
style().animation(pulse(s(1.5)).iterationCount(iterationInfinite))
style().animation(bounce(s(1)))
style().animation(rotate360(s(2)).timing(timingLinear))

// Animation builder
style().animation(
    fadeIn(s(1))
        .timing(timingEaseOut)
        .delay(ms(200))
        .fillMode(fillModeForwards)
)

// Scroll-driven animations
style().animation(fadeIn(s(1)))
       .animationTimeline(scrollTimeline())
       .animationRange("entry", "exit")

// Timing functions
timingEase, timingLinear, timingEaseIn, timingEaseOut, timingEaseInOut
timingCubicBezier(0.4, 0, 0.2, 1)
timingSteps(5, "end")
```

---

## JavaScript DSL

### Core JS Module

```java
import static com.osmig.Jweb.framework.js.JS.*;

// Variables and values
variable("count")           // count
str("hello")               // 'hello'
obj("name", "John", "age", 30)  // {name:'John',age:30}
array(1, 2, 3)             // [1,2,3]

// Functions
Func formatTime = func("formatTime", "seconds")
    .var_("hrs", floor(variable("seconds").div(3600)))
    .ret(variable("hrs").padStart(2, "0"));

// DOM access
getElem("submit")          // document.getElementById('submit')
$("submit")                // shorthand for getElem()
query(".card")             // document.querySelector('.card')
queryAll(".item")          // document.querySelectorAll('.item')

// Property access shortcuts
variable("response").call("json")         // response.json() - simpler than .dot("json").invoke()
variable("e").path("target.result")       // e.target.result - simpler than .dot("target").dot("result")
variable("res").json()                    // res.json() - common method shortcut
variable("res").text()                    // res.text()
variable("res").blob()                    // res.blob()

// Script builder
String js = script()
    .var_("count", 0)
    .var_("running", false)
    .add(formatTime)
    .build();
```

### Actions

High-level UI interactions (no JS knowledge needed):

```java
import static com.osmig.Jweb.framework.js.Actions.*;

// Built-in actions
showMessage("status").success("Saved!")
showMessage("status").error("Failed!")
resetForm("contact-form")
navigateTo("/dashboard")
reload()
show("modal")
hide("modal")
toggle("panel")
setText("title", "New Title")
addClass("card", "active")
removeClass("card", "active")
```

### Form Handlers

```java
// Form submission to API
onSubmit("login-form")
    .loading("Logging in...")
    .post("/api/login")
    .withFormData()
    .ok(all(
        showMessage("status").success("Login successful!"),
        navigateTo("/dashboard")))
    .fail(showMessage("status").error("Invalid credentials"))

// External service (e.g., EmailJS)
onSubmitExternal("contact-form")
    .loading("Sending...")
    .service("emailjs")
    .call("send", "'service_id'", "'template_id'", "{...}")
    .ok(showMessage("status").success("Sent!"))
    .fail(showMessage("status").error("Failed"))
```

### Click Handlers

```java
onClick("delete-btn")
    .confirm("Are you sure?")
    .post("/api/delete")
    .ok(reload())
    .fail(showMessage("error").error("Delete failed"))

onClick("toggle-btn").toggle("panel")
onClick("show-btn").show("modal")
onClick("close-btn").hide("modal")
```

### Async/Await

```java
// Async function
asyncFunc("loadData")
    .does(
        await_(get("/api/users").ok(assignVar("users", "_data"))),
        await_(get("/api/posts").ok(assignVar("posts", "_data"))),
        call("renderDashboard")
    )

// Promise.all for parallel requests
promiseAll(
    get("/api/users"),
    get("/api/posts"),
    get("/api/comments")
).ok(all(
    assignVar("users", "_data[0]"),
    assignVar("posts", "_data[1]"),
    assignVar("comments", "_data[2]")
))
```

### Fetch Builder

```java
// GET request
get("/api/users")
    .ok(assignVar("users", "_data"))
    .fail(showMessage("error").error("Failed to load"))

// POST with JSON
post("/api/users")
    .json("{\"name\":\"John\"}")
    .ok(showMessage("status").success("Created!"))

// POST with form data
post("/api/contact")
    .formData("contact-form")
    .ok(resetForm("contact-form"))

// Handle specific status codes
get("/api/data")
    .onStatus(401, navigateTo("/login"))
    .onStatus(404, showMessage("error").error("Not found"))
    .ok(processData())

// Status code shortcuts
get("/api/data")
    .onUnauthorized(navigateTo("/login"))   // shorthand for onStatus(401, ...)
    .onForbidden(showMessage("error"))      // shorthand for onStatus(403, ...)
    .onNotFound(showMessage("error"))       // shorthand for onStatus(404, ...)
    .onServerError(showMessage("error"))    // shorthand for onStatus(500, ...)
    .onBadRequest(showMessage("error"))     // shorthand for onStatus(400, ...)
    .ok(processData())

// Type-safe ternary URL builder (no raw JS strings!)
fetch("").urlFromVar(
    whenVar("currentTab").equals("pending")
        .thenUrl("/api/pending")
        .elseUrl("/api/all")
)
```

### DOM Query Builder

```java
// Select and manipulate
query("#myDiv").hide()
query("#panel").addClass("visible")
query("#title").setText("Hello World")

// Chain operations
query("#card")
    .addClass("selected")
    .removeClass("dimmed")
    .setText("Selected!")

// Query multiple elements
queryAll(".item").addClass("processed")
```

### Advanced JS Modules

JWeb includes 38 JavaScript DSL modules for comprehensive browser API coverage:

| Module | Purpose |
|--------|---------|
| `JSPromise` | Promise utilities - retry, timeout, cancellable, combinators |
| `JSIterator` | Iterators and generators - `function*`, `yield`, async iterators |
| `JSProxy` | Proxy and Reflect API - all 13 handler traps |
| `JSWorker` | Web Workers - dedicated, shared, message channels |
| `JSServiceWorker` | Service Workers - caching, push notifications, sync |
| `JSMedia` | Media APIs - audio/video control, MediaRecorder, Picture-in-Picture |
| `JSCanvas` | Canvas 2D API - drawing, gradients, transforms |
| `JSWebRTC` | WebRTC - peer connections, media streams, data channels |
| `JSCrypto` | Web Crypto - encryption, hashing, key management |
| `JSPerformance` | Performance API - timing, observers, metrics |
| `JSStorage` | localStorage/sessionStorage |
| `JSWebSocket` | WebSocket with auto-reconnect |
| `JSAnimation` | requestAnimationFrame, transitions |
| `JSWebAnimations` | Web Animations API |
| `JSGeolocation` | Geolocation API |
| `JSNotification` | Push notifications |
| `JSClipboard` | Clipboard API |
| `JSFullscreen` | Fullscreen API |
| `JSShare` | Web Share API |
| `JSVisibility` | Page Visibility API |
| `JSObservers` | Intersection/Mutation/Resize observers |
| `JSIntl` | Internationalization |

**Example - Promise with retry:**

```java
import static com.osmig.Jweb.framework.js.JSPromise.*;

retry(variable("apiCall"), 3)
    .delay(1000)
    .exponentialBackoff()
    .onRetry(callback("attempt").log("Retry:", variable("attempt")))
    .build();
```

**Example - WebSocket:**

```java
import static com.osmig.Jweb.framework.js.JSWebSocket.*;

webSocket("/ws/chat")
    .onMessage(callback("e").log(variable("e").dot("data")))
    .onError(callback("e").log("Error:", variable("e")))
    .autoReconnect(3000)
    .build("ws");
```

---

## Components and Templates

### Template Interface

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

### Lifecycle Hooks

```java
public class UserProfilePage implements Template {
    private User user;
    private List<Post> posts;

    @Override
    public void beforeRender(Request request) {
        int userId = request.requireParamInt("id");
        this.user = userService.findById(userId);
        this.posts = userService.getPostsForUser(userId);
    }

    @Override
    public String onMount() {
        return "initProfileCharts(); setupInfiniteScroll();";
    }

    @Override
    public String onUnmount() {
        return "clearInterval(refreshTimer);";
    }

    @Override
    public Optional<String> pageTitle() {
        return Optional.of(user.getName() + " - Profile");
    }

    @Override
    public Element render() {
        return div(class_("profile"),
            h1(user.getName()),
            div(class_("posts"), each(posts, PostCard::new))
        );
    }
}
```

### Layouts

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

---

## Reactive State

```java
import com.osmig.Jweb.framework.state.State;

// Create state
State<Integer> count = State.of(0);
State<String> name = State.of("");

// Update state
count.set(5);
count.update(n -> n + 1);

// Get current value
int currentCount = count.get();
```

---

## Event Handling

```java
// Mouse events
button(attrs().onClick(e -> handleClick(e)), text("Click"))
div(attrs().onMouseEnter(e -> showTooltip()), content)

// Keyboard events
input(attrs().onKeyDown(e -> {
    if ("Enter".equals(e.key())) submit();
}))

// Form events
input(attrs().onChange(e -> name.set(e.value())))
form(attrs().onSubmit(e -> {
    e.preventDefault();
    submitForm(e.formData());
}), formContent)
```

---

## Routing

### Page Routes

```java
app.get("/", () -> new HomePage());
app.get("/about", () -> new AboutPage());
app.get("/profile/:id", req -> {
    User user = getCurrentUser(req);
    return new ProfilePage(user);
});
```

### API Routes

```java
app.get("/api/users", req -> Response.json(userService.findAll()));
app.post("/api/users", req -> {
    User user = req.bodyAs(User.class);
    return Response.json(userService.save(user)).status(201);
});
app.put("/api/users/:id", req -> {
    String id = req.param("id");
    User user = req.bodyAs(User.class);
    return Response.json(userService.update(id, user));
});
app.delete("/api/users/:id", req -> {
    userService.delete(req.param("id"));
    return Response.ok();
});
```

### Route Parameters

```java
// Typed parameters
int id = req.paramInt("id");
long bigId = req.paramLong("id");
UUID uuid = req.paramUUID("id");

// With defaults
int page = req.paramInt("page", 1);

// Required (throws if missing)
String username = req.requireParam("username");
int userId = req.requireParamInt("id");
```

---

## Middleware

```java
app.use(Middlewares.logging())
   .use(Middlewares.cors())
   .use(Middlewares.csrf())
   .use(Middlewares.rateLimit(100, 60000))
   .use(Middlewares.securityHeaders());

// Path-specific middleware
app.use("/api/**", authMiddleware);
app.use("/admin/**", adminOnlyMiddleware);
```

---

## REST API

```java
@REST("/api/users")
public class UserApi {

    @GET
    public List<User> getAll() {
        return userService.findAll();
    }

    @GET("/{id}")
    public User getById(@PathVariable String id) {
        return userService.findById(id);
    }

    @POST
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.status(201).body(userService.save(user));
    }

    @UPDATE("/{id}")
    public User update(@PathVariable String id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DEL("/{id}")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
```

---

## Database Integration

### MongoDB

```java
import static com.osmig.Jweb.framework.db.mongo.Mongo.*;

Mongo.connect("mongodb://localhost:27017", "mydb");

// Create
Doc user = Doc.of("users")
    .set("name", "John Doe")
    .set("email", "john@example.com");
Mongo.save(user);

// Query
List<Doc> users = Mongo.find("users")
    .where("age").gte(18)
    .where("status").eq("active")
    .sort("name", 1)
    .limit(10)
    .toList();

// Update
Mongo.update("users")
    .where("id", id)
    .set("name", "Jane Doe")
    .execute();

// Delete
Mongo.delete("users")
    .where("status").eq("inactive")
    .execute();
```

---

## Security

### JWT Authentication

```java
import com.osmig.Jweb.framework.security.Jwt;

Jwt.init("your-256-bit-secret-key-minimum-32-chars");

// Create token
String token = Jwt.create()
    .subject("user123")
    .claim("role", "ADMIN")
    .expiresIn(Duration.ofHours(24))
    .sign();

// Validate
if (Jwt.isValid(token)) {
    Jwt.Token parsed = Jwt.parse(token);
    String userId = parsed.subject();
    String role = parsed.claim("role");
}

// Middleware protection
app.use("/api/**", Jwt.protect());
```

### Password Hashing

```java
import com.osmig.Jweb.framework.security.Password;

String hashed = Password.hash("userPassword123");
boolean isValid = Password.verify("userPassword123", hashed);
```

---

## Validation

```java
import static com.osmig.Jweb.framework.validation.Validators.*;

// Chain validators
Validator<String> emailValidator = required()
    .and(email())
    .and(maxLength(100));

ValidationResult result = emailValidator.validate(value, "email");
if (!result.isValid()) {
    List<String> errors = result.errors();
}

// Form validation (lambda-based)
ValidationResult result = FormValidator.create()
    .field("email", email, f -> f.required().email())
    .field("password", password, f -> f.required().minLength(8))
    .field("age", age, f -> f.required().min(18).max(120))
    .validate();
```

---

## AI Integration

JWeb includes a fluent DSL for AI/LLM interactions via Spring AI OpenAI.

### Configuration

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.7
```

### Basic Chat

```java
import static com.osmig.Jweb.framework.ai.AI.*;

// Simple question
String answer = AI.ask("What is the capital of France?");

// With options
String response = AI.chat("Explain quantum computing")
    .model("gpt-4o")
    .temperature(0.7)
    .maxTokens(500)
    .send();

// With system prompt
String response = AI.chat()
    .system("You are a helpful coding assistant")
    .user("How do I sort a list in Java?")
    .send();

// Presets
AI.chat("Write a creative story").creative().send();  // High temperature
AI.chat("Analyze this data").precise().send();        // Low temperature
AI.chat("Help me with this").balanced().send();       // Moderate settings
```

### Streaming Responses

```java
// Stream chunks as they arrive
AI.stream("Write a long story about Java")
    .onChunk(chunk -> System.out.print(chunk))
    .onComplete(fullText -> saveToFile(fullText))
    .onError(error -> log.error("AI error", error))
    .send();

// Stream and wait for completion
String result = AI.stream("Generate a report")
    .onChunk(chunk -> updateUI(chunk))
    .sendAndWait();
```

### Multi-turn Conversations

```java
// Maintain context across exchanges
Conversation conv = AI.conversation()
    .system("You are a friendly assistant")
    .model("gpt-4o");

String r1 = conv.say("My name is John");
String r2 = conv.say("What is my name?");  // AI remembers: "Your name is John"

// Get history
List<Conversation.Turn> history = conv.getHistory();

// Fork conversation for branching
Conversation branch = conv.fork();
branch.say("Actually, call me Johnny");
```

### Task-Specific Helpers

```java
// Summarization
String summary = AI.summarize(longArticle)
    .maxLength(100)
    .bullets()
    .send();

// Translation
String spanish = AI.translate("Hello, world!")
    .to("Spanish")
    .formal()
    .send();

// Code generation/analysis
String code = AI.code("Sort a list of integers")
    .language("Java")
    .send();

String explanation = AI.code(existingCode)
    .explain()
    .send();

String review = AI.code(myCode)
    .review()
    .send();

// Data extraction
String json = AI.extract(emailText)
    .fields("name", "email", "phone", "company")
    .asJson()
    .send();
```

### Check Availability

```java
if (AI.isAvailable()) {
    // AI is configured and ready
    String response = AI.ask("Hello!");
} else {
    // Handle missing API key
    log.warn("AI not configured");
}
```

### Ollama - Local or Remote LLMs

Run LLMs locally via Docker or connect to remote Ollama servers.

#### Option 1: Connect to Remote Ollama (Simplest)

No Docker needed - just connect to an existing Ollama server:

```java
// Connect to Ollama running on a server
Ollama.connect("http://192.168.1.100:11434");
Ollama.connect("http://gpu-server.mycompany.com:11434", "llama3.2");

// Check if server is reachable
if (Ollama.pingOllama("http://server:11434")) {
    Ollama.connect("http://server:11434");
}

// Now use it normally
String response = AI.chat("Hello!").send();

// Disconnect when done
Ollama.disconnect();
```

#### Option 2: Remote Docker Host

Run the container on a remote server with Docker:

```java
// Configure remote Docker (call BEFORE start)
Ollama.useDockerHost("tcp://192.168.1.100:2375");

// Or use SSH
Ollama.useDockerSSH("user@gpu-server.mycompany.com");

// Enable GPU support (requires NVIDIA runtime on host)
Ollama.enableGpu();

// Now start - container runs on remote host
Ollama.start("llama3.2");
```

#### Option 3: Local Docker

```java
// Start Ollama locally (downloads model if needed)
Ollama.start("llama3.2");           // Default: Llama 3.2 (3B)
Ollama.start("llama3.2:1b");        // Smaller, faster
Ollama.start("mistral");            // Mistral 7B
Ollama.start("codellama");          // Code-focused

// With progress callback
Ollama.start("llama3.2", progress -> System.out.println(progress));

// Async start (non-blocking)
Ollama.startAsync("mistral")
    .thenAccept(url -> System.out.println("Ready at " + url));
```

#### Using Ollama

```java
// AI.chat() automatically uses Ollama when connected
String response = AI.chat("Hello!").send();

// Or use Ollama directly
String response = Ollama.chat("What is 2+2?").send();
String answer = Ollama.ask("Quick question");

// With options
Ollama.chat("Write code")
    .system("You are a coding assistant")
    .temperature(0.3)
    .maxTokens(500)
    .send();

// Switch models
Ollama.useModel("codellama");

// Switch between providers
AI.useOpenAI();   // Use OpenAI API
AI.useOllama();   // Use Ollama

// Check status
AI.Provider provider = AI.getProvider();  // OPENAI or OLLAMA
boolean running = Ollama.isRunning();
boolean remote = Ollama.isRemote();
String endpoint = Ollama.getEndpoint();

// Stop/disconnect
Ollama.stop();
```

**Available Models:**

| Model | Description | Size |
|-------|-------------|------|
| `llama3.2` | Meta Llama 3.2 (default) | 3B |
| `llama3.2:1b` | Llama 3.2 smaller | 1B |
| `llama3.1` | Meta Llama 3.1 | 8B |
| `mistral` | Mistral 7B | 7B |
| `codellama` | Code Llama | 7B |
| `phi3` | Microsoft Phi-3 | 3.8B |
| `gemma2` | Google Gemma 2 | 9B |
| `qwen2.5` | Alibaba Qwen 2.5 | 7B |

See [Ollama Model Library](https://ollama.ai/library) for all available models.

---

## Additional Features

- **Internationalization (i18n)** - `I18n.load("en")`, `messages.get("greeting")`
- **File Uploads** - `FileUpload.single(req, "file")`, validation, save to disk
- **Email** - `Email.create().to("...").subject("...").html(content)`
- **Background Jobs** - `Jobs.run(() -> processOrder(id))`
- **Health Checks** - `Health.register("database", () -> HealthStatus.healthy())`
- **Testing** - `JWebTest`, `TestClient`, `MockRequest`
- **Accessibility** - `A11y.srOnly("text")`, ARIA helpers
- **Server-Sent Events** - SSE support for real-time updates
- **WebSockets** - WebSocket support with auto-reconnect

---

## Development Tools

### Hot Reload

```yaml
jweb:
  dev:
    hot-reload: true
    watch-paths: src/main/java,src/main/resources
    debounce-ms: 10
    debug: false  # Set true for browser console logs
```

### Prefetch

```yaml
jweb:
  performance:
    prefetch:
      enabled: true
      cache-ttl: 300000
      hover-delay: 300
```

---

## Configuration

### application.yaml

```yaml
server:
  port: ${PORT:8085}

spring:
  application:
    name: MyApp

jweb:
  admin:
    token: ${JWEB_ADMIN_TOKEN:}
  api:
    base: /api/v1
  data:
    enabled: false
    mongo:
      uri: ${MONGO_URI:mongodb://localhost:27017}
      database: ${MONGO_DB:myapp}
  dev:
    hot-reload: true
    debug: false
  performance:
    minify-css: true
    prefetch:
      enabled: true
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | 8085 |
| `JWEB_ADMIN_TOKEN` | Admin authentication token | - |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | - |
| `MONGO_URI` | MongoDB connection URI | mongodb://localhost:27017 |
| `MONGO_DB` | MongoDB database name | myapp |
| `OPENAI_API_KEY` | OpenAI API key for AI features | - |

---

## Project Structure

```
src/main/java/com/yourapp/
|-- app/                    # Your application code
|   |-- layouts/            # Page layouts
|   |-- pages/              # Page components
|   |-- partials/           # Reusable components
|   |-- api/                # REST API controllers
|   |-- services/           # Business logic
|   |-- models/             # Data models
|   |-- Routes.java         # Route definitions
|   |-- App.java            # Application entry point
|
|-- framework/              # JWeb framework (150+ modules)
    |-- ai/                 # AI/LLM integration (Spring AI)
    |-- core/               # Element, Renderable interfaces
    |-- elements/           # HTML elements DSL (18 modules)
    |-- styles/             # CSS DSL (29 modules)
    |-- js/                 # JavaScript DSL (38 modules)
    |-- validation/         # Validation framework
    |-- vdom/               # Virtual DOM
    |-- template/           # Template interface
    |-- state/              # Reactive state
    |-- routing/            # Router and routes
    |-- middleware/         # Middleware system
    |-- server/             # Request, Response
    |-- api/                # REST annotations
    |-- db/mongo/           # MongoDB integration
    |-- security/           # Auth, JWT, CSRF
    |-- i18n/               # Internationalization
    |-- upload/             # File uploads
    |-- email/              # Email sending
    |-- async/              # Background jobs
    |-- health/             # Health checks
    |-- testing/            # Test utilities
    |-- accessibility/      # A11y helpers
    |-- dev/                # Development tools
    |-- util/               # JSON, logging
```

---

## Running the Application

```bash
# Development (with hot reload)
./mvnw spring-boot:run

# Production build
./mvnw clean package
java -jar target/your-app.jar

# With specific profile
java -jar target/your-app.jar --spring.profiles.active=prod
```

Then open `http://localhost:8085` in your browser.

---

## Requirements

- **Java 21+** (uses modern Java features like records, pattern matching)
- **Maven 3.6+**
- **Spring Boot 4.x**

---

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.0.0 | Framework base |
| Spring Boot Web | - | HTTP handling |
| Spring Boot WebSocket | - | WebSocket support |
| Spring Boot DevTools | - | Hot reload |
| MongoDB Driver | 5.2.0 | Database |
| Spring Security Crypto | - | BCrypt password hashing |
| JJWT | 0.12.6 | JWT support |
| Jackson Databind | - | JSON processing |

### Optional Dependencies (commented in pom.xml)
- Spring AI OpenAI 2.0.0-M1 - AI/LLM integration
- Spring AI Ollama 2.0.0-M1 - Local LLM support
- Testcontainers Ollama 1.20.4 - Docker-based Ollama

---

## License

MIT

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

Email: the.jweb.team@gmail.com

---

Built with Java by developers who love Java!
