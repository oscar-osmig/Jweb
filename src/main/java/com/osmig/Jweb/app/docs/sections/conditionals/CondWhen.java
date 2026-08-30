package com.osmig.Jweb.app.docs.sections.conditionals;

import jweb.Element;
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

            h3Title("Inverse Conditions"),
            para("Negate the condition to show elements only when it is false."),
            codeBlock("""
// Show login link when NOT logged in
when(!isLoggedIn, () ->
    a(attrs().href("/login"), text("Please log in"))
)

// Show empty state
when(!items.isEmpty(), () -> itemList(items))
when(items.isEmpty(), () -> emptyState())

// Combine both branches
div(
    when(isLoggedIn, () -> userMenu()),
    when(!isLoggedIn, () -> loginButton())
)""")
        );
    }
}
