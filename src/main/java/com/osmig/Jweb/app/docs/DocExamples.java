package com.osmig.Jweb.app.docs;

/**
 * Code examples for documentation pages.
 * Extracted to keep DocContent.java concise.
 */
public final class DocExamples {

    private DocExamples() {}

    // ==================== Intro Section ====================

    public static final String INTRO_PHILOSOPHY = """
                    // A component in JWeb:
                    div(class_("card"),
                        h1("Hello")
                    )
                    
                    // Styling in JWeb:
                    attrs().style()
                        .padding(rem(2))
                        .backgroundColor(hex("#f5f5f5"))
                    .done()""";

    // ==================== Setup Section ====================

    public static final String SETUP_PROJECT_STRUCTURE = """
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
                        └── Card.java      # Reusable card component""";

    public static final String SETUP_FIRST_ROUTE = """
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
                    }""";

    public static final String SETUP_IMPORTS = """
                    import static com.osmig.Jweb.framework.elements.Elements.*;
                    import static com.osmig.Jweb.framework.styles.CSS.*;
                    import static com.osmig.Jweb.framework.styles.CSSUnits.*;
                    import static com.osmig.Jweb.framework.styles.CSSColors.*;""";

    public static final String SETUP_RUN = """
                    mvn spring-boot:run
                    
                    # Or run the main class directly
                    java -jar target/your-app.jar""";

    // ==================== Routing Section ====================

    public static final String ROUTING_BASIC = """
                app.get("/", () -> h1("Home Page"));
                app.get("/about", () -> div(h1("About"), p("Learn more...")));
                app.post("/submit", req -> handleSubmit(req));
                app.put("/users/:id", req -> updateUser(req));
                app.delete("/users/:id", req -> deleteUser(req));""";

    public static final String ROUTING_PATH_PARAMS = """
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
                });""";

    public static final String ROUTING_QUERY_PARAMS = """
                // URL: /search?q=java&page=2
                app.get("/search", req -> {
                    String query = req.query("q");
                    String page = req.query("page", "1"); // with default
                    return div(
                        h1("Search: " + query),
                        p("Page: " + page)
                    );
                });""";

    public static final String ROUTING_REQUEST_BODY = """
                app.post("/login", req -> {
                    // Form data
                    String email = req.body("email");
                    String password = req.body("password");
                
                    // Or get all form data as a Map
                    Map<String, String> formData = req.formData();
                
                    return authenticate(email, password);
                });""";

    public static final String ROUTING_HANDLERS = """
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
                );""";

    public static final String ROUTING_LAYOUTS = """
                app.get("/about", ctx ->
                    new Layout("About Us", new AboutPage().render()).render()
                );
                
                // Or create a helper method:
                private Element withLayout(String title, Element content) {
                    return new Layout(title, content).render();
                }
                
                app.get("/contact", ctx ->
                    withLayout("Contact", new ContactPage().render())
                );""";

    public static final String ROUTING_MIDDLEWARE = """
                // Apply middleware globally
                app.use(Middlewares.logging());
                app.use(Middlewares.csrf());
                
                // Apply to specific paths
                app.use("/admin", Middlewares.auth());
                
                // Apply conditionally
                app.useIf(isProd, Middlewares.compression());""";

    // ==================== Templates Section ====================

    public static final String TEMPLATES_INTERFACE = """
                public interface Template extends Element {
                    Element render();
                }""";

    public static final String TEMPLATES_CARD = """
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
                }""";

    public static final String TEMPLATES_USAGE = """
                // In another component or page
                div(class_("container"),
                    new Card("Welcome", "Hello World!"),
                    new Card("Features", "Build apps in pure Java"),
                    new Card("Learn More", "Read the documentation")
                )""";

    public static final String TEMPLATES_COMPOSITION = """
                public class CardGrid implements Template {
                    private final List<Card> cards;
                
                    public CardGrid(List<Card> cards) {
                        this.cards = cards;
                    }
                
                    @Override
                    public Element render() {
                        return div(attrs().style()
                                .display(grid)
                                .gridTemplateColumns(repeat(3, fr(1)))
                                .gap(SP_4).done(),
                            each(cards, card -> card.render())
                        );
                    }
                }""";

    public static final String TEMPLATES_LAYOUT = """
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
                                main(attrs().style().flex(num(1)).done(),
                                    content),
                                new Footer().render()
                            )
                        );
                    }
                }""";

