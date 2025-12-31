# Templates & Components

Templates are the foundation of JWeb's component system.

## Basic Template

```java
import com.osmig.Jweb.framework.template.Template;
import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.framework.elements.Elements.*;

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
            h3(title),
            p(content)
        );
    }
}

// Usage
div(class_("container"),
    new Card("Welcome", "Hello World!"),
    new Card("About", "Learn more...")
)
```

---

## Level 1: Simple Components

```java
// Stateless component
public class Badge implements Template {
    private final String text;

    public Badge(String text) {
        this.text = text;
    }

    @Override
    public Element render() {
        return span(class_("badge"), text);
    }
}

// Usage
div(
    new Badge("New"),
    new Badge("Featured")
)
```

---

## Level 2: Components with Children

```java
public class Card implements Template {
    private final String title;
    private final Element[] children;

    public Card(String title, Element... children) {
        this.title = title;
        this.children = children;
    }

    @Override
    public Element render() {
        return div(class_("card"),
            h3(class_("card-title"), title),
            div(class_("card-body"),
                fragment(children)
            )
        );
    }
}

// Usage
new Card("User Profile",
    p("Name: John Doe"),
    p("Email: john@example.com"),
    button("Edit Profile")
)
```

---

## Level 3: Factory Methods

```java
public class Alert implements Template {
    private final String message;
    private final String type;

    private Alert(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public static Alert success(String message) {
        return new Alert(message, "success");
    }

    public static Alert error(String message) {
        return new Alert(message, "error");
    }

    public static Alert warning(String message) {
        return new Alert(message, "warning");
    }

    @Override
    public Element render() {
        return div(class_("alert alert-" + type),
            span(message)
        );
    }
}

// Usage
Alert.success("Operation completed!")
Alert.error("Something went wrong")
```

---

## Level 4: Lifecycle Hooks

Templates support lifecycle hooks for data loading and setup:

```java
public class UserPage implements Template {
    private final UserService userService;
    private User user;
    private List<Post> posts;

    public UserPage(UserService userService) {
        this.userService = userService;
    }

    // Called BEFORE render() - load data here
    @Override
    public void beforeRender(Request request) {
        int userId = request.requireParamInt("id");
        this.user = userService.findById(userId);
        this.posts = userService.getPostsForUser(userId);
    }

    // Called AFTER render() - cleanup here
    @Override
    public void afterRender(Request request) {
        // Optional: logging, cleanup, etc.
    }

    @Override
    public Element render() {
        return div(class_("user-profile"),
            h1(user.getName()),
            p(user.getBio()),
            div(class_("posts"),
                each(posts, post -> new PostCard(post))
            )
        );
    }
}
```

---

## Level 5: Page Title & Meta

```java
public class ProductPage implements Template {
    private Product product;

    @Override
    public void beforeRender(Request request) {
        product = productService.findById(request.requireParamInt("id"));
    }

    // Dynamic page title
    @Override
    public Optional<String> pageTitle() {
        return Optional.of(product.getName() + " | MyStore");
    }

    // SEO meta description
    @Override
    public Optional<String> metaDescription() {
        return Optional.of(product.getDescription().substring(0, 150));
    }

    @Override
    public Element render() {
        return div(class_("product"),
            h1(product.getName()),
            p(product.getDescription())
        );
    }
}
```

---

## Level 6: Extra Head Elements

```java
public class AnalyticsPage implements Template {

    // Add custom elements to <head>
    @Override
    public Optional<Element> extraHead() {
        return Optional.of(fragment(
            // Open Graph tags
            meta(name("og:title"), content(getTitle())),
            meta(name("og:image"), content(getImageUrl())),
            meta(name("og:description"), content(getDescription())),

            // Preconnect for performance
            link(rel("preconnect"), href("https://fonts.googleapis.com")),

            // Page-specific CSS
            link(rel("stylesheet"), href("/css/analytics.css")),

            // Inline critical CSS
            style(criticalCss())
        ));
    }

    @Override
    public Element render() {
        return div(class_("analytics-dashboard"),
            // Dashboard content...
        );
    }
}
```

---

## Level 7: Page Scripts

