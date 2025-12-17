# JWeb Framework - Implementation Plan

A full-featured Java web framework for building web applications with state management, where you write both frontend and backend in pure Java.

## Vision
- **Full-stack Java**: Write frontend logic in Java, not JavaScript
- **Component-based**: Build UIs by composing reusable components
- **State management**: Reactive state that automatically updates the UI
- **API + Frontend hybrid**: REST APIs and UI in the same application
- **Optional annotations**: Use them for convenience, but never required
- **Accurate rendering**: Server-side rendering with client-side hydration
- **CSS support**: Full styling capabilities, scoped or global

---

## The Rendering Problem & Solution

### The Challenge
> "I don't want to just generate HTML - what if it's not accurate?"

You're right to be concerned. Simply generating HTML strings has problems:
- No validation that HTML structure is correct
- No type safety
- Hard to debug
- Dynamic updates require full page reloads

### Our Solution: Virtual DOM + Server-Client Sync

```
┌─────────────────────────────────────────────────────────────────┐
│                         SERVER (Java)                            │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │ Components  │───▶│ Virtual DOM │───▶│ HTML + State JSON   │  │
│  │ with State  │    │   (Valid)   │    │ (Initial Render)    │  │
│  └─────────────┘    └─────────────┘    └─────────────────────┘  │
│         │                                         │              │
│         │ State Changes                           │              │
│         ▼                                         ▼              │
│  ┌─────────────┐                        ┌─────────────────────┐  │
│  │  Diff/Patch │◀──── WebSocket ───────▶│     Browser         │  │
│  │  Calculator │                        │  (Minimal JS glue)  │  │
│  └─────────────┘                        └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

**How it works:**
1. **Server renders** components to a Virtual DOM (type-safe, validated)
2. **Virtual DOM converts** to HTML (guaranteed correct structure)
3. **Initial page load** sends HTML + serialized state
4. **User interactions** sent to server via WebSocket
5. **Server processes** event, updates state, calculates DOM diff
6. **Only changes** sent to browser (efficient updates)
7. **Tiny JS runtime** applies patches to real DOM

**This gives us:**
- ✅ Type-safe HTML (can't create invalid structure)
- ✅ Java handles all logic (no JS to write)
- ✅ Reactive updates without full page reloads
- ✅ Server-authoritative state (secure)
- ✅ Works without JS (graceful degradation)

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                      User Application                             │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐  │
│  │   Pages    │  │ Components │  │   State    │  │    API     │  │
│  │ @Route("/")│  │  Custom UI │  │  @State    │  │ @Get @Post │  │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘  │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                       JWeb Framework                              │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                     Core Layer                               │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐   │ │
│  │  │ Virtual  │ │  State   │ │Component │ │  Annotation   │   │ │
│  │  │   DOM    │ │ Manager  │ │ Lifecycle│ │  Processor    │   │ │
│  │  └──────────┘ └──────────┘ └──────────┘ └───────────────┘   │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                     Web Layer                                │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐   │ │
│  │  │  Router  │ │WebSocket │ │  HTTP    │ │ Static Files  │   │ │
│  │  │          │ │  Handler │ │  Server  │ │    + CSS      │   │ │
│  │  └──────────┘ └──────────┘ └──────────┘ └───────────────┘   │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                     API Layer                                │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐   │ │
│  │  │   REST   │ │   JSON   │ │ Request  │ │   Response    │   │ │
│  │  │ Handlers │ │  Codec   │ │  Parser  │ │   Builder     │   │ │
│  │  └──────────┘ └──────────┘ └──────────┘ └───────────────┘   │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                    Java HttpServer + WebSocket                    │
└──────────────────────────────────────────────────────────────────┘
```

---

## Developer Experience Goals

### 1. Simple Component with State
```java
public class Counter extends Component {
    @State private int count = 0;

    @Override
    public Element render() {
        return div(
            h1("Count: " + count),
            button("Increment").onClick(this::increment),
            button("Decrement").onClick(this::decrement)
        );
    }

    private void increment() {
        count++;
    }

    private void decrement() {
        count--;
    }
}
```

