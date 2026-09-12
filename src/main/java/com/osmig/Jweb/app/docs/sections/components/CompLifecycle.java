package com.osmig.Jweb.app.docs.sections.components;

import jweb.Action;
import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CompLifecycle {
    private CompLifecycle() {}

    public static Element render() {
        return section(
            h3Title("Lifecycle Hooks"),
            para("Templates support lifecycle hooks for data loading, cleanup, and page metadata."),

            h3Title("beforeRender & afterRender"),
            para("Server-side hooks for data loading and cleanup."),
            codeBlock("""
record User(String getName) {}
class UserService { User findById(int id) { return new User("Ada"); } }

public class UserPage implements Template {
    private final UserService userService;
    private User user;

    public UserPage(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void beforeRender(Request request) {
        int userId = request.paramInt("id");
        this.user = userService.findById(userId);
    }

    @Override
    public void afterRender(Request request) {
        // Cleanup, logging, analytics
    }

    @Override
    public Element render() {
        return div(h1(user.getName()));
    }
}"""),

            h3Title("Page Title & Meta"),
            para("Dynamic page title and SEO metadata — plain Strings, null for the default."),
            codeBlock("""
record Product(String getName, String getDescription) {}
private Product product = new Product("Widget",
    "A very fine widget for all your widget needs. Built to last, designed " +
    "to delight, and priced to make your accountant smile every single quarter, " +
    "no matter how the market moves or the weather turns outside.");

@Override
public String pageTitle() {
    return product.getName() + " | Store";
}

@Override
public String description() {
    return product.getDescription().substring(0, 150);
}"""),
            before("v3.0.0",
                para("Before 3.0 these returned Optional<String> (pageTitle() / metaDescription()); " +
                     "an Optional metaDescription() override still feeds description().")),

            h3Title("Extra Head Elements"),
            para("Add custom elements to the HTML head."),
            codeBlock("""
private String getTitle() { return "Home"; }
private String getImageUrl() { return "/og-image.png"; }

@Override
public Optional<Element> extraHead() {
    return Optional.of(fragment(
        meta("og:title", getTitle()),
        meta("og:image", getImageUrl()),
        link(attrs().rel("preconnect").href("https://fonts.googleapis.com")),
        css("/css/page.css")
    ));
}"""),

            h3Title("Client-Side Lifecycle"),
            para("Actions to run on DOM ready and cleanup."),
            codeBlock("""
@Override
public Action onMount() {
    return all(call("initCharts"), call("setupWebSocket"));
}

@Override
public Action onUnmount() {
    return all(call("closeWebSocket"), call("saveScrollPosition"));
}"""),

            h3Title("Caching"),
            para("Control response caching for performance."),
            codeBlock("""
private Object currentUser = null;

@Override
public boolean cacheable() {
    return currentUser == null;  // Only cache for anonymous users
}

@Override
public int cacheDuration() {
    return 3600;  // Cache for 1 hour
}"""),

            docTip("Use beforeRender for data loading, not render(). This keeps render() pure.")
        );
    }
}