```java
public class InteractivePage implements Template {

    // Inline scripts at end of body
    @Override
    public Optional<String> scripts() {
        return Optional.of(
            script()
                .withHelpers()
                .add(onClick("toggle-btn").then(toggle("panel")))
                .add(onSubmit("form")
                    .post("/api/submit")
                    .ok(showMessage("status").success("Saved!")))
                .build()
        );
    }

    @Override
    public Element render() {
        return div(
            button(id("toggle-btn"), "Toggle Panel"),
            div(id("panel"), "Panel content..."),
            form(id("form"),
                // form fields...
            ),
            div(id("status"))
        );
    }
}
```

---

## Level 8: Client-Side Lifecycle

```java
public class DashboardPage implements Template {

    // JavaScript to run when DOM is ready
    @Override
    public String onMount() {
        return """
            initCharts();
            setupWebSocket();
            startAutoRefresh();
            """;
    }

    // JavaScript for cleanup (page unload/SPA navigation)
    @Override
    public String onUnmount() {
        return """
            stopAutoRefresh();
            closeWebSocket();
            saveScrollPosition();
            """;
    }

    @Override
    public Element render() {
        return div(class_("dashboard"),
            div(id("charts")),
            div(id("live-data"))
        );
    }
}
```

---

## Level 9: Caching

```java
public class StaticPage implements Template {

    // Enable/disable caching
    @Override
    public boolean cacheable() {
        return true;  // This page can be cached
    }

    // Cache duration in seconds
    @Override
    public int cacheDuration() {
        return 3600;  // Cache for 1 hour
    }

    @Override
    public Element render() {
        return div(class_("static-content"),
            h1("About Us"),
            p("Static content that rarely changes...")
        );
    }
}

// Dynamic page - no caching
public class UserDashboard implements Template {

    @Override
    public boolean cacheable() {
        return false;  // Never cache user-specific content
    }

    @Override
    public Element render() {
        return div(
            h1("Welcome, " + user.getName())
        );
    }
}
```

---

## Level 10: Full Page Template

```java
public class BlogPostPage implements Template {
    private final BlogService blogService;
    private BlogPost post;
    private List<Comment> comments;
    private User currentUser;

    public BlogPostPage(BlogService blogService) {
        this.blogService = blogService;
    }

    @Override
    public void beforeRender(Request request) {
        String slug = request.requireParam("slug");
        post = blogService.findBySlug(slug);
        comments = blogService.getComments(post.getId());
        currentUser = Auth.isAuthenticated(request)
            ? Auth.getPrincipal(request).as(User.class)
            : null;
    }

    @Override
    public void afterRender(Request request) {
        blogService.incrementViewCount(post.getId());
    }

    @Override
    public Optional<String> pageTitle() {
        return Optional.of(post.getTitle() + " | My Blog");
    }

    @Override
    public Optional<String> metaDescription() {
        return Optional.of(post.getExcerpt());
    }

    @Override
    public Optional<Element> extraHead() {
        return Optional.of(fragment(
            meta(name("og:title"), content(post.getTitle())),
            meta(name("og:image"), content(post.getCoverImage())),
            meta(name("article:published_time"), content(post.getPublishedAt().toString())),
            link(rel("canonical"), href("https://myblog.com/posts/" + post.getSlug()))
        ));
    }

    @Override
    public String onMount() {
        return "initSyntaxHighlighting(); setupCommentForm();";
    }

    @Override
    public Optional<String> scripts() {
        return Optional.of(
            script()
                .add(onSubmit("comment-form")
                    .loading("Posting...")
                    .post("/api/posts/" + post.getId() + "/comments")
                    .ok(all(
                        resetForm("comment-form"),
                        call("refreshComments")
                    ))
                    .fail(responseError("comment-error")))
                .build()
        );
    }

    @Override
    public boolean cacheable() {
        return currentUser == null;  // Only cache for anonymous users
    }

    @Override
    public int cacheDuration() {
        return 300;  // 5 minutes
    }

    @Override
    public Element render() {
        return article(class_("blog-post"),
            header(
                h1(post.getTitle()),
                div(class_("meta"),
                    span("By " + post.getAuthor().getName()),
                    span(formatDate(post.getPublishedAt()))
                )
            ),
            div(class_("content"),
                raw(post.getHtmlContent())
            ),
            section(class_("comments"),
                h2("Comments (" + comments.size() + ")"),
                each(comments, this::renderComment),
                when(currentUser != null, this::renderCommentForm)
            )
        );
    }

    private Element renderComment(Comment comment) {
        return div(class_("comment"),
            div(class_("comment-header"),
                strong(comment.getAuthor().getName()),
                span(class_("date"), formatDate(comment.getCreatedAt()))
            ),
            p(comment.getContent())
        );
    }

    private Element renderCommentForm() {
        return form(id("comment-form"),
            div(id("comment-error"), class_("error")),
            textarea(name("content"), placeholder("Write a comment..."), required()),
            button(type("submit"), "Post Comment")
        );
    }
}
```