    public static final String TEMPLATES_CONDITIONAL = """
            // Show element only if condition is true
            when(isLoggedIn, () ->
                span("Welcome, " + userName)
            )
            
            // Choose between two elements
            ifElse(isAdmin,
                () -> a("/admin", "Admin Panel"),
                () -> a("/dashboard", "Dashboard")
            )""";

    public static final String TEMPLATES_LIST = """
List<User> users = getUsers();

ul(each(users, user ->
    li(
        strong(user.getName()),
        span(" - " + user.getEmail())
    )
))""";

    public static final String TEMPLATES_ORGANIZATION = """
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
    └── Modal.java""";

    // ==================== Styling Section ====================

    public static final String STYLING_INLINE = """
div(attrs().style()
    .display(flex)
    .padding(rem(2))
    .backgroundColor(hex("#f5f5f5"))
    .borderRadius(px(8))
.done(),
    text("Styled content")
)""";

    public static final String STYLING_UNITS = """
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
none            // none""";

    public static final String STYLING_COLORS = """
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
currentColor""";

    public static final String STYLING_LAYOUT = """
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
    .gridTemplateColumns(repeat(3, fr(1)))
    .gap(SP_4)
.done())""";

    public static final String STYLING_VALUES = """
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
ease, easeIn, easeOut, linear""";

    public static final String STYLING_THEME = """
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
.done())""";

    public static final String STYLING_TRANSITIONS = """
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
.transform(scale(1.1))""";

    public static final String STYLING_GRADIENTS = """
// Linear gradient
.background(linearGradient(red, blue))
.background(linearGradient("to right", hex("#6366f1"), hex("#8b5cf6")))

// Radial gradient
.background(radialGradient(white, black))

// Conic gradient
.background(conicGradient(red, yellow, green, blue, red))""";

    public static final String STYLING_CUSTOM_PROPS = """
// Reference a CSS variable
.color(var("primary-color"))
.color(var("text-color", black)) // with fallback""";

    // ==================== State Section ====================

    public static final String STATE_CREATE = """
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
}""";

    public static final String STATE_READ = """
State<String> name = useState("John");

// Read the value
String currentName = name.get();

// Use in render
p("Hello, " + name.get())""";

    public static final String STATE_UPDATE = """
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
});""";

    public static final String STATE_SUBSCRIBE = """
State<String> searchTerm = useState("");

// Subscribe to changes
searchTerm.subscribe(newValue -> {
    System.out.println("Search changed to: " + newValue);
    // Trigger side effects, API calls, etc.
});

// Unsubscribe when done
searchTerm.unsubscribe(subscriber);""";

    public static final String STATE_FORMS = """
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
}""";

    public static final String STATE_PATTERNS = """
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
user.update(u -> u.withName("Jane")); // immutable update""";

    public static final String STATE_SERIALIZATION = """
State<Integer> count = useState(0);

// Serialize to JSON
String json = count.toJson();
// {"id": "state_1", "value": 0}

// State IDs are unique and stable
String id = count.getId();""";

    // ==================== Forms Section ====================

    public static final String FORMS_BASIC = """
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
)""";

    public static final String FORMS_ELEMENTS = """
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
input(attrs().type("hidden").name("csrf").value(token))""";

    public static final String FORMS_VALIDATION = """
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
});""";

    public static final String FORMS_VALIDATORS = """
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
.matches(field, value)   // Must match another field""";

    public static final String FORMS_CUSTOM = """
// Using check() for simple predicates
.check(s -> s.startsWith("@"), "Must start with @")

// Using custom() for complex validators
.custom(Validator.of(
    value -> !forbiddenWords.contains(value.toLowerCase()),
    field -> field + " contains forbidden words"
))""";

    public static final String FORMS_ERRORS = """
private Element showErrors(Map<String, List<String>> errors) {
    return div(class_("errors"),
        each(errors.entrySet(), entry ->
            div(class_("error"),
                strong(entry.getKey() + ": "),
                span(String.join(", ", entry.getValue()))
            )
        )
    );
}""";

    public static final String FORMS_EVENTS = """
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
    .onBlur(e -> validateField()))""";

    // ==================== Form Builder Section ====================

    public static final String FORM_BUILDER_BASIC = """
import static com.osmig.Jweb.framework.forms.Form.*;

Form.create()
    .action("/contact")
    .method("POST")
    .text("name", f -> f.label("Full Name").placeholder("John Doe").required())
    .email("email", f -> f.label("Email").required())
    .submit("Send Message")
    .build()""";

