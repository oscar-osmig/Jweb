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
                    style()
                        .padding(rem(2))
                        .backgroundColor(hex("#f5f5f5"))""";

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
                    import jweb.Element;
                    import static jweb.El.*;
                    import static jweb.Css.*;""";

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
                    int page = req.queryInt("page", 1); // with default
                    return div(
                        h1("Search: " + query),
                        p("Page: " + page)
                    );
                });""";

    public static final String ROUTING_REQUEST_BODY = """
                app.post("/login", req -> {
                    // Form data
                    String email = req.formParam("email");
                    String password = req.formParam("password");

                    // Or get all form data as a Map
                    Map<String, String[]> formData = req.formParams();

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
                app.use("/admin", Auth.requireAuth("/login"));

                // Apply conditionally
                app.useIf(isProd, Middlewares.compressionHeaders());""";

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
                        return div(class_("card"), style()
                                .padding(SP_4).backgroundColor(hex("#f8fafc"))
                                .borderRadius(ROUNDED),
                            h3(style().fontSize(TEXT_LG).fontWeight(600),
                                title),
                            p(style().color(TEXT_LIGHT).marginTop(SP_2),
                                content)
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
                        return div(style()
                                .display(grid)
                                .gridTemplateColumns(repeat(3, fr(1)))
                                .gap(SP_4),
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
                            body(style()
                                    .display(flex).flexDirection(column)
                                    .minHeight(vh(100)),
                                new Nav(),
                                main(style().flex(1),
                                    content),
                                new Footer()
                            )
                        );
                    }
                }""";

    public static final String TEMPLATES_CONDITIONAL = """
            // Show element only if condition is true
            when(isLoggedIn, () ->
                span("Welcome, " + userName)
            )

            // Choose between two elements — a Java ternary
            isAdmin
                ? a(href("/admin"), "Admin Panel")
                : a(href("/dashboard"), "Dashboard")""";

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
div(style()
    .display(flex)
    .padding(rem(2))
    .backgroundColor(hex("#f5f5f5"))
    .borderRadius(px(8)),
    "Styled content"
)""";

    public static final String STYLING_UNITS = """
import static jweb.Css.*;

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
import static jweb.Css.*;

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
div(style()
    .display(flex)
    .flexDirection(column)
    .justifyContent(center)
    .alignItems(center)
    .gap(rem(1)))

// Grid
div(style()
    .display(grid)
    .gridTemplateColumns(repeat(3, fr(1)))
    .gap(SP_4))""";

    public static final String STYLING_VALUES = """
import static jweb.Css.*;

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

div(style()
    .color(PRIMARY)
    .padding(SP_4)
    .fontSize(TEXT_LG))""";

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
import static jweb.State.*;

public class Counter implements Template {
    private final State<Integer> count = useState(0);

    @Override
    public Element render() {
        return div(
            h1("Count: " + count.get()),
            button(onClick(e -> count.set(count.get() + 1)),
                "Increment")
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
        return form(onSubmit(this::handleSubmit),
            input(type("email"), value(email.get()),
                onInput(e -> email.set(e.value()))),
            input(type("password"), value(password.get()),
                onInput(e -> password.set(e.value()))),
            button(type("submit"), "Login")
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
button(onClick(e -> isOpen.update(open -> !open)),
    isOpen.get() ? "Close" : "Open")

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
form(action("/submit"), method("POST"),
    div(class_("form-group"),
        label(for_("email"), "Email"),
        input(type("email"), id("email"), name("email"),
            placeholder("you@example.com"), required())
    ),
    div(class_("form-group"),
        label(for_("password"), "Password"),
        input(type("password"), id("password"), name("password"), required())
    ),
    button(type("submit"), "Sign In")
)""";

    public static final String FORMS_ELEMENTS = """
// Text inputs
input(type("text"), name("username"))
input(type("email"), name("email"))
input(type("password"), name("password"))
input(type("number"), name("age"))

// Textarea
textarea(name("bio"), placeholder("Tell us about yourself"))

// Select dropdown
select(name("country"),
    option(value("us"), "United States"),
    option(value("uk"), "United Kingdom"),
    option(value("ca"), "Canada")
)

// Checkboxes and radios
input(type("checkbox"), name("agree"), checked())
input(type("radio"), name("plan"), value("basic"))

// File upload
input(type("file"), name("avatar"))

// Hidden fields
input(type("hidden"), name("csrf"), value(token))""";

    public static final String FORMS_VALIDATION = """
import jweb.FormValidator;

app.post("/register", req -> {
    var result = FormValidator.create()
        .field("email", req.formParam("email"))
            .required()
            .email()
        .field("password", req.formParam("password"))
            .required()
            .minLength(8)
            .maxLength(100)
        .field("confirmPassword", req.formParam("confirmPassword"))
            .required()
            .matches("password", req.formParam("password"))
        .field("age", req.formParam("age"))
            .optional()
            .numeric()
        .validate();

    if (result.hasErrors()) {
        return showErrors(result.getAllErrors());
    }

    return createUser(req);
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
form(onSubmit(e -> {
    e.preventDefault();
    submitForm();
}))

// Input change
input(onChange(e -> {
    String value = e.value();
    updateState(value);
}))

// Real-time input
input(onInput(e -> {
    searchTerm.set(e.value());
}))

// Focus/blur
input(onFocus(e -> showHint()), onBlur(e -> validateField()))""";

    // ==================== Form Builder Section ====================
    // A form is a record: one class declares the fields, their types and their
    // rules; the same record renders the form, validates the submission, and
    // comes back as a typed value. There is no separate builder chain.

    public static final String FORM_BUILDER_BASIC = """
public record ContactForm(
    @Form.Required String name,
    @Form.Required @Form.Email String email) {}

form(ContactForm.class)
    .action("/contact")
    .submit("Send Message")""";

    public static final String FORM_BUILDER_TYPES = """
// The Java type of each record component picks the control
public record Signup(
    String username,                       // <input type="text">
    @Form.Email String email,              // <input type="email">
    @Form.Password String password,        // <input type="password">
    @Form.Multiline(rows = 4) String bio,   // <textarea>
    int age,                                // <input type="number">
    boolean subscribe,                      // <input type="checkbox">
    Plan plan,                              // <select> over the enum's constants
    LocalDate birthdate,                    // <input type="date">
    LocalTime meetingTime,                  // <input type="time">
    LocalDateTime eventAt,                  // <input type="datetime-local">
    UploadedFile resume) {}                 // <input type="file">, form goes multipart

enum Plan { FREE, PRO, ENTERPRISE }

form(Signup.class).action("/signup").submit("Sign Up")""";

    public static final String FORM_BUILDER_CONFIG = """
// field(name, f -> ...) is presentational only — label, placeholder, help
// text, control type, rows, accept, autocomplete, options. The rules
// (required, email, length) stay on the record's annotations.
form(Signup.class)
    .field("username", f -> f
        .label("Username")             // overrides the derived label
        .placeholder("johndoe")        // placeholder text
        .help("3-20 characters")       // help text under the control
        .autocomplete("username"))
    .field("bio", f -> f.rows(6))                 // more textarea rows
    .field("resume", f -> f.accept(".pdf,.doc"))  // file input's accept
    .submit("Create Account")""";

    public static final String FORM_BUILDER_SELECT = """
public enum Country { US, UK, CA, DE }
public record Address(Country country) {}

// An enum component renders as a <select> over its constants automatically
form(Address.class).submit("Save")

// options(...) replaces the choices shown — relabels an enum, or supplies
// the choices for a plain String component
form(Address.class)
    .field("country", f -> f.options("United States", "United Kingdom", "Canada", "Germany"))
    .submit("Save")""";

    public static final String FORM_BUILDER_RADIO = """
// There is no separate radio-group builder — a fixed set of choices is
// an enum component, which renders as a <select>:
public enum Plan { FREE, PRO, ENTERPRISE }
public record Signup(Plan plan) {}

form(Signup.class).submit("Sign Up")

// A native radio-button group is plain elements, same as any other
// hand-written field:
fieldset(
    legend("Plan"),
    label(input(type("radio"), name("plan"), value("free"), checked()), " Free"),
    label(input(type("radio"), name("plan"), value("pro")), " Pro - $9/month"),
    label(input(type("radio"), name("plan"), value("enterprise")), " Enterprise - $99/month")
)""";

    public static final String FORM_BUILDER_BUTTONS = """
form(Signup.class)
    .submit("Create Account")   // the submit button's text

// There is no reset() or a per-button config lambda — style the emitted
// .jweb-submit class instead, or skip Form.styles() and write your own CSS.""";

    public static final String FORM_BUILDER_COMPLETE = """
public record Registration(
    @Form.Required String firstName,
    @Form.Required String lastName,
    @Form.Required @Form.Email String email,
    @Form.Required @Form.Password String password,
    Country country,
    Plan plan,
    boolean newsletter,
    @Form.Required boolean terms) {}

form(Registration.class)
    .cls("registration-form")
    .action("/register")
    .field("password", f -> f.help("At least 8 characters"))
    .field("terms", f -> f.label("I agree to the Terms of Service"))
    .submit("Create Account")

// Server side — bind, then either use the value or re-render with the errors
app.post("/register", req -> {
    Form.Bound<Registration> bound = Form.bind(Registration.class, req);
    if (!bound.ok()) {
        return form(Registration.class).action("/register").errors(bound).submit("Create Account");
    }
    accounts.create(bound.value());
    return Response.redirect("/welcome");
});""";

    // ==================== UI Components Section ====================

    public static final String UI_IMPORT = """
import static jweb.UI.*;""";

    public static final String UI_BUTTONS = """
// Button variants
UI.primaryButton("Submit", e -> handleSubmit())
UI.secondaryButton("Cancel", e -> handleCancel())
UI.dangerButton("Delete", e -> handleDelete())
UI.ghostButton("Learn More", e -> navigate())
UI.linkButton("View Details", e -> showDetails())

// Icon button (icon + label)
UI.iconButton("\\u2605", "Favorite", e -> handleClick())""";

    public static final String UI_BADGES = """
import static jweb.UI.*;

// Colored badges
UI.badge("New", Badge.SUCCESS)     // green
UI.badge("Pending", Badge.WARNING) // yellow
UI.badge("Error", Badge.ERROR)     // red
UI.badge("Info", Badge.INFO)       // blue
UI.badge("Default", Badge.DEFAULT) // gray
UI.badge("Primary", Badge.PRIMARY) // indigo

// Tags (similar to badges, for categories)
UI.tag("JavaScript")
UI.tag("Removable", e -> removeTag())  // with remove handler""";

    public static final String UI_ALERTS = """
import static jweb.UI.*;

// Alert with icon
UI.alert("Operation completed successfully!", Alert.SUCCESS)
UI.alert("Please review your input.", Alert.WARNING)
UI.alert("Something went wrong.", Alert.ERROR)
UI.alert("Here's some useful information.", Alert.INFO)

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
p("Use the ", UI.inlineCode("npm install"), " command")

// Keyboard shortcut
p("Press ", UI.kbd("Ctrl"), " + ", UI.kbd("S"), " to save")

// Code block with syntax highlighting colors
UI.codeBlock(\"\"\"
function hello() {
    console.log("Hello, World!");
}
\"\"\")""";

    public static final String UI_BREADCRUMB = """
UI.breadcrumb("Home", "Documentation", "Current Page")""";

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
import static jweb.UI.*;
import static jweb.Css.*;

UI.card(
    // Header with badge
    div(row(rem(1)),
        h3("User Statistics"),
        badge("Live", Badge.SUCCESS)
    ),

    UI.divider(),

    // Progress section
    div(stack(rem(0.5)),
        p("Storage Used"),
        progressBar(65),
        p("65% of 100GB")
    ),

    UI.divider(),

    // Actions
    div(cluster(rem(0.5)),
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
raw("<b>html</b>")   // Unescaped HTML — a bare String is always escaped text
fragment(...)        // Group without wrapper
each(list, mapper)   // List iteration
when(cond, supplier) // Conditional — Java's ternary/switch handle the rest""";

    public static final String DSL_ATTRIBUTES = """
attrs()
    // Core
    .id("main")
    .class_("container")
    .addClass("active")
    .style(s -> s.display(flex))

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
style()
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
    .transition(all, s(0.2), ease)
    .transform(scale(1.1))""";

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
req.queryInt("page", 1)   // With default
req.formParam("email")    // Form field
req.formParams()          // All form data as Map
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

    @GET("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @POST
    public User create(@RequestBody User user) {
        return userService.save(user);
    }

    @DEL("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}""";

    public static final String API_ANNOTATIONS = """
// JWeb simplified annotations
@REST("/api")      // Marks REST controller with base path
@GET               // GET request (list all)
@GET("/{id}")      // GET with path variable
@POST              // POST request (create)
@UPDATE("/{id}")   // PUT request (update)
@DEL("/{id}")      // DELETE request

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

@GET("/{category}/{id}")
public Item getItem(
    @PathVariable String category,
    @PathVariable Long id) {
    return itemService.find(category, id);
}""";

    public static final String API_OPENAPI_CONFIG = """
// Mount interactive API docs
OpenApi.create()
    .title("My API")
    .version("1.0.0")
    .addApi(UserApi.class)
    .mount(app);  // Serves /docs, /redoc, /scalar, /openapi.json""";

    // ==================== Security Section ====================

    public static final String SECURITY_PASSWORD = """
import jweb.Password;

// Hash a password (BCrypt)
String hashed = Password.hash("user-password");

// Verify a password
boolean valid = Password.verify("input", hashed);""";

    public static final String SECURITY_JWT = """
// Generate JWT token
String token = Jwt.create()
    .subject(userId)
    .claim("role", "admin")
    .expiresIn(Duration.ofHours(1))
    .sign();

// Validate and read claims
if (Jwt.isValid(token)) {
    Jwt.Token parsed = Jwt.parse(token);
    String subject = parsed.subject();
    String role = parsed.claim("role");
}""";

    public static final String SECURITY_SESSION = """
// Store in session
req.sessionAttr("userId", user.getId());

// Retrieve from session
Long userId = req.sessionAttr("userId");

// Clear session (logout)
req.session().invalidate();""";

    public static final String SECURITY_PROTECTED = """
// Protect routes with the built-in middleware
app.use("/admin", Auth.requireAuth("/login"));

// Or write the check by hand
app.use("/admin", (req, chain) -> {
    if (!Auth.isAuthenticated(req)) {
        return Response.redirect("/login");
    }
    return chain.next(); // Continue to route
});""";

    public static final String SECURITY_CSRF = """
import jweb.Csrf;

form(action("/submit"), method("POST"),
    Csrf.tokenField(req),  // hidden _csrf input
    // ... form fields
    button(type("submit"), "Submit")
)""";

    // ==================== UI Components Section ====================

    public static final String UI_MODAL = """
UI.Modal.create("confirm-modal")
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
UI.Tabs.create("settings-tabs")
    .tab("general", "General", generalContent)
    .tab("security", "Security", securityContent)
    .defaultTab("general")
    .build()

// Include once: UI.tabsScript()""";

    public static final String UI_DROPDOWN = """
UI.Dropdown.create("user-menu")
    .trigger(div(UI.avatar("John"), span("John")))
    .item("Profile", e -> goTo("/profile"))
    .item("Settings", e -> goTo("/settings"))
    .divider()
    .item("Logout", e -> logout())
    .build()""";

    public static final String UI_ACCORDION = """
UI.Accordion.create("faq")
    .item("What is JWeb?", p("A Java web framework."))
    .item("Do I need Node?", p("No, just Maven."))
    .allowMultiple(false)
    .build()""";

    public static final String UI_TOAST = """
// Setup (once in layout)
Toast.setup()

// Show from a click handler
button(onClick(Toast.success("Saved!")), "Save")
button(onClick(Toast.error("Failed")), "Delete")

// Show on page load
Toast.initial(Toast.Type.SUCCESS, "Welcome!")""";

    public static final String UI_DATATABLE = """
UI.DataTable.<User>create()
    .column("Name", User::getName)
    .column("Email", User::getEmail)
    .column("Role", u -> UI.badge(u.getRole(), Badge.INFO))
    .data(users)
    .striped()
    .hoverable()
    .build()""";

    public static final String UI_NAVBAR = """
UI.Nav.create()
    .brand("MyApp", "/")
    .link("Home", "/")
    .link("Docs", "/docs")
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
import jweb.DevServer;

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
