package com.osmig.Jweb.app.docs.sections.conditionals;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CondTernary {
    private CondTernary() {}

    public static Element render() {
        return section(
            h3Title("cond() - Either/Or"),
            para("Choose between two elements based on a condition."),
            codeBlock("""
// Basic ternary: condition ? ifTrue : ifFalse
cond(isAdmin, adminDashboard(), userDashboard())

// Status display
cond(user.isActive(),
    span(attrs().class_("text-green"), text("Active")),
    span(attrs().class_("text-red"), text("Inactive"))
)

// Toggle button text
button(
    cond(isExpanded,
        text("Show Less"),
        text("Show More")
    )
)

// Nested conditions
cond(isPremium,
    premiumContent(),
    cond(isRegistered,
        basicContent(),
        guestContent()
    )
)"""),

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
div(attrs().class_(isError ? "error" : "success"),
    text(message)
)""")
        );
    }
}