### 2. Page with Route (Annotation Style)
```java
@Route("/")
public class HomePage extends Page {
    @Override
    public Element render() {
        return html(
            head(
                title("My App"),
                css("/styles.css"),
                css(this::styles)  // Component-scoped CSS
            ),
            body(
                new Navbar(),
                new Counter(),
                new TodoList()
            )
        );
    }

    private CSS styles() {
        return CSS.create()
            .rule("body", css -> css
                .fontFamily("Arial, sans-serif")
                .margin("0")
                .padding("20px")
            );
    }
}
```

### 3. Page without Annotations (Fluent Style)
```java
public class AboutPage extends Page {
    @Override
    public Element render() {
        return html(
            body(h1("About Us"))
        );
    }
}

// Registration
JWeb.create()
    .route("/about", AboutPage::new)
    .start(8080);
```

### 4. REST API Endpoints
```java
@Api("/api/users")
public class UserApi {

    @Get
    public List<User> listUsers() {
        return userService.findAll();
    }

    @Get("/:id")
    public User getUser(@Param("id") Long id) {
        return userService.findById(id);
    }

    @Post
    public User createUser(@Body User user) {
        return userService.save(user);
    }

    @Delete("/:id")
    public void deleteUser(@Param("id") Long id) {
        userService.delete(id);
    }
}
```

### 5. API without Annotations
```java
JWeb.create()
    .get("/api/users", (req, res) -> userService.findAll())
    .post("/api/users", (req, res) -> {
        User user = req.bodyAs(User.class);
        return userService.save(user);
    })
    .start(8080);
```

### 6. Custom Reusable Component
```java
public class Card extends Component {
    private final String title;
    private final Element content;
    private String variant = "default";

    public Card(String title, Element content) {
        this.title = title;
        this.content = content;
    }

    public Card variant(String variant) {
        this.variant = variant;
        return this;
    }

    @Override
    public Element render() {
        return div(attrs().class_("card card-" + variant),
            div(attrs().class_("card-header"), text(title)),
            div(attrs().class_("card-body"), content)
        );
    }

    @Override
    protected CSS styles() {
        return CSS.create()
            .rule(".card", css -> css
                .border("1px solid #ddd")
                .borderRadius("8px")
                .overflow("hidden")
            )
            .rule(".card-header", css -> css
                .backgroundColor("#f5f5f5")
                .padding("12px")
                .fontWeight("bold")
            )
            .rule(".card-body", css -> css
                .padding("16px")
            );
    }
}

// Usage
new Card("Welcome", p("Hello there!")).variant("primary")
```

### 7. CSS Styling Options
```java
// Option 1: External CSS file
css("/styles.css")

// Option 2: Inline styles on elements
div(attrs().style("color: red; font-size: 16px"), ...)

// Option 3: Component-scoped CSS (Java DSL)
@Override
protected CSS styles() {
    return CSS.create()
        .rule(".my-class", css -> css
            .color("blue")
            .fontSize("14px")
        );
}

// Option 4: Global CSS in Java
JWeb.create()
    .globalCss(CSS.create()
        .rule("*", css -> css.boxSizing("border-box"))
        .rule("body", css -> css.margin("0"))
    )
```

### 8. State Management Across Components
```java
// Define application state
public class AppState {
    @State private User currentUser;
    @State private List<Todo> todos = new ArrayList<>();
    @State private String theme = "light";

    // Getters and mutators...
}

// Access in any component
public class Header extends Component {
    @Inject private AppState appState;

    @Override
    public Element render() {
        User user = appState.getCurrentUser();
        return header(
            user != null
                ? span("Welcome, " + user.getName())
                : link("/login", "Sign In")
        );
    }
}
```

---

## Package Structure

