package com.osmig.Jweb.app.docs.sections.components;

import com.osmig.Jweb.framework.core.Element;
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
            para("Dynamic page title and SEO metadata."),
            codeBlock("""
@Override
public Optional<String> pageTitle() {
    return Optional.of(product.getName() + " | Store");
}

@Override
public Optional<String> metaDescription() {
    return Optional.of(product.getDescription().substring(0, 150));
}"""),

            h3Title("Extra Head Elements"),
            para("Add custom elements to the HTML head."),
            codeBlock("""
@Override
public Optional<Element> extraHead() {
    return Optional.of(fragment(
        meta(name("og:title"), content(getTitle())),
        meta(name("og:image"), content(getImageUrl())),
        link(rel("preconnect"), href("https://fonts.googleapis.com")),
        link(rel("stylesheet"), href("/css/page.css"))
    ));
}"""),

            h3Title("Client-Side Lifecycle"),
            para("JavaScript hooks for DOM ready and cleanup."),
            codeBlock("""
@Override
public String onMount() {
    return "initCharts(); setupWebSocket();";
}

@Override
public String onUnmount() {
    return "closeWebSocket(); saveScrollPosition();";
}"""),

            h3Title("Caching"),
            para("Control response caching for performance."),
            codeBlock("""
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