    public static final String FORM_BUILDER_TYPES = """
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
    .build()""";

    public static final String FORM_BUILDER_CONFIG = """
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
)""";

    public static final String FORM_BUILDER_SELECT = """
.select("country", s -> s
    .label("Country")
    .placeholder("Select a country")
    .required()
    .option("us", "United States")
    .option("uk", "United Kingdom")
    .option("ca", "Canada")
    .option("de", "Germany")
    .selected("us")  // Pre-selected value
)""";

    public static final String FORM_BUILDER_RADIO = """
.radio("plan", r -> r
    .label("Subscription Plan")
    .required()
    .option("basic", "Basic - $9/month")
    .option("pro", "Professional - $29/month")
    .option("enterprise", "Enterprise - $99/month")
    .selected("pro")
)""";

    public static final String FORM_BUILDER_BUTTONS = """
.submit("Create Account")     // Submit button text
.reset("Clear Form")          // Optional reset button

// Or with custom styling:
.submitButton(b -> b
    .text("Sign Up")
    .class_("btn-primary")
    .disabled()
)""";

    public static final String FORM_BUILDER_COMPLETE = """
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
    .build()""";

    // ==================== Layouts Section ====================

    public static final String LAYOUTS_IMPORT = """
import static com.osmig.Jweb.framework.layout.Layout.*;""";

    public static final String LAYOUTS_PAGE = """
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
)""";

    public static final String LAYOUTS_CONTAINERS = """
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
Layout.container(px(960), content)""";

    public static final String LAYOUTS_FLEXBOX = """
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
)""";

    public static final String LAYOUTS_GRID = """
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
)  // Cards are at least 300px, columns adjust automatically""";

    public static final String LAYOUTS_COLUMNS = """
// Equal width columns
Layout.columns(2, child1, child2)
Layout.columns(3, rem(2), child1, child2, child3)

// Custom column ratios
Layout.columns("1fr", "2fr", rem(2),
    sidebar,  // 1/3 width
    content   // 2/3 width
)""";

    public static final String LAYOUTS_SIDEBAR = """
// Left sidebar
Layout.sidebar(px(250),      // sidebar width
    div(text("Sidebar")),    // sidebar content
    div(text("Main content")) // main content
)

// Right sidebar
Layout.sidebarRight(px(300),
    div(text("Main content")),
    div(text("Sidebar"))
)""";

    public static final String LAYOUTS_STACK = """
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
)""";

    public static final String LAYOUTS_SPECIAL = """
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
Layout.space(rem(2))       // fixed space""";

    // ==================== UI Components Section ====================

    public static final String UI_IMPORT = """
import static com.osmig.Jweb.framework.ui.UI.*;""";

    public static final String UI_BUTTONS = """
// Button variants
UI.primaryButton("Submit", e -> handleSubmit())
UI.secondaryButton("Cancel", e -> handleCancel())
UI.dangerButton("Delete", e -> handleDelete())
UI.ghostButton("Learn More", e -> navigate())
UI.linkButton("View Details", e -> showDetails())

// Icon button (for icons)
UI.iconButton(iconElement, e -> handleClick())""";

    public static final String UI_BADGES = """
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
UI.tag("Tutorial", SECONDARY)""";

    public static final String UI_ALERTS = """
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
UI.infoAlert("New features available")""";

    public static final String UI_CARDS = """
// Basic card
UI.card(
    h3("Card Title"),
    p("Card content goes here..."),
    UI.primaryButton("Action", e -> {})
)

// Card is a styled container
// Includes padding, border-radius, and subtle shadow""";

    public static final String UI_AVATARS = """
// Text avatar (shows initials)
UI.avatar("John Doe")    // Shows "JD"
UI.avatar("Alice")       // Shows "A"

// Image avatar
UI.avatarImage("/images/user.jpg", "John Doe")""";

    public static final String UI_LOADING = """
// Progress bar (0-100)
UI.progressBar(75)  // 75% complete

// Spinning loader
UI.spinner()

// Skeleton loaders (content placeholders)
UI.skeleton(px(200), px(20))     // rectangle
UI.skeletonText()                 // text line
UI.skeletonCircle(px(48))        // circle (for avatar)""";