```
src/main/java/jweb/
├── JWeb.java                      # Main entry point & app builder
│
├── core/
│   ├── Component.java             # Base class for all components
│   ├── Page.java                  # Base class for full pages
│   ├── Element.java               # Interface for renderable elements
│   └── Renderable.java            # Marker interface
│
├── vdom/
│   ├── VNode.java                 # Virtual DOM node
│   ├── VText.java                 # Virtual text node
│   ├── VComponent.java            # Virtual component node
│   ├── VDom.java                  # Virtual DOM tree
│   ├── DiffEngine.java            # Calculates differences between trees
│   ├── Patch.java                 # Represents a DOM change
│   └── HtmlRenderer.java          # Converts VDom to valid HTML
│
├── elements/
│   ├── Tag.java                   # HTML tag implementation
│   ├── TextNode.java              # Text content (escaped)
│   ├── RawHtml.java               # Unescaped HTML (use carefully)
│   ├── Elements.java              # Static factories (div, p, h1, etc.)
│   └── FormElements.java          # Form-specific elements
│
├── attributes/
│   ├── Attributes.java            # Fluent attribute builder
│   ├── EventHandler.java          # Server-side event handlers
│   └── Attrs.java                 # Static helper
│
├── state/
│   ├── StateManager.java          # Manages component state
│   ├── StateProxy.java            # Detects state changes
│   ├── ReactiveValue.java         # Observable value wrapper
│   └── StateScope.java            # Request/Session/Application scope
│
├── css/
│   ├── CSS.java                   # CSS builder DSL
│   ├── CSSRule.java               # Single CSS rule
│   ├── CSSProperties.java         # Fluent property setters
│   ├── StyleSheet.java            # Collection of rules
│   └── ScopedStyles.java          # Component-scoped CSS
│
├── annotations/
│   ├── Route.java                 # @Route("/path")
│   ├── State.java                 # @State for reactive fields
│   ├── Inject.java                # @Inject for dependencies
│   ├── Api.java                   # @Api("/base") for REST
│   ├── Get.java                   # @Get, @Get("/:id")
│   ├── Post.java                  # @Post
│   ├── Put.java                   # @Put
│   ├── Delete.java                # @Delete
│   ├── Param.java                 # @Param("name")
│   ├── Body.java                  # @Body for request body
│   ├── Query.java                 # @Query("search")
│   └── AnnotationProcessor.java   # Processes all annotations
│
├── server/
│   ├── Server.java                # HTTP server wrapper
│   ├── WebSocketHandler.java      # WebSocket for live updates
│   ├── Request.java               # HTTP request abstraction
│   ├── Response.java              # HTTP response abstraction
│   └── Session.java               # Session management
│
├── routing/
│   ├── Router.java                # URL routing
│   ├── Route.java                 # Route definition
│   ├── RouteHandler.java          # Handler interface
│   ├── PathMatcher.java           # Path parameter matching
│   └── RouteGroup.java            # Grouped routes
│
├── api/
│   ├── ApiRouter.java             # REST API routing
│   ├── JsonCodec.java             # JSON serialization
│   ├── ApiResponse.java           # Standard API responses
│   └── ApiException.java          # API error handling
│
├── static_files/
│   ├── StaticFileHandler.java     # Serves static assets
│   └── MimeTypes.java             # MIME type detection
│
├── middleware/
│   ├── Middleware.java            # Middleware interface
│   ├── MiddlewareChain.java       # Chain execution
│   ├── CorsMiddleware.java        # CORS handling
│   └── AuthMiddleware.java        # Authentication
│
├── client/
│   └── jweb-client.js             # Minimal JS runtime (~2KB)
│       - WebSocket connection
│       - DOM patching
│       - Event forwarding
│
└── validation/
    ├── HtmlValidator.java         # Validates element nesting
    └── ElementRules.java          # HTML5 nesting rules
```

---

## Implementation Phases

### Phase 1: Core Foundation
**Goal**: Components render to valid HTML

1. **Virtual DOM**
   - [ ] `VNode` interface
   - [ ] `VElement` for tags with children validation
   - [ ] `VText` for text content
   - [ ] HTML5 nesting rules validation
   - [ ] `HtmlRenderer` that outputs correct HTML

2. **Element System**
   - [ ] `Element` interface
   - [ ] `Tag` implementation wrapping VNode
   - [ ] `Elements` static factory (div, p, h1-h6, span, a, etc.)
   - [ ] Automatic text wrapping

3. **Attribute System**
   - [ ] `Attributes` fluent builder
   - [ ] `class_()`, `id()`, `style()`, `href()`, etc.
   - [ ] Custom attributes via `attr(name, value)`
   - [ ] Boolean attributes (disabled, checked)

4. **Component Base**
   - [ ] `Component` abstract class
   - [ ] `render()` method
   - [ ] Component lifecycle hooks

5. **Basic HTTP Server**
   - [ ] Wrap `com.sun.net.httpserver.HttpServer`
   - [ ] Request/Response abstractions
   - [ ] Basic routing

