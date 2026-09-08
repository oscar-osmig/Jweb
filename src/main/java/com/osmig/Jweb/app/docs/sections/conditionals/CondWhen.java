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
        img(src(user.getAvatar())),
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

            before("v3.0.0",
                h3Title("Inverse Conditions"),
                para("Negate the condition to show elements only when it is false."),
                codeBlock("""
// Show login link when NOT logged in
when(!isLoggedIn, () ->
    a(href("/login"), "Please log in")
)

// Show empty state
when(!items.isEmpty(), () -> itemList(items))
when(items.isEmpty(), () -> emptyState())

// Combine both branches
div(
    when(isLoggedIn, () -> userMenu()),
    when(!isLoggedIn, () -> loginButton())
)""")),

            since("v3.0.0",
                h3Title("Inverse Conditions"),
                para("A one-sided when() takes a negated condition; two branches are one " +
                     "call, so the predicate is never written twice."),
                codeBlock("""
// Show login link when NOT logged in
when(!isLoggedIn, a(href("/login"), "Please log in"))

// Show the empty state instead of the list — one call, one predicate
when(items.isEmpty(), emptyState(), itemList(items))

// Same for a menu
when(isLoggedIn, userMenu(), loginButton())""")),

            since("v3.0.0",
                h3Title("Conditional Text and Classes"),
                para("A String branch is text, so when() also composes class names: " +
                     "classes(...) joins its parts and skips the ones that did not match."),
                codeBlock("""
// Text
span(when(unread > 0, unread + " new", "All caught up"))

// Class names — no string concatenation, no ternary
a(classes("chip", when(active, "chip-on")), href("/tag/" + tag), tag)

// The same on an attrs() chain
a(attrs().cls("chip").classIf(active, "chip-on").href(url), tag)"""))
        );
    }
}