    public static final String UI_TYPOGRAPHY = """
// Inline code
p(text("Use the "), UI.inlineCode("npm install"), text(" command"))

// Keyboard shortcut
p(text("Press "), UI.kbd("Ctrl"), text(" + "), UI.kbd("S"), text(" to save"))

// Code block with syntax highlighting colors
UI.codeBlock(\"\"\"
function hello() {
    console.log("Hello, World!");
}
\"\"\")""";

    public static final String UI_BREADCRUMB = """
UI.breadcrumb(
    a(attrs().href("/"), text("Home")),
    a(attrs().href("/docs"), text("Documentation")),
    span(text("Current Page"))
)""";

    public static final String UI_EMPTY = """
UI.emptyState(
    "No Results Found",
    "Try adjusting your search or filters"
)""";

    public static final String UI_DIVIDERS = """
// Horizontal divider
UI.divider()

// With custom margin
div(
    content1,
    UI.divider(),
    content2
)""";

    public static final String UI_COMPLETE = """
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
)""";

    // ==================== DSL Reference Section ====================

    public static final String DSL_ELEMENTS = """
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
ifElse(cond, t, f)   // Conditional with else""";

    public static final String DSL_ATTRIBUTES = """
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
    .onKeyDown(e -> ...)""";

    public static final String DSL_STYLES = """
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
.done()""";

    public static final String DSL_ROUTING = """
JWeb app = JWeb.create();

app.get(path, () -> element)    // Simple GET
app.get(path, req -> element)   // GET with request
app.post(path, req -> result)   // POST
app.put(path, req -> result)    // PUT
app.delete(path, req -> result) // DELETE

// Middleware
app.use(middleware)             // Global
app.use(path, middleware)       // Path-specific
app.useIf(cond, middleware)     // Conditional""";

    public static final String DSL_REQUEST = """
req.param("id")           // Path parameter
req.query("page")         // Query parameter
req.query("page", "1")    // With default
req.body("email")         // Form field
req.formData()            // All form data as Map
req.header("Accept")      // Request header
req.cookie("session")     // Cookie value
req.method()              // HTTP method
req.path()                // Request path""";

    public static final String DSL_STATE = """
State<T> state = useState(initialValue);

state.get()               // Read value
state.set(newValue)       // Set value
state.update(v -> v + 1)  // Transform value
state.subscribe(callback) // Listen for changes
state.unsubscribe(cb)     // Remove listener
state.getId()             // Unique state ID
state.toJson()            // JSON serialization""";

    // ==================== API Section ====================

    public static final String API_CONTROLLER = """
@REST("/api/users")
public class UserApi {
    @GET
    public List<User> getAll() {
        return userService.findAll();
    }

    @GET("/:id")
    public User getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @POST
    public User create(@RequestBody User user) {
        return userService.save(user);
    }

    @DEL("/:id")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}""";

    public static final String API_ANNOTATIONS = """
// JWeb simplified annotations
@REST("/api")      // Marks REST controller with base path
@GET               // GET request (list all)
@GET("/:id")       // GET with path variable
@POST              // POST request (create)
@UPDATE("/:id")    // PUT request (update)
@DEL("/:id")       // DELETE request

// These map to Spring's @RestController, @GetMapping, etc.""";

    public static final String API_JSON_RESPONSE = """
@GET("/status")
public Map<String, Object> status() {
    return Map.of(
        "status", "healthy",
        "timestamp", Instant.now()
    );
}
// Response: {"status":"healthy","timestamp":"..."}""";

    public static final String API_REQUEST_BODY = """
@POST
public User create(@RequestBody User user) {
    return userService.save(user);
}

@GET("/:category/:id")
public Item getItem(
    @PathVariable String category,
    @PathVariable Long id) {
    return itemService.find(category, id);
}""";

    public static final String API_OPENAPI_CONFIG = """
# application.yaml
jweb:
  api:
    base: /api/v1

# Access documentation at /api-docs""";

    // ==================== Security Section ====================

    public static final String SECURITY_PASSWORD = """
import com.osmig.Jweb.framework.security.Auth;

// Hash a password
String hashed = Auth.hashPassword("user-password");

// Verify a password
boolean valid = Auth.verifyPassword("input", hashed);""";

    public static final String SECURITY_JWT = """
// Generate JWT token
String token = Auth.generateToken(userId, Map.of(
    "role", "admin"
));

// Validate token
Optional<Auth.TokenClaims> claims = Auth.validateToken(token);
claims.ifPresent(c -> {
    String subject = c.subject();
    String role = c.get("role");
});""";