6. **JWeb Entry Point**
   - [ ] `JWeb.create()` builder
   - [ ] `.route()` method
   - [ ] `.start(port)` method

**Milestone**: Render a static page with nested components

---

### Phase 2: CSS & Styling
**Goal**: Full styling capabilities

1. **External CSS**
   - [ ] `css("/path.css")` helper
   - [ ] Static file serving for CSS

2. **Inline Styles**
   - [ ] `attrs().style("...")`
   - [ ] Style string builder

3. **CSS Java DSL**
   - [ ] `CSS.create()` builder
   - [ ] `.rule(selector, properties)`
   - [ ] `CSSProperties` fluent API
   - [ ] All common CSS properties

4. **Scoped Styles**
   - [ ] Auto-generate unique class prefixes
   - [ ] Component `styles()` method
   - [ ] Inject scoped CSS into page head

---

### Phase 3: State Management
**Goal**: Reactive UI updates

1. **State Tracking**
   - [ ] `@State` annotation
   - [ ] StateProxy using reflection
   - [ ] Change detection

2. **WebSocket Communication**
   - [ ] WebSocket server endpoint
   - [ ] Client connection management
   - [ ] Message protocol design

3. **Client Runtime**
   - [ ] `jweb-client.js` (~2KB)
   - [ ] WebSocket connection
   - [ ] DOM patching logic
   - [ ] Event forwarding to server

4. **Diff Engine**
   - [ ] Virtual DOM diffing algorithm
   - [ ] Patch generation
   - [ ] Efficient update serialization

5. **Event Handling**
   - [ ] `onClick()`, `onSubmit()`, etc.
   - [ ] Event serialization to server
   - [ ] Handler invocation

**Milestone**: Counter component with live increment/decrement

---

### Phase 4: Complete HTML Elements
**Goal**: Build any UI

1. **All HTML5 Tags**
   - [ ] Semantic: header, footer, nav, main, article, section, aside
   - [ ] Text: p, span, strong, em, code, pre, blockquote
   - [ ] Headings: h1-h6
   - [ ] Lists: ul, ol, li, dl, dt, dd
   - [ ] Tables: table, thead, tbody, tfoot, tr, th, td
   - [ ] Media: img, video, audio, canvas, svg
   - [ ] Embeds: iframe, embed, object

2. **Form Elements**
   - [ ] form, input, textarea, select, option, optgroup
   - [ ] button, label, fieldset, legend
   - [ ] Input types: text, password, email, number, date, etc.
   - [ ] File upload handling

3. **Head Elements**
   - [ ] title, meta, link, script, style
   - [ ] Helper methods: `title()`, `css()`, `js()`, `meta()`

---

### Phase 5: Pages & Layouts
**Goal**: Structured applications

1. **Page Base Class**
   - [ ] `Page` extends `Component`
   - [ ] Default HTML5 structure
   - [ ] `<!DOCTYPE html>` handling
   - [ ] `head()` and `body()` hooks

2. **Layout System**
   - [ ] `Layout` class
   - [ ] Slot-based content: `slot("content")`
   - [ ] Nested layouts

3. **Page Metadata**
   - [ ] Title management
   - [ ] Meta tags
   - [ ] Open Graph support

---

### Phase 6: Routing & Navigation
**Goal**: Multi-page applications

1. **Route Annotation**
   - [ ] `@Route("/path")` annotation
   - [ ] Annotation scanning at startup
   - [ ] Path parameters: `@Route("/users/:id")`

2. **Programmatic Routing**
   - [ ] `.route("/path", Component::new)`
   - [ ] `.route("/path", (req) -> new Component(req))`

3. **Path Parameters**
   - [ ] `/users/:id/posts/:postId`
   - [ ] `request.param("id")`

4. **Query Parameters**
   - [ ] `request.query("search")`
   - [ ] `request.queryInt("page")`

5. **Navigation**
   - [ ] `link("/path", "Text")` component
   - [ ] Client-side navigation (no full reload)
   - [ ] `JWeb.navigate("/path")` programmatic

6. **Route Guards**
   - [ ] Before-route middleware
   - [ ] Authentication checks
   - [ ] Redirects

---

### Phase 7: REST API Layer
**Goal**: Full API support

1. **API Annotations**
   - [ ] `@Api("/base")` class annotation
   - [ ] `@Get`, `@Post`, `@Put`, `@Delete`, `@Patch`
   - [ ] `@Param`, `@Query`, `@Body`

