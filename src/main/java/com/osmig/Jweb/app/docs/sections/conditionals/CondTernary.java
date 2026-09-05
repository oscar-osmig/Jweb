package com.osmig.Jweb.app.docs.sections.conditionals;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CondTernary {
    private CondTernary() {}

    public static Element render() {
        return section(
            before("v3.0.0",
                h3Title("Either/Or"),
                para("Choose between two elements with when().then().otherwise()."),
                codeBlock("""
// condition ? ifTrue : ifFalse
when(isAdmin)
    .then(adminDashboard())
    .otherwise(userDashboard())

// Status display
when(user.isActive())
    .then(span(attrs().class_("text-green"), text("Active")))
    .otherwise(span(attrs().class_("text-red"), text("Inactive")))

// Toggle button text (Java ternary)
button(isExpanded ? text("Show Less") : text("Show More"))

// Nested conditions
when(isPremium)
    .then(premiumContent())
    .otherwise(when(isRegistered)
        .then(basicContent())
        .otherwise(guestContent()))""")),
            since("v3.0.0",
                h3Title("Either/Or"),
                para("Choose between two elements with a Java ternary; " +
                     "when(condition, element) still covers the one-sided, optional case."),
                codeBlock("""
// condition ? ifTrue : ifFalse
isAdmin ? adminDashboard() : userDashboard()

// Status display
user.isActive()
    ? span(class_("text-green"), "Active")
    : span(class_("text-red"), "Inactive")

// Toggle button text
button(isExpanded ? "Show Less" : "Show More")

// Nested conditions
isPremium
    ? premiumContent()
    : (isRegistered ? basicContent() : guestContent())""")),

            h3Title("Using Java Ternary"),
            para("Standard Java ternary works too."),
            codeBlock("""
// Java ternary with elements
div(
    isLoggedIn ? userPanel() : loginPrompt()
)

// For simple text
span(isActive ? "Active" : "Inactive")

// Choose class
div(class_(isError ? "error" : "success"),
    message
)""")
        );
    }
}