    public static final String SECURITY_SESSION = """
// Store in session
Session.set(request, "userId", user.getId());

// Retrieve from session
Long userId = Session.get(request, "userId", Long.class);

// Clear session (logout)
Session.clear(request);""";

    public static final String SECURITY_PROTECTED = """
// Protect routes with middleware
app.use("/admin", req -> {
    if (!Session.has(req, "userId")) {
        return Response.redirect("/login");
    }
    return null; // Continue to route
});""";

    public static final String SECURITY_CSRF = """
String csrfToken = Auth.generateCsrfToken(session);

form(attrs().action("/submit").method("POST"),
    input(attrs().type("hidden").name("_csrf").value(csrfToken)),
    // ... form fields
    button(type("submit"), text("Submit"))
)""";

    // ==================== UI Components Section ====================

    public static final String UI_MODAL = """
UI.modal("confirm-modal")
    .title("Confirm Action")
    .body(p("Are you sure?"))
    .footer(
        UI.secondaryButton("Cancel", e -> {}),
        UI.dangerButton("Delete", e -> deleteItem())
    )
    .build()

// Trigger: UI.modalTrigger("confirm-modal", "Delete")
// Script: UI.modalScript()""";

    public static final String UI_TABS = """
UI.tabs("settings-tabs")
    .tab("general", "General", generalContent)
    .tab("security", "Security", securityContent)
    .defaultTab("general")
    .build()

// Include once: UI.tabsScript()""";

    public static final String UI_DROPDOWN = """
UI.dropdown("user-menu")
    .trigger(div(UI.avatar("John"), span("John")))
    .item("Profile", "/profile")
    .item("Settings", "/settings")
    .divider()
    .item("Logout", e -> logout())
    .build()""";

    public static final String UI_ACCORDION = """
UI.accordion("faq")
    .item("q1", "What is JWeb?", p("A Java web framework."))
    .item("q2", "Do I need Node?", p("No, just Maven."))
    .allowMultiple(false)
    .build()""";

    public static final String UI_TOAST = """
// Setup (once in layout)
Toast.setup()

// Show from JavaScript
button(attrs().onClick("Toast.success('Saved!')"), text("Save"))
button(attrs().onClick("Toast.error('Failed')"), text("Delete"))

// Show on page load
Toast.initial(Toast.Type.SUCCESS, "Welcome!")""";

    public static final String UI_DATATABLE = """
UI.dataTable(users)
    .column("Name", User::getName)
    .column("Email", User::getEmail)
    .column("Role", u -> UI.badge(u.getRole(), Badge.INFO))
    .striped()
    .hoverable()
    .build()""";

    public static final String UI_NAVBAR = """
UI.navbar()
    .brand("/", "MyApp")
    .link("/", "Home")
    .link("/docs", "Docs")
    .right(UI.primaryButton("Sign In", e -> {}))
    .build()""";

    // ==================== Data Section ====================

    public static final String DATA_ENTITY = """
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    // getters and setters
}""";

    public static final String DATA_REPOSITORY = """
public interface UserRepository extends JpaRepository<User, Long> {
    // Built-in: findAll(), findById(), save(), delete()

    // Auto-implemented by name
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
}""";

    public static final String DATA_USAGE = """
@Component
public class Routes implements JWebRoutes {
    private final UserRepository userRepo;

    public Routes(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public void configure(JWeb app) {
        app.get("/users", req -> {
            List<User> users = userRepo.findAll();
            return new UsersPage(users).render();
        });
    }
}""";

    public static final String DATA_CONFIG = """
# application.yaml
spring:
  datasource:
    url: jdbc:h2:mem:devdb
  jpa:
    hibernate:
      ddl-auto: update
  h2:
    console:
      enabled: true""";

    // ==================== DevTools Section ====================

    public static final String DEV_CONFIG = """
# application.yaml
jweb:
  dev:
    hot-reload: true
    watch-paths: src/main/java,src/main/resources""";

    public static final String DEV_LAYOUT = """
import com.osmig.Jweb.framework.dev.DevServer;

public class Layout implements Template {
    public Element render() {
        return html(
            head(title("My App")),
            body(
                content,
                DevServer.script()  // Hot reload script
            )
        );
    }
}""";

    public static final String DEV_DEVTOOLS_POM = """
<!-- pom.xml - Add for full hot reload -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>""";
}