2. **Programmatic API**
   - [ ] `.get("/api/...", handler)`
   - [ ] `.post("/api/...", handler)`
   - [ ] etc.

3. **JSON Handling**
   - [ ] Built-in JSON codec (no Jackson dependency initially)
   - [ ] `request.bodyAs(Class)`
   - [ ] Auto-serialize response objects

4. **API Response Helpers**
   - [ ] `Response.ok(data)`
   - [ ] `Response.created(data)`
   - [ ] `Response.notFound()`
   - [ ] `Response.error(message)`

5. **API Documentation**
   - [ ] Auto-generate OpenAPI spec (optional)

---

### Phase 8: Dependency Injection
**Goal**: Clean architecture

1. **Simple DI Container**
   - [ ] `@Inject` annotation
   - [ ] Singleton and request-scoped
   - [ ] Constructor injection

2. **Service Registration**
   - [ ] `JWeb.create().service(UserService.class, new UserServiceImpl())`
   - [ ] Auto-discovery option

---

### Phase 9: Sessions & Security
**Goal**: Production-ready

1. **Session Management**
   - [ ] Cookie-based sessions
   - [ ] `request.session()`
   - [ ] Session storage (in-memory, pluggable)

2. **Authentication Helpers**
   - [ ] `@Authenticated` annotation
   - [ ] Login/logout flow helpers

3. **CSRF Protection**
   - [ ] Token generation
   - [ ] Automatic form injection

4. **Input Validation**
   - [ ] `@Valid` annotation
   - [ ] Validation rules
   - [ ] Error messages

---

### Phase 10: Developer Experience
**Goal**: Joy to use

1. **Error Handling**
   - [ ] Pretty error pages (dev mode)
   - [ ] Stack trace display
   - [ ] Production error pages

2. **Logging**
   - [ ] Request logging
   - [ ] Component lifecycle logging
   - [ ] Configurable levels

3. **Hot Reload**
   - [ ] File watching
   - [ ] Auto-restart on changes
   - [ ] State preservation (if possible)

4. **Dev Tools**
   - [ ] Component inspector
   - [ ] State viewer
   - [ ] Network tab for WS messages

---

## HTML Validation Rules

The Virtual DOM enforces valid HTML structure:

```java
// This would throw ValidationException:
p(div("Can't put div inside p!"))

// Valid nesting is enforced:
div(p("Paragraph inside div - OK"))

// Self-closing tags can't have children:
img().child(span("Error!"))  // Throws exception
```

**Validation includes:**
- Block vs inline element nesting
- Void elements (no children): img, br, hr, input, meta, link
- Required children: ul/ol must contain li
- Unique attributes: no duplicate IDs warned
- Required attributes: img needs src (warning)

---

## The Client Runtime (jweb-client.js)

A minimal JavaScript file (~2KB minified) that:

```javascript
// Pseudocode of what it does:
class JWebClient {
    connect() {
        this.ws = new WebSocket('ws://...');
        this.ws.onmessage = (msg) => this.handlePatch(msg);
    }

    handlePatch(patches) {
        // Apply DOM patches from server
        patches.forEach(patch => {
            switch(patch.type) {
                case 'INSERT': // Insert new element
                case 'REMOVE': // Remove element
                case 'UPDATE': // Update attributes
                case 'TEXT':   // Update text content
            }
        });
    }

    sendEvent(elementId, eventType, data) {
        this.ws.send({ elementId, eventType, data });
    }
}
```

**User never writes JavaScript** - this runtime is bundled with JWeb.

---

## Full Example Application

