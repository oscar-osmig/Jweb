package com.osmig.Jweb.app.docs.sections.conditionals;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CondWhen {
    private CondWhen() {}

    public static Element render() {
        return section(
            h3Title("when() - Conditional Display"),
            para("Show elements only when a condition is true."),
            codeBlock("""
// Basic when
when(isLoggedIn, () -> span("Welcome back!"))

// With complex element
when(user != null, () ->
    div(
        img(attrs().src(user.getAvatar())),
        span(user.getName())
    )
)

// Check property
when(cart.hasItems(), () ->
    div(
        span("Items: " + cart.getItemCount()),
        button("Checkout")
    )
)"""),

            h3Title("unless() - Inverse Conditional"),
            para("Show elements only when a condition is false."),
            codeBlock("""
// Show login link when NOT logged in
unless(isLoggedIn, () ->
    a(attrs().href("/login"), text("Please log in"))
)

// Show empty state
unless(items.isEmpty(), () -> itemList(items))
unless(!items.isEmpty(), () -> emptyState())

// Combine with when
div(
    when(isLoggedIn, () -> userMenu()),
    unless(isLoggedIn, () -> loginButton())
)""")
        );
    }
}