---

## Level 11: Layouts

```java
public class MainLayout implements Template {
    private final String title;
    private final Element content;

    public MainLayout(String title, Element content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Element render() {
        return html(
            head(
                meta(attr("charset", "UTF-8")),
                meta(name("viewport"), content("width=device-width, initial-scale=1")),
                title(title),
                link(rel("stylesheet"), href("/css/main.css"))
            ),
            body(
                header(class_("site-header"),
                    nav(
                        a(href("/"), "Home"),
                        a(href("/about"), "About"),
                        a(href("/contact"), "Contact")
                    )
                ),
                main(class_("content"),
                    content
                ),
                footer(class_("site-footer"),
                    p("Copyright 2024")
                )
            )
        );
    }
}

// Usage
public class HomePage implements Template {
    @Override
    public Element render() {
        return new MainLayout("Home",
            div(class_("hero"),
                h1("Welcome to Our Site"),
                p("Build amazing things with JWeb")
            )
        );
    }
}
```

---

## Level 12: Partials

Small reusable components:

```java
// partials/Nav.java
public class Nav implements Template {
    private final String currentPath;

    public Nav(String currentPath) {
        this.currentPath = currentPath;
    }

    @Override
    public Element render() {
        return nav(class_("main-nav"),
            navLink("/", "Home"),
            navLink("/products", "Products"),
            navLink("/about", "About"),
            navLink("/contact", "Contact")
        );
    }

    private Element navLink(String path, String label) {
        boolean active = currentPath.equals(path);
        return a(
            href(path),
            class_("nav-link"),
            class_("active", active),
            label
        );
    }
}

// partials/Pagination.java
public class Pagination implements Template {
    private final int currentPage;
    private final int totalPages;
    private final String baseUrl;

    public Pagination(int currentPage, int totalPages, String baseUrl) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.baseUrl = baseUrl;
    }

    @Override
    public Element render() {
        return div(class_("pagination"),
            when(currentPage > 1, () ->
                a(href(baseUrl + "?page=" + (currentPage - 1)), "Previous")
            ),
            span(currentPage + " of " + totalPages),
            when(currentPage < totalPages, () ->
                a(href(baseUrl + "?page=" + (currentPage + 1)), "Next")
            )
        );
    }
}
```

---

## Lifecycle Hook Summary

| Hook | When Called | Use For |
|------|-------------|---------|
| `beforeRender(Request)` | Before `render()` | Data loading, validation, setup |
| `afterRender(Request)` | After `render()` | Cleanup, logging, analytics |
| `onMount()` | DOM ready (client) | Initialize JS, bind events |
| `onUnmount()` | Page leave (client) | Cleanup timers, save state |
| `pageTitle()` | Head generation | Set `<title>` tag |
| `metaDescription()` | Head generation | SEO meta tags |
| `extraHead()` | Head generation | Custom meta, CSS, scripts |
| `scripts()` | Body end | Page-specific JavaScript |
| `cacheable()` | Response headers | Enable/disable caching |
| `cacheDuration()` | Response headers | Cache-Control max-age |

---

## Best Practices

### 1. Keep Templates Focused

```java
// Good: Single responsibility
public class ProductCard implements Template { ... }
public class ProductList implements Template { ... }
public class ProductPage implements Template { ... }

// Bad: Too much in one template
public class ProductEverything implements Template { ... }
```

### 2. Extract Reusable Parts

```java
// Extract common UI patterns
public class FormField implements Template {
    private final String label;
    private final Element input;
    private final String error;

    // ...
}

// Use in multiple forms
new FormField("Email", emailInput("email"), errors.get("email"))
new FormField("Password", passwordInput("password"), errors.get("password"))
```

### 3. Use beforeRender for Data

```java
// Good: Load data in beforeRender
@Override
public void beforeRender(Request request) {
    this.user = userService.find(request.paramInt("id"));
}

// Bad: Load data in render
@Override
public Element render() {
    User user = userService.find(...);  // Don't do this
}
```

### 4. Keep Files Under 100 Lines

Split large templates into smaller components per STANDARD.md.
