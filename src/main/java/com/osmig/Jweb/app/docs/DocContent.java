package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Documentation content sections with detailed explanations.
 */
public class DocContent {

    public static Element get(String section) {
        return switch (section) {
            case "setup" -> setup();
            case "routing" -> routing();
            case "templates" -> templates();
            case "styling" -> styling();
            case "state" -> state();
            case "forms" -> forms();
            case "form-builder" -> formBuilder();
            case "layouts" -> layouts();
            case "ui" -> uiComponents();
            case "api" -> api();
            default -> intro();
        };
    }

    // ==================== Intro Section ====================

    private static Element intro() {
        return section(
            sectionTitle("Introduction"),
            paragraph("JWeb is a Java web framework for building complete web applications entirely in Java. Define components, layouts, routes, styles, and state all in one language with full compile-time safety and IDE support."),

            subheading("Why JWeb?"),
            paragraph("Traditional web development requires multiple languages and toolchains. JWeb takes a unified approach:"),

            featureList(
                "Type-Safe Components - Compile-time verification of your entire UI",
                "Fluent Styling API - Define styles with IDE autocomplete and type checking",
                "Component Model - Create reusable UI components with the Template interface",
                "No Build Tools - No webpack, no npm, just Maven and Java",
                "Spring Boot Integration - Seamlessly works with the Spring ecosystem"
            ),

            subheading("Core Philosophy"),
            paragraph("JWeb follows a simple principle: your entire web application is Java. This means:"),

            codeBlock("""
                // A component in JWeb:
                div(class_("card"),
                    h1("Hello")
                )
                
                // Styling in JWeb:
                attrs().style()
                    .padding(rem(2))
                    .backgroundColor(hex("#f5f5f5"))
                .done()"""),

            paragraph("Components are Java classes. Styles are fluent builders. Routes are lambdas. Everything compiles together."),

            subheading("How It Works"),
            paragraph("JWeb uses a Virtual DOM architecture:"),

            orderedList(
                "You define components using JWeb's fluent API",
                "JWeb builds a Virtual DOM (VNode tree) from your components",
                "The Virtual DOM renders to the response",
                "Spring Boot serves your application"
            ),

            paragraph("This architecture provides server-side rendering with a component-based development model, all in pure Java.")
        );
    }

    // ==================== Setup Section ====================

    private static Element setup() {
        return section(
            sectionTitle("Getting Started"),
            paragraph("Get up and running with JWeb in minutes. This guide covers installation, project structure, and creating your first route."),

            subheading("Prerequisites"),
            featureList(
                "Java 17 or higher",
                "Maven 3.6+",
                "Your favorite IDE (IntelliJ IDEA, VS Code, Eclipse)"
            ),

            subheading("Adding JWeb to Your Project"),
            paragraph("JWeb is currently in active development. A public release will be available soon on Maven Central. Stay tuned for updates!"),

            subheading("Project Structure"),
            paragraph("A typical JWeb application follows this structure:"),

            codeBlock("""
src/main/java/com/yourapp/
├── App.java           # Spring Boot entry point
├── Routes.java        # Route definitions
├── layout/
│   ├── Layout.java    # Main page layout
│   ├── Nav.java       # Navigation component
│   ├── Footer.java    # Footer component
│   └── Theme.java     # Design tokens (colors, spacing)
├── pages/
│   ├── HomePage.java  # Home page
│   └── AboutPage.java # About page
└── partials/
    └── Card.java      # Reusable card component"""),

            subheading("Creating Your First Route"),
            paragraph("Routes are defined in a class implementing JWebRoutes:"),

            codeBlock("""
@Component
public class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        // Simple route returning an element
        app.get("/", () -> h1("Hello World"));

        // Route with a page component
        app.get("/about", ctx ->
            new Layout("About", new AboutPage().render()).render()
        );
    }
}"""),

            subheading("Static Imports"),
            paragraph("For the cleanest syntax, add these static imports to your classes:"),

            codeBlock("""
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;"""),

            subheading("Running Your App"),
            paragraph("Start your application like any Spring Boot app:"),

            codeBlock("""
mvn spring-boot:run

# Or run the main class directly
java -jar target/your-app.jar"""),

            paragraph("Your app will be available at http://localhost:8080")
        );
    }

    // ==================== Routing Section ====================