```java
package com.example.todoapp;

import jweb.*;
import jweb.annotations.*;
import static jweb.Elements.*;
import static jweb.CSS.*;

public class TodoApp {
    public static void main(String[] args) {
        JWeb.create()
            .scan("com.example.todoapp")  // Scan for @Route, @Api
            .service(TodoService.class, new TodoServiceImpl())
            .staticFiles("/public")
            .start(8080);
    }
}

// === State ===
class AppState {
    @State List<Todo> todos = new ArrayList<>();
    @State String filter = "all"; // all, active, completed
}

// === Main Page ===
@Route("/")
class TodoPage extends Page {
    @Inject AppState state;
    @Inject TodoService todoService;

    @State String newTodoText = "";

    @Override
    protected Element head() {
        return head(
            title("Todo App - JWeb"),
            css("/styles.css")
        );
    }

    @Override
    protected Element body() {
        return body(attrs().class_("todo-app"),
            div(attrs().class_("container"),
                h1("Todos"),
                new TodoInput(this::addTodo),
                new TodoList(getFilteredTodos()),
                new TodoFilters(state.filter, this::setFilter)
            )
        );
    }

    private void addTodo(String text) {
        todoService.create(text);
        state.todos = todoService.findAll();
    }

    private void setFilter(String filter) {
        state.filter = filter;
    }

    private List<Todo> getFilteredTodos() {
        return switch(state.filter) {
            case "active" -> state.todos.stream()
                .filter(t -> !t.isCompleted()).toList();
            case "completed" -> state.todos.stream()
                .filter(Todo::isCompleted).toList();
            default -> state.todos;
        };
    }
}

// === Components ===
class TodoInput extends Component {
    private final Consumer<String> onAdd;
    @State private String text = "";

    public TodoInput(Consumer<String> onAdd) {
        this.onAdd = onAdd;
    }

    @Override
    public Element render() {
        return form(attrs().class_("todo-input").onSubmit(this::handleSubmit),
            input(attrs()
                .type("text")
                .placeholder("What needs to be done?")
                .value(text)
                .onInput(e -> text = e.getValue())
            ),
            button(attrs().type("submit"), text("Add"))
        );
    }

    private void handleSubmit(Event e) {
        e.preventDefault();
        if (!text.isBlank()) {
            onAdd.accept(text);
            text = "";
        }
    }
}

class TodoList extends Component {
    private final List<Todo> todos;

    public TodoList(List<Todo> todos) {
        this.todos = todos;
    }

    @Override
    public Element render() {
        return ul(attrs().class_("todo-list"),
            each(todos, todo -> new TodoItem(todo))
        );
    }
}

class TodoItem extends Component {
    @Inject TodoService todoService;
    private final Todo todo;

    public TodoItem(Todo todo) {
        this.todo = todo;
    }

    @Override
    public Element render() {
        return li(attrs().class_(todo.isCompleted() ? "completed" : ""),
            input(attrs()
                .type("checkbox")
                .checked(todo.isCompleted())
                .onChange(e -> toggle())
            ),
            span(todo.getText()),
            button(attrs().class_("delete").onClick(e -> delete()),
                text("×"))
        );
    }

    private void toggle() {
        todoService.toggle(todo.getId());
    }

    private void delete() {
        todoService.delete(todo.getId());
    }
}

// === REST API (for external access) ===
@Api("/api/todos")
class TodoApi {
    @Inject TodoService todoService;

    @Get
    public List<Todo> list() {
        return todoService.findAll();
    }

    @Post
    public Todo create(@Body CreateTodoRequest req) {
        return todoService.create(req.text());
    }

    @Put("/:id/toggle")
    public Todo toggle(@Param("id") Long id) {
        return todoService.toggle(id);
    }

    @Delete("/:id")
    public void delete(@Param("id") Long id) {
        todoService.delete(id);
    }
}
```

---

## Maven Project Structure

```
my-jweb-app/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/
│       │       ├── App.java
│       │       ├── pages/
│       │       ├── components/
│       │       ├── api/
│       │       └── services/
│       └── resources/
│           └── public/
│               ├── styles.css
│               └── images/
```

**pom.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-jweb-app</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.jweb</groupId>
            <artifactId>jweb-core</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.example.App</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Success Criteria

- [ ] Valid HTML output guaranteed by Virtual DOM
- [ ] Components with state that update without page reload
- [ ] Full CSS styling (external, inline, Java DSL, scoped)
- [ ] Works with and without annotations
- [ ] REST API endpoints alongside UI
- [ ] User writes zero JavaScript
- [ ] Single JAR dependency
- [ ] Runs on Java 17+
- [ ] Startup time < 1 second
- [ ] Intuitive for Java developers

---

## Next Steps

1. **Phase 1**: Build Virtual DOM + Element system with validation
2. Get a component rendering to valid HTML
3. Add basic routing and server
4. Then tackle state management (Phase 3)

Ready to start implementing?
