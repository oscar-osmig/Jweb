package com.osmig.Jweb.app.docs.sections.elements;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsConditionals {
    private ElementsConditionals() {}

    public static Element render() {
        return section(
            h3Title("Conditional Rendering"),
            para("Show or hide content based on conditions."),

            h3Title("when() - Simple Conditional"),
            para("Render content only when a condition is true."),
            codeBlock("""
// Lazy evaluation (recommended for expensive operations)
when(isLoggedIn, () -> span("Welcome back!"))

// Eager evaluation (for simple values)
when(isLoggedIn, span("Welcome, " + username))

// Examples
div(
    h1("Dashboard"),
    when(isAdmin, () -> adminPanel()),
    when(hasNotifications, () -> notificationBadge(count))
)"""),

            before("v3.0.0",
                h3Title("when().then().otherwise() - If/Else"),
                para("Render different content for true and false conditions."),
                codeBlock("""
// If/else pattern
when(isLoggedIn)
    .then(userMenu())
    .otherwise(loginButton())

// In context
header(
    h1("My App"),
    nav(
        a(href("/"), "Home"),
        when(isLoggedIn)
            .then(a(href("/dashboard"), "Dashboard"))
            .otherwise(a(href("/login"), "Sign In"))
    )
)"""),

                h3Title("when().then().elif() - Multiple Conditions"),
                para("Chain multiple conditions like if/else-if/else."),
                codeBlock("""
// Multiple conditions
when(isAdmin)
    .then(adminPanel())
    .elif(isModerator, modPanel())
    .elif(isUser, userPanel())
    .otherwise(guestPanel())

// Status display
when(status.equals("success"))
    .then(successMessage())
    .elif(status.equals("warning"), warningMessage())
    .elif(status.equals("error"), errorMessage())
    .otherwise(infoMessage())

// Without fallback (renders nothing if all false)
when(showPromo)
    .then(promoBanner())
    .end()"""),

                h3Title("match() - Pattern Matching"),
                para("Match against multiple conditions in a clean syntax."),
                codeBlock("""
// Match pattern
match(
    cond(isAdmin, adminPanel()),
    cond(isModerator, modPanel()),
    cond(isUser, userPanel()),
    otherwise(loginPrompt())
)

// User type display
div(
    match(
        cond(user.isPremium(), premiumBadge()),
        cond(user.isVerified(), verifiedBadge()),
        otherwise(standardBadge())
    )
)""")),

            since("v3.0.0",
                h3Title("Either/Or"),
                para("Render different content for true and false conditions with a Java ternary."),
                codeBlock("""
// If/else pattern
isLoggedIn ? userMenu() : loginButton()

// In context
header(
    h1("My App"),
    nav(
        a(href("/"), "Home"),
        isLoggedIn
            ? a(href("/dashboard"), "Dashboard")
            : a(href("/login"), "Sign In")
    )
)"""),

                h3Title("Multi-Way Choices"),
                para("One conditional shape — when(condition, element) — covers optional " +
                     "content; Java's switch expression covers branching on a value. " +
                     "There is no separate chain or match() to learn."),
                codeBlock("""
// Multiple conditions
Element panel = switch (role) {
    case ADMIN -> adminPanel();
    case MODERATOR -> modPanel();
    case USER -> userPanel();
    default -> guestPanel();
};

// Status display
Element status = switch (statusCode) {
    case "success" -> successMessage();
    case "warning" -> warningMessage();
    case "error" -> errorMessage();
    default -> infoMessage();
};

// Without a fallback, just skip rendering when false
when(showPromo, () -> promoBanner())

// User type display — arbitrary predicates read best as a ternary chain
div(
    user.isPremium() ? premiumBadge()
        : user.isVerified() ? verifiedBadge()
        : standardBadge()
)""")),

            h3Title("Inline Ternary Conditions"),
            para("Java's native ternary operator works for simple inline conditions."),
            codeBlock("""
// Ternary pattern
span(isActive ? "Active" : "Inactive")

// With elements
div(hasError
    ? span(class_("error"), errorMessage)
    : span(class_("success"), "All good!"))

// Styling
div(class_(isDark ? "dark-theme" : "light-theme"))"""),

            h3Title("each() - Iteration"),
            para("Render a list of items."),
            codeBlock("""
// Basic iteration
List<String> items = List.of("Apple", "Banana", "Cherry");

ul(
    each(items, item -> li(item))
)

// With complex rendering
List<User> users = getUsers();

div(class_("user-list"),
    each(users, user ->
        div(class_("user-card"),
            img(user.getAvatar(), user.getName()),
            h3(user.getName()),
            p(user.getEmail()),
            when(user.isVerified(), () -> verifiedBadge())
        )
    )
)

// With index (if needed)
ul(
    each(IntStream.range(0, items.size()).boxed().toList(), i ->
        li(data("index", String.valueOf(i)),
            strong((i + 1) + ". "),
            items.get(i)
        )
    )
)"""),

            h3Title("Combining Conditionals"),
            para("Use conditionals together for complex UIs."),
            codeBlock("""
// Complex dashboard
div(class_("dashboard"),
    // Header with conditional user display
    header(
        h1("Dashboard"),
        currentUser != null ? userDropdown(currentUser) : a(href("/login"), "Sign In")
    ),

    // Main content with role-based rendering
    main(
        switch (role) {
            case ADMIN -> adminContent();
            case EDITOR -> editorContent();
            default -> viewerContent();
        }
    ),

    // Conditional sidebar
    when(showSidebar, () ->
        aside(
            h3("Quick Links"),
            each(quickLinks, link ->
                a(href(link.getUrl()), link.getTitle())
            )
        )
    ),

    // Footer with conditional elements
    footer(
        p("© 2026"),
        when(showDebugInfo, () -> debugPanel())
    )
)"""),

            h3Title("Null Safety"),
            para("Handle potentially null values safely."),
            codeBlock("""
// Using when() for null checks
when(user != null, () ->
    div(
        h2("Welcome, " + user.getName()),
        p(user.getBio())
    )
)

// With Optional
Optional<User> maybeUser = findUser(id);

maybeUser.map(user ->
    div(h2(user.getName()))
).orElse(
    div(p("User not found"))
)

// Null-safe chaining in rendering
div(
    h2(user != null ? user.getName() : "Guest"),
    when(user != null && user.getAddress() != null, () ->
        p(user.getAddress().getCity())
    )
)"""),

            docTip("Use lazy evaluation () -> element() for expensive operations to avoid unnecessary computation when the condition is false.")
        );
    }
}