    private static Element routing() {
        return section(
            sectionTitle("Routing"),
            paragraph("JWeb provides a clean, fluent API for defining routes. Routes map URL paths to handlers that return HTML elements."),

            subheading("Basic Routes"),
            paragraph("Define routes using HTTP method helpers on the JWeb app:"),

            codeBlock("""
app.get("/", () -> h1("Home Page"));
app.get("/about", () -> div(h1("About"), p("Learn more...")));
app.post("/submit", req -> handleSubmit(req));
app.put("/users/:id", req -> updateUser(req));
app.delete("/users/:id", req -> deleteUser(req));"""),

            subheading("Path Parameters"),
            paragraph("Capture dynamic segments using :paramName syntax:"),

            codeBlock("""
// Route: /users/:id
app.get("/users/:id", req -> {
    String userId = req.param("id");
    return div(h1("User Profile: " + userId));
});

// Route: /posts/:category/:slug
app.get("/posts/:category/:slug", req -> {
    String category = req.param("category");
    String slug = req.param("slug");
    return article(h1(slug), span("Category: " + category));
});"""),

            subheading("Query Parameters"),
            paragraph("Access query string parameters with req.query():"),

            codeBlock("""
// URL: /search?q=java&page=2
app.get("/search", req -> {
    String query = req.query("q");
    String page = req.query("page", "1"); // with default
    return div(
        h1("Search: " + query),
        p("Page: " + page)
    );
});"""),

            subheading("Request Body"),
            paragraph("For POST/PUT requests, access form data and JSON bodies:"),

            codeBlock("""
app.post("/login", req -> {
    // Form data
    String email = req.body("email");
    String password = req.body("password");

    // Or get all form data as a Map
    Map<String, String> formData = req.formData();

    return authenticate(email, password);
});"""),

            subheading("Route Handlers"),
            paragraph("Routes can return different types:"),

            codeBlock("""
// Return an Element (rendered as HTML)
app.get("/page", () -> div("Hello"));

// Return a Template
app.get("/home", () -> new HomePage());

// Return a Response object for more control
app.get("/api/data", req ->
    Response.json(Map.of("status", "ok"))
);

// Redirect to another route
app.get("/old-page", req ->
    Response.redirect("/new-page")
);"""),

            subheading("Layouts with Routes"),
            paragraph("Wrap pages in layouts for consistent structure:"),

            codeBlock("""
app.get("/about", ctx ->
    new Layout("About Us", new AboutPage().render()).render()
);

// Or create a helper method:
private Element withLayout(String title, Element content) {
    return new Layout(title, content).render();
}

app.get("/contact", ctx ->
    withLayout("Contact", new ContactPage().render())
);"""),

            subheading("Middleware"),
            paragraph("Add cross-cutting concerns with middleware:"),

            codeBlock("""
// Apply middleware globally
app.use(Middlewares.logging());
app.use(Middlewares.csrf());

// Apply to specific paths
app.use("/admin", Middlewares.auth());

// Apply conditionally
app.useIf(isProd, Middlewares.compression());""")
        );
    }

    // ==================== Templates Section ====================

    private static Element templates() {
        return section(
            sectionTitle("Templates"),
            paragraph("Templates are the building blocks of JWeb applications. They're Java classes that implement the Template interface and define reusable UI components."),

            subheading("The Template Interface"),
            paragraph("Every component implements Template with a single render() method:"),

            codeBlock("""
public interface Template extends Element {
    Element render();
}"""),

            subheading("Creating a Component"),
            paragraph("Here's a simple Card component:"),

            codeBlock("""
public class Card implements Template {
    private final String title;
    private final String content;

    public Card(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Element render() {
        return div(attrs().class_("card").style()
                .padding(SP_4).backgroundColor(hex("#f8fafc"))
                .borderRadius(ROUNDED).done(),
            h3(attrs().style().fontSize(TEXT_LG).fontWeight(600).done(),
                text(title)),
            p(attrs().style().color(TEXT_LIGHT).marginTop(SP_2).done(),
                text(content))
        );
    }
}"""),

            subheading("Using Components"),
            paragraph("Components are used like any other element:"),

            codeBlock("""
// In another component or page
div(class_("container"),
    new Card("Welcome", "Hello World!"),
    new Card("Features", "Build apps in pure Java"),
    new Card("Learn More", "Read the documentation")
)"""),

            subheading("Component Composition"),
            paragraph("Components can contain other components:"),

            codeBlock("""
public class CardGrid implements Template {
    private final List<Card> cards;

    public CardGrid(List<Card> cards) {
        this.cards = cards;
    }

    @Override
    public Element render() {
        return div(attrs().style()
                .display(grid)
                .prop("grid-template-columns", "repeat(3, 1fr)")
                .gap(SP_4).done(),
            each(cards, card -> card.render())
        );
    }
}"""),

            subheading("Layout Components"),
            paragraph("Layouts wrap pages with common structure like headers and footers:"),

            codeBlock("""
public class Layout implements Template {
    private final String title;
    private final Element content;

    public Layout(String title, Element content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Element render() {
        return html(
            head(title(title)),
            body(attrs().style()
                    .display(flex).flexDirection(column)
                    .minHeight(vh(100)).done(),
                new Nav().render(),
                main(attrs().style().prop("flex", "1").done(),
                    content),
                new Footer().render()
            )
        );
    }
}"""),

            subheading("Conditional Rendering"),
            paragraph("Use when() and ifElse() for conditional content:"),

            codeBlock("""
// Show element only if condition is true
when(isLoggedIn, () ->
    span("Welcome, " + userName)
)

// Choose between two elements
ifElse(isAdmin,
    () -> a("/admin", "Admin Panel"),
    () -> a("/dashboard", "Dashboard")
)"""),

            subheading("List Rendering"),
            paragraph("Use each() to render collections:"),

            codeBlock("""
List<User> users = getUsers();

ul(each(users, user ->
    li(
        strong(user.getName()),
        span(" - " + user.getEmail())
    )
))"""),

            subheading("Component Organization"),
            paragraph("Organize components by their purpose:"),

            codeBlock("""
app/
├── layout/      # Layouts wrap entire pages
│   ├── Layout.java
│   └── AdminLayout.java
├── pages/       # Full page components
│   ├── HomePage.java
│   └── ProfilePage.java
└── partials/    # Reusable pieces
    ├── Card.java
    ├── Button.java
    └── Modal.java""")
        );
    }

    // ==================== Styling Section ====================

    private static Element styling() {
        return section(
            sectionTitle("Styling"),
            paragraph("JWeb provides a comprehensive type-safe styling API. Define styles with IDE autocomplete, compile-time checks, and full refactoring support."),

            subheading("Inline Styles"),
            paragraph("Apply styles directly to elements using the fluent style builder:"),

            codeBlock("""
div(attrs().style()
    .display(flex)
    .padding(rem(2))
    .backgroundColor(hex("#f5f5f5"))
    .borderRadius(px(8))
.done(),
    text("Styled content")
)"""),

            subheading("Units"),
            paragraph("All standard units are available as type-safe methods:"),

            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

// Length units
px(16)          // 16px
rem(1.5)        // 1.5rem
em(2)           // 2em
percent(50)     // 50%

// Viewport units
vh(100)         // 100vh
vw(50)          // 50vw
vmin(10)        // 10vmin

// Time units (for animations)
s(0.3)          // 0.3s
ms(300)         // 300ms

// Angle units
deg(45)         // 45deg
turn(0.5)       // 0.5turn

// Special values
zero            // 0
auto            // auto
none            // none"""),

            subheading("Colors"),
            paragraph("Named colors and color functions:"),

            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSColors.*;

// Named colors
red, blue, green, white, black, gray

// Hex colors
hex("#6366f1")
hex("#fff")

// RGB/RGBA
rgb(99, 102, 241)
rgba(0, 0, 0, 0.5)

// HSL/HSLA
hsl(239, 84, 67)
hsla(239, 84, 67, 0.8)

// Special colors
transparent
currentColor"""),

            subheading("Layout Properties"),
            paragraph("Flexbox and Grid layouts:"),

            codeBlock("""
// Flexbox
div(attrs().style()
    .display(flex)
    .flexDirection(column)
    .justifyContent(center)
    .alignItems(center)
    .gap(rem(1))
.done())

// Grid
div(attrs().style()
    .display(grid)
    .prop("grid-template-columns", "repeat(3, 1fr)")
    .gap(SP_4)
.done())"""),

            subheading("Common Style Values"),
            paragraph("Pre-defined constants for common values:"),

            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSS.*;

// Display
block, inline, flex, grid, inlineBlock, inlineFlex

// Position
relative, absolute, fixed, sticky

// Flexbox
row, column, center, flexStart, flexEnd
spaceBetween, spaceAround, stretch

// Border styles
solid, dashed, dotted, double_

// Text
left, right, center, justify
underline, uppercase, lowercase

// Timing (for transitions)
ease, easeIn, easeOut, linear"""),

            subheading("Design Tokens (Theme)"),
            paragraph("Create a Theme class for consistent design:"),

            codeBlock("""
public final class Theme {
    // Colors
    public static final CSSValue PRIMARY = hex("#6366f1");
    public static final CSSValue TEXT = hex("#1e293b");
    public static final CSSValue TEXT_LIGHT = hex("#64748b");

    // Spacing scale
    public static final CSSValue SP_2 = rem(0.5);
    public static final CSSValue SP_4 = rem(1);
    public static final CSSValue SP_8 = rem(2);

    // Font sizes
    public static final CSSValue TEXT_SM = rem(0.875);
    public static final CSSValue TEXT_LG = rem(1.125);
    public static final CSSValue TEXT_2XL = rem(1.5);

    // Border radius
    public static final CSSValue ROUNDED = px(6);
}

// Usage:
import static com.yourapp.layout.Theme.*;

div(attrs().style()
    .color(PRIMARY)
    .padding(SP_4)
    .fontSize(TEXT_LG)
.done())"""),

            subheading("Transitions & Animations"),
            paragraph("Add smooth transitions:"),

            codeBlock("""
// Simple transition
.transition(propBackgroundColor, s(0.2), ease)

// Multiple transitions
.transition(transitions(
    trans(propColor, s(0.2), ease),
    trans(propTransform, s(0.3), easeOut)
))

// Transforms
.transform(translate(px(10), px(20)))
.transform(rotate(deg(45)))
.transform(scale(1.1))"""),

            subheading("Gradients"),
            paragraph("Create gradient backgrounds:"),

            codeBlock("""
// Linear gradient
.background(linearGradient(red, blue))
.background(linearGradient("to right", hex("#6366f1"), hex("#8b5cf6")))

// Radial gradient
.background(radialGradient(white, black))

// Conic gradient
.background(conicGradient(red, yellow, green, blue, red))"""),

            subheading("Custom Properties"),
            paragraph("Reference custom properties in styles:"),

            codeBlock("""
// Reference a CSS variable
.color(var("primary-color"))
.color(var("text-color", black)) // with fallback""")
        );
    }

    // ==================== State Section ====================

    private static Element state() {
        return section(
            sectionTitle("State Management"),
            paragraph("JWeb provides reactive state management with the useState hook. State changes trigger re-renders and can be synchronized across clients via WebSocket."),

            subheading("Creating State"),
            paragraph("Use useState() to create reactive state containers:"),

            codeBlock("""
import static com.osmig.Jweb.framework.state.StateHooks.*;

public class Counter implements Template {
    private final State<Integer> count = useState(0);

    @Override
    public Element render() {
        return div(
            h1("Count: " + count.get()),
            button(attrs().onClick(e -> count.set(count.get() + 1)),
                text("Increment"))
        );
    }
}"""),

            subheading("Reading State"),
            paragraph("Use get() to read the current value:"),

            codeBlock("""
State<String> name = useState("John");

// Read the value
String currentName = name.get();

// Use in render
p("Hello, " + name.get())"""),

            subheading("Updating State"),
            paragraph("Use set() to update the value, or update() for transformations:"),

            codeBlock("""
State<Integer> count = useState(0);

// Direct set
count.set(10);

// Update based on current value
count.update(c -> c + 1);

// With complex objects
State<List<String>> items = useState(new ArrayList<>());
items.update(list -> {
    list.add("New Item");
    return list;
});"""),

            subheading("Subscribing to Changes"),
            paragraph("React to state changes with subscribers:"),

            codeBlock("""
State<String> searchTerm = useState("");

// Subscribe to changes
searchTerm.subscribe(newValue -> {
    System.out.println("Search changed to: " + newValue);
    // Trigger side effects, API calls, etc.
});

// Unsubscribe when done
searchTerm.unsubscribe(subscriber);"""),

            subheading("State with Forms"),
            paragraph("Bind state to form inputs:"),

            codeBlock("""
public class LoginForm implements Template {
    private final State<String> email = useState("");
    private final State<String> password = useState("");

    @Override
    public Element render() {
        return form(attrs().onSubmit(this::handleSubmit),
            input(attrs()
                .type("email")
                .value(email.get())
                .onInput(e -> email.set(e.value()))),
            input(attrs()
                .type("password")
                .value(password.get())
                .onInput(e -> password.set(e.value()))),
            button(type("submit"), text("Login"))
        );
    }

    private void handleSubmit(Event e) {
        e.preventDefault();
        authenticate(email.get(), password.get());
    }
}"""),

            subheading("State Patterns"),
            paragraph("Common patterns for managing state:"),

            codeBlock("""
// Boolean toggle
State<Boolean> isOpen = useState(false);
button(attrs().onClick(e -> isOpen.update(open -> !open)),
    text(isOpen.get() ? "Close" : "Open"))

// List management
State<List<Todo>> todos = useState(new ArrayList<>());

// Add item
todos.update(list -> {
    list.add(new Todo("New task"));
    return list;
});

// Remove item
todos.update(list -> {
    list.removeIf(t -> t.getId().equals(id));
    return list;
});

// Object state
State<User> user = useState(new User("John", "john@example.com"));
user.update(u -> u.withName("Jane")); // immutable update"""),

            subheading("State Serialization"),
            paragraph("State can be serialized to JSON for WebSocket sync:"),

            codeBlock("""
State<Integer> count = useState(0);

// Serialize to JSON
String json = count.toJson();
// {"id": "state_1", "value": 0}

// State IDs are unique and stable
String id = count.getId();""")
        );
    }

    // ==================== Forms Section ====================

    private static Element forms() {
        return section(
            sectionTitle("Forms"),
            paragraph("JWeb provides fluent APIs for building forms and validating user input with type-safe validators."),

            subheading("Building Forms"),
            paragraph("Use the Elements DSL to create form elements:"),

            codeBlock("""
form(attrs().action("/submit").method("POST"),
    div(class_("form-group"),
        label(for_("email"), text("Email")),
        input(attrs()
            .type("email")
            .id("email")
            .name("email")
            .placeholder("you@example.com")
            .required())
    ),
    div(class_("form-group"),
        label(for_("password"), text("Password")),
        input(attrs()
            .type("password")
            .id("password")
            .name("password")
            .required())
    ),
    button(type("submit"), text("Sign In"))
)"""),

            subheading("Form Elements"),
            paragraph("All standard HTML form elements are available:"),

            codeBlock("""
// Text inputs
input(attrs().type("text").name("username"))
input(attrs().type("email").name("email"))
input(attrs().type("password").name("password"))
input(attrs().type("number").name("age"))

// Textarea
textarea(attrs().name("bio").placeholder("Tell us about yourself"))

// Select dropdown
select(attrs().name("country"),
    option("us", "United States"),
    option("uk", "United Kingdom"),
    option("ca", "Canada")
)

// Checkboxes and radios
input(attrs().type("checkbox").name("agree").checked())
input(attrs().type("radio").name("plan").value("basic"))

// File upload
input(attrs().type("file").name("avatar"))

// Hidden fields
input(attrs().type("hidden").name("csrf").value(token))"""),

            subheading("Form Validation"),
            paragraph("Use FormValidator for server-side validation:"),

            codeBlock("""
import com.osmig.Jweb.framework.validation.*;

app.post("/register", req -> {
    Map<String, String> form = req.formData();

    ValidationResult result = FormValidator.create()
        .field("email", form.get("email"))
            .required()
            .email()
        .field("password", form.get("password"))
            .required()
            .minLength(8)
            .maxLength(100)
        .field("confirmPassword", form.get("confirmPassword"))
            .required()
            .matches("password", form.get("password"))
        .field("age", form.get("age"))
            .optional()
            .numeric()
        .validate();

    if (result.hasErrors()) {
        return showErrors(result.getErrors());
    }

    return createUser(form);
});"""),

            subheading("Available Validators"),
            paragraph("Built-in validation rules:"),

            codeBlock("""
.required()              // Field must have a value
.optional()              // Skip validation if empty
.minLength(8)            // Minimum string length
.maxLength(100)          // Maximum string length
.lengthBetween(3, 50)    // Length range
.email()                 // Valid email format
.url()                   // Valid URL format
.numeric()               // Only digits
.alpha()                 // Only letters
.alphanumeric()          // Letters and digits
.phone()                 // Phone number format
.pattern(regex, msg)     // Custom regex pattern
.matches(field, value)   // Must match another field"""),

            subheading("Custom Validators"),
            paragraph("Create custom validation rules:"),

            codeBlock("""
// Using check() for simple predicates
.check(s -> s.startsWith("@"), "Must start with @")

// Using custom() for complex validators
.custom(Validator.of(
    value -> !forbiddenWords.contains(value.toLowerCase()),
    field -> field + " contains forbidden words"
))"""),

            subheading("Displaying Errors"),
            paragraph("Show validation errors to users:"),

            codeBlock("""
private Element showErrors(Map<String, List<String>> errors) {
    return div(class_("errors"),
        each(errors.entrySet(), entry ->
            div(class_("error"),
                strong(entry.getKey() + ": "),
                span(String.join(", ", entry.getValue()))
            )
        )
    );
}"""),

            subheading("Event Handlers"),
            paragraph("Handle form events with Java callbacks:"),

            codeBlock("""
// Form submit
form(attrs().onSubmit(e -> {
    e.preventDefault();
    submitForm();
}))

// Input change
input(attrs().onChange(e -> {
    String value = e.value();
    updateState(value);
}))

// Real-time input
input(attrs().onInput(e -> {
    searchTerm.set(e.value());
}))

// Focus/blur
input(attrs()
    .onFocus(e -> showHint())
    .onBlur(e -> validateField()))""")
        );
    }

    // ==================== Form Builder Section ====================

    private static Element formBuilder() {
        return section(
            sectionTitle("Form Builder"),
            paragraph("The Form Builder provides a fluent DSL for creating forms with minimal boilerplate. Build complete forms with labels, validation, and proper structure in a readable, chainable API."),

            subheading("Basic Usage"),
            paragraph("Import the Form class and use Form.create() to start building:"),

            codeBlock("""
import static com.osmig.Jweb.framework.forms.Form.*;

Form.create()
    .action("/contact")
    .method("POST")
    .text("name", f -> f.label("Full Name").placeholder("John Doe").required())
    .email("email", f -> f.label("Email").required())
    .submit("Send Message")
    .build()"""),

            subheading("Field Types"),
            paragraph("The Form Builder supports all common input types:"),

            codeBlock("""
Form.create()
    // Text inputs
    .text("username", f -> f.label("Username").required())
    .email("email", f -> f.label("Email"))
    .password("password", f -> f.label("Password").minLength(8))
    .url("website", f -> f.label("Website"))
    .tel("phone", f -> f.label("Phone"))
    .number("age", f -> f.label("Age").min(0).max(120))

    // Date/time inputs
    .date("birthdate", f -> f.label("Birth Date"))
    .time("meeting", f -> f.label("Meeting Time"))
    .datetime("event", f -> f.label("Event Date/Time"))

    // Other inputs
    .textarea("bio", f -> f.label("Bio").rows(4))
    .checkbox("terms", f -> f.label("I agree to the terms"))
    .hidden("csrf", csrfToken)
    .build()"""),

            subheading("Field Configuration"),
            paragraph("Each field can be configured with validation and display options:"),

            codeBlock("""
.text("username", f -> f
    .label("Username")           // Form label
    .placeholder("johndoe")      // Placeholder text
    .required()                  // Required field
    .minLength(3)                // Minimum length
    .maxLength(20)               // Maximum length
    .pattern("[a-z0-9]+")        // Regex pattern
    .autocomplete("username")    // Autocomplete hint
    .autofocus()                 // Auto focus on load
    .disabled()                  // Disabled field
    .readonly()                  // Read-only field
    .value("default")            // Default value
    .helpText("3-20 characters") // Help text below field
)"""),

            subheading("Select Dropdowns"),
            paragraph("Create select dropdowns with options:"),

            codeBlock("""
.select("country", s -> s
    .label("Country")
    .placeholder("Select a country")
    .required()
    .option("us", "United States")
    .option("uk", "United Kingdom")
    .option("ca", "Canada")
    .option("de", "Germany")
    .selected("us")  // Pre-selected value
)"""),

            subheading("Radio Groups"),
            paragraph("Create radio button groups for single selection:"),

            codeBlock("""
.radio("plan", r -> r
    .label("Subscription Plan")
    .required()
    .option("basic", "Basic - $9/month")
    .option("pro", "Professional - $29/month")
    .option("enterprise", "Enterprise - $99/month")
    .selected("pro")
)"""),

            subheading("Form Buttons"),
            paragraph("Add submit and reset buttons:"),

            codeBlock("""
.submit("Create Account")     // Submit button text
.reset("Clear Form")          // Optional reset button

// Or with custom styling:
.submitButton(b -> b
    .text("Sign Up")
    .class_("btn-primary")
    .disabled()
)"""),

            subheading("Complete Example"),
            paragraph("Here's a complete registration form:"),

            codeBlock("""
Form.create()
    .class_("registration-form")
    .action("/register")
    .method("POST")

    // Personal Info
    .text("firstName", f -> f.label("First Name").required())
    .text("lastName", f -> f.label("Last Name").required())
    .email("email", f -> f.label("Email Address").required())

    // Account
    .password("password", f -> f
        .label("Password")
        .required()
        .minLength(8)
        .helpText("At least 8 characters"))
    .password("confirmPassword", f -> f
        .label("Confirm Password")
        .required())

    // Preferences
    .select("country", s -> s
        .label("Country")
        .option("us", "United States")
        .option("uk", "United Kingdom")
        .required())

    .radio("plan", r -> r
        .label("Plan")
        .option("free", "Free")
        .option("pro", "Pro - $10/mo")
        .selected("free"))

    .checkbox("newsletter", f -> f
        .label("Subscribe to newsletter"))
    .checkbox("terms", f -> f
        .label("I agree to the Terms of Service")
        .required())

    .submit("Create Account")
    .build()""")
        );
    }

    // ==================== Layouts Section ====================

    private static Element layouts() {
        return section(
            sectionTitle("Layout Helper"),
            paragraph("The Layout module provides pre-built layout patterns for common UI structures. Stop writing repetitive flexbox and grid CSS - use semantic layout components instead."),

            subheading("Import"),
            paragraph("Import the Layout class to use layout helpers:"),

            codeBlock("""
import static com.osmig.Jweb.framework.layout.Layout.*;"""),

            subheading("Page Structure"),
            paragraph("Create standard page layouts with header, main, and footer:"),

            codeBlock("""
// Full page with header, main, footer
Layout.page(
    Layout.header(
        a(attrs().href("/"), text("Logo")),
        nav(
            a(attrs().href("/about"), text("About")),
            a(attrs().href("/contact"), text("Contact"))
        )
    ),
    Layout.main(
        Layout.container(
            h1("Welcome"),
            p("Page content here...")
        )
    ),
    Layout.footer(
        text("© 2024 My Company")
    )
)"""),

            subheading("Containers"),
            paragraph("Center content with max-width containers:"),

            codeBlock("""
// Default container (max-width: 1200px)
Layout.container(
    h1("Centered Content"),
    p("This content has a maximum width and is centered.")
)

// Narrow container (max-width: 800px) - good for reading
Layout.narrow(
    article(
        h1("Blog Post Title"),
        p("Article content that's easy to read...")
    )
)

// Wide container (max-width: 1400px)
Layout.wide(content)

// Custom max-width
Layout.container(px(960), content)"""),

            subheading("Flexbox Layouts"),
            paragraph("Quick flexbox layouts for common patterns:"),

            codeBlock("""
// Horizontal row
Layout.row(child1, child2, child3)
Layout.row(rem(2), child1, child2)  // with gap

// Vertical column
Layout.column(child1, child2, child3)
Layout.column(rem(1), child1, child2)  // with gap

// Centered content
Layout.center(
    h1("Perfectly Centered")
)

// Space between items
Layout.spaceBetween(
    div(text("Left")),
    div(text("Right"))
)

// Wrapping items with gap
Layout.wrap(rem(1),
    badge("Tag 1"),
    badge("Tag 2"),
    badge("Tag 3")
)"""),

            subheading("Grid Layouts"),
            paragraph("CSS Grid layouts made simple:"),

            codeBlock("""
// Equal columns
Layout.grid(3,  // 3 columns
    card1, card2, card3,
    card4, card5, card6
)

// Grid with gap
Layout.grid(3, rem(2),
    card1, card2, card3
)

// Auto-fit grid (responsive)
Layout.autoGrid(px(300), rem(1.5),
    card1, card2, card3, card4
)  // Cards are at least 300px, columns adjust automatically"""),

            subheading("Multi-Column Layouts"),
            paragraph("Create column layouts with custom ratios:"),

            codeBlock("""
// Equal width columns
Layout.columns(2, child1, child2)
Layout.columns(3, rem(2), child1, child2, child3)

// Custom column ratios
Layout.columns("1fr", "2fr", rem(2),
    sidebar,  // 1/3 width
    content   // 2/3 width
)"""),

            subheading("Sidebar Layouts"),
            paragraph("Common sidebar patterns:"),

            codeBlock("""
// Left sidebar
Layout.sidebar(px(250),      // sidebar width
    div(text("Sidebar")),    // sidebar content
    div(text("Main content")) // main content
)

// Right sidebar
Layout.sidebarRight(px(300),
    div(text("Main content")),
    div(text("Sidebar"))
)"""),

            subheading("Stack and Cluster"),
            paragraph("Vertical stacking and horizontal clustering:"),

            codeBlock("""
// Vertical stack with consistent spacing
Layout.stack(rem(1),
    h1("Title"),
    p("Paragraph 1"),
    p("Paragraph 2"),
    p("Paragraph 3")
)

// Horizontal cluster (good for tags, buttons)
Layout.cluster(rem(0.5),
    button("Save"),
    button("Cancel"),
    button("Delete")
)"""),

            subheading("Special Layouts"),
            paragraph("Other useful layout patterns:"),

            codeBlock("""
// Split layout (left/right)
Layout.split(
    div(text("Left side")),
    div(text("Right side"))
)

// Aspect ratio container
Layout.aspectRatio("16/9",
    img(attrs().src("/video-thumbnail.jpg"))
)

// Full-height centered cover
Layout.fullCover(
    div(
        h1("Hero Section"),
        p("Full viewport height, centered content")
    )
)

// Sticky header
Layout.stickyHeader(
    nav(text("This sticks to top when scrolling"))
)

// Scrollable container
Layout.scrollable(px(400),
    longContent
)

// Card with padding and shadow
Layout.card(
    h3("Card Title"),
    p("Card content...")
)

// Dividers and spacers
Layout.divider()           // horizontal rule
Layout.verticalDivider(px(24))
Layout.spacer()            // flexible space
Layout.space(rem(2))       // fixed space""")
        );
    }

    // ==================== UI Components Section ====================

    private static Element uiComponents() {
        return section(
            sectionTitle("UI Components"),
            paragraph("The UI module provides pre-styled, reusable UI components. These components follow common design patterns and are ready to use out of the box."),

            subheading("Import"),
            paragraph("Import the UI class to use components:"),

            codeBlock("""
import static com.osmig.Jweb.framework.ui.UI.*;"""),

            subheading("Buttons"),
            paragraph("Pre-styled button variants:"),

            codeBlock("""
// Button variants
UI.primaryButton("Submit", e -> handleSubmit())
UI.secondaryButton("Cancel", e -> handleCancel())
UI.dangerButton("Delete", e -> handleDelete())
UI.ghostButton("Learn More", e -> navigate())
UI.linkButton("View Details", e -> showDetails())

// Icon button (for icons)
UI.iconButton(iconElement, e -> handleClick())"""),

            subheading("Badges and Tags"),
            paragraph("Small labels for status and categories:"),

            codeBlock("""
import static com.osmig.Jweb.framework.ui.UI.Badge.*;

// Colored badges
UI.badge("New", SUCCESS)    // green
UI.badge("Pending", WARNING) // yellow
UI.badge("Error", DANGER)    // red
UI.badge("Info", INFO)       // blue
UI.badge("Default", DEFAULT) // gray

// Tags (similar to badges, for categories)
import static com.osmig.Jweb.framework.ui.UI.Tag.*;

UI.tag("JavaScript", PRIMARY)
UI.tag("Tutorial", SECONDARY)"""),

            subheading("Alerts"),
            paragraph("Alert messages for notifications:"),

            codeBlock("""
import static com.osmig.Jweb.framework.ui.UI.Alert.*;

// Alert with icon
UI.alert("Operation completed successfully!", SUCCESS)
UI.alert("Please review your input.", WARNING)
UI.alert("Something went wrong.", DANGER)
UI.alert("Here's some useful information.", INFO)

// Shorthand methods
UI.successAlert("Saved successfully!")
UI.warningAlert("Check your connection")
UI.errorAlert("Failed to save")
UI.infoAlert("New features available")"""),

            subheading("Cards"),
            paragraph("Card containers for grouped content:"),

            codeBlock("""
// Basic card
UI.card(
    h3("Card Title"),
    p("Card content goes here..."),
    UI.primaryButton("Action", e -> {})
)

// Card is a styled container
// Includes padding, border-radius, and subtle shadow"""),

            subheading("Avatars"),
            paragraph("User avatars with initials or images:"),

            codeBlock("""
// Text avatar (shows initials)
UI.avatar("John Doe")    // Shows "JD"
UI.avatar("Alice")       // Shows "A"

// Image avatar
UI.avatarImage("/images/user.jpg", "John Doe")"""),

            subheading("Loading States"),
            paragraph("Components for loading and progress:"),

            codeBlock("""
// Progress bar (0-100)
UI.progressBar(75)  // 75% complete

// Spinning loader
UI.spinner()

// Skeleton loaders (content placeholders)
UI.skeleton(px(200), px(20))     // rectangle
UI.skeletonText()                 // text line
UI.skeletonCircle(px(48))        // circle (for avatar)"""),

            subheading("Typography Helpers"),
            paragraph("Styled code and keyboard elements:"),

            codeBlock("""
// Inline code
p(text("Use the "), UI.inlineCode("npm install"), text(" command"))

// Keyboard shortcut
p(text("Press "), UI.kbd("Ctrl"), text(" + "), UI.kbd("S"), text(" to save"))

// Code block with syntax highlighting colors
UI.codeBlock(\"""
function hello() {
    console.log("Hello, World!");
}
\""")"""),

            subheading("Navigation"),
            paragraph("Breadcrumb navigation:"),

            codeBlock("""
UI.breadcrumb(
    a(attrs().href("/"), text("Home")),
    a(attrs().href("/docs"), text("Documentation")),
    span(text("Current Page"))
)"""),

            subheading("Empty States"),
            paragraph("Placeholder for empty content:"),

            codeBlock("""
UI.emptyState(
    "No Results Found",
    "Try adjusting your search or filters"
)"""),

            subheading("Dividers"),
            paragraph("Visual separators:"),

            codeBlock("""
// Horizontal divider
UI.divider()

// With custom margin
div(
    content1,
    UI.divider(),
    content2
)"""),

            subheading("Complete Example"),
            paragraph("Here's a dashboard card using multiple UI components:"),

            codeBlock("""
import static com.osmig.Jweb.framework.ui.UI.*;
import static com.osmig.Jweb.framework.layout.Layout.*;

card(
    // Header with badge
    row(rem(1),
        h3("User Statistics"),
        badge("Live", Badge.SUCCESS)
    ),

    divider(),

    // Progress section
    stack(rem(0.5),
        p("Storage Used"),
        progressBar(65),
        p("65% of 100GB")
    ),

    divider(),

    // Actions
    cluster(rem(0.5),
        primaryButton("Upgrade", e -> {}),
        ghostButton("View Details", e -> {})
    )
)""")
        );
    }

    // ==================== DSL Reference Section ====================

    private static Element api() {
        return section(
            sectionTitle("DSL Reference"),
            paragraph("Quick reference for the core JWeb DSL."),

            subheading("Elements"),
            paragraph("All element types are available as static methods:"),

            codeBlock("""
// Document structure
html(), head(), body(), title(), meta(), link(), script(), style()

// Semantic elements
header(), footer(), nav(), main(), section(), article(), aside()

// Headings
h1(), h2(), h3(), h4(), h5(), h6()

// Text content
p(), span(), div(), strong(), em(), code(), pre(), blockquote()

// Lists
ul(), ol(), li(), dl(), dt(), dd()

// Tables
table(), thead(), tbody(), tr(), th(), td()

// Forms
form(), input(), textarea(), select(), option(), button(), label()

// Media
img(), video(), audio(), canvas(), svg(), iframe()

// Helpers
text("content")      // Text node
raw("<b>html</b>")   // Unescaped HTML
fragment(...)        // Group without wrapper
each(list, mapper)   // List iteration
when(cond, supplier) // Conditional
ifElse(cond, t, f)   // Conditional with else"""),

            subheading("Attributes"),
            paragraph("Fluent attribute builder methods:"),

            codeBlock("""
attrs()
    // Core
    .id("main")
    .class_("container")
    .addClass("active")
    .style().display(flex).done()

    // Links & Media
    .href("/page")
    .src("/image.png")
    .alt("Description")
    .target("_blank")
    .targetBlank()  // with noopener noreferrer

    // Forms
    .type("email")
    .name("email")
    .value("john@example.com")
    .placeholder("Enter email")
    .action("/submit")
    .method("POST")
    .for_("fieldId")

    // Boolean
    .disabled()
    .checked()
    .required()
    .readonly()
    .hidden()
    .autofocus()

    // Data & ARIA
    .data("userId", "123")
    .aria("label", "Close")
    .role("button")

    // Events
    .onClick(e -> ...)
    .onChange(e -> ...)
    .onInput(e -> ...)
    .onSubmit(e -> ...)
    .onFocus(e -> ...)
    .onBlur(e -> ...)
    .onKeyDown(e -> ...)"""),

            subheading("Style Properties"),
            paragraph("Style builder methods (partial list):"),

            codeBlock("""
.style()
    // Layout
    .display(flex)
    .position(relative)
    .top/right/bottom/left(value)
    .zIndex(10)

    // Flexbox
    .flexDirection(column)
    .justifyContent(center)
    .alignItems(center)
    .gap(rem(1))
    .flexGrow(1)

    // Sizing
    .width/height(value)
    .minWidth/maxWidth(value)
    .minHeight/maxHeight(value)

    // Spacing
    .margin(value)
    .margin(vertical, horizontal)
    .padding(value)
    .padding(vertical, horizontal)

    // Typography
    .color(hex("#333"))
    .fontSize(rem(1.25))
    .fontWeight(600)
    .fontFamily("system-ui")
    .lineHeight(1.5)
    .textAlign(center)
    .textDecoration(none)

    // Background
    .backgroundColor(white)
    .background(linearGradient(...))
    .backgroundImage(url("/bg.jpg"))

    // Border
    .border(px(1), solid, gray)
    .borderRadius(px(8))
    .borderColor(hex("#e5e7eb"))

    // Effects
    .boxShadow(px(0), px(4), px(6), rgba(0,0,0,0.1))
    .opacity(0.8)
    .overflow(hidden)
    .cursor(pointer)
    .transition(propAll, s(0.2), ease)
    .transform(scale(1.1))
.done()"""),

            subheading("Routing"),
            paragraph("Route definition methods:"),

            codeBlock("""
JWeb app = JWeb.create();

app.get(path, () -> element)    // Simple GET
app.get(path, req -> element)   // GET with request
app.post(path, req -> result)   // POST
app.put(path, req -> result)    // PUT
app.delete(path, req -> result) // DELETE

// Middleware
app.use(middleware)             // Global
app.use(path, middleware)       // Path-specific
app.useIf(cond, middleware)     // Conditional"""),

            subheading("Request Object"),
            paragraph("Request methods for handlers:"),

            codeBlock("""
req.param("id")           // Path parameter
req.query("page")         // Query parameter
req.query("page", "1")    // With default
req.body("email")         // Form field
req.formData()            // All form data as Map
req.header("Accept")      // Request header
req.cookie("session")     // Cookie value
req.method()              // HTTP method
req.path()                // Request path"""),

            subheading("State"),
            paragraph("Reactive state management:"),

            codeBlock("""
State<T> state = useState(initialValue);

state.get()               // Read value
state.set(newValue)       // Set value
state.update(v -> v + 1)  // Transform value
state.subscribe(callback) // Listen for changes
state.unsubscribe(cb)     // Remove listener
state.getId()             // Unique state ID
state.toJson()            // JSON serialization""")
        );
    }

    // ==================== Helper Methods ====================

    private static Element sectionTitle(String text) {
        return h1(attrs().style()
                .fontSize(TEXT_3XL).fontWeight(700).color(TEXT)
                .marginBottom(SP_4)
            .done(), text(text));
    }

    private static Element subheading(String text) {
        return h2(attrs().style()
                .fontSize(TEXT_XL).fontWeight(600).color(TEXT)
                .marginTop(SP_8).marginBottom(SP_3)
                .prop("border-bottom", "2px solid #e2e8f0")
                .paddingBottom(SP_2)
            .done(), text(text));
    }

    private static Element paragraph(String text) {
        return p(attrs().style()
                .color(TEXT_LIGHT).lineHeight(1.7).marginBottom(SP_4)
            .done(), text(text));
    }

    private static Element codeBlock(String code) {
        return pre(attrs().style()
                .backgroundColor(hex("#1e293b"))
                .color(hex("#e2e8f0"))
                .padding(SP_4)
                .borderRadius(ROUNDED)
                .overflow(scroll)
                .fontSize(TEXT_SM)
                .lineHeight(1.6)
                .marginBottom(SP_6)
            .done(),
            code(text(code))
        );
    }

    private static Element featureList(String... items) {
        return ul(attrs().style()
                .marginBottom(SP_6).paddingLeft(SP_6)
                .prop("list-style-type", "disc")
            .done(),
            each(java.util.List.of(items), item ->
                li(attrs().style()
                        .color(TEXT_LIGHT).lineHeight(1.8).marginBottom(SP_2)
                    .done(), text(item))
            )
        );
    }

    private static Element orderedList(String... items) {
        return ol(attrs().style()
                .marginBottom(SP_6).paddingLeft(SP_6)
            .done(),
            each(java.util.List.of(items), item ->
                li(attrs().style()
                        .color(TEXT_LIGHT).lineHeight(1.8).marginBottom(SP_2)
                    .done(), text(item))
            )
        );
    }
}
