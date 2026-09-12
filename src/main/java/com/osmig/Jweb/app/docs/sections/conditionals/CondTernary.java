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
    .then(span(attrs().class_("text-green"), "Active"))
    .otherwise(span(attrs().class_("text-red"), "Inactive"))

// Toggle button text (Java ternary)
button(isExpanded ? "Show Less" : "Show More")

// Nested conditions
when(isPremium)
    .then(premiumContent())
    .otherwise(when(isRegistered)
        .then(basicContent())
        .otherwise(guestContent()))""")),
            since("v3.0.0",
                h3Title("Either/Or"),
                para("when(condition, ifTrue, ifFalse) is the choice: the same call as the "
                     + "one-sided form with one more argument. Each branch may be an "
                     + "Element, a lambda (only the taken branch is built), or a String."),
                codeBlock("""
boolean isAdmin = true;
Element adminDashboard() { return div("Admin dashboard"); }
Element userDashboard() { return div("User dashboard"); }
class CurrentUser { boolean isActive() { return true; } }
CurrentUser user = new CurrentUser();
boolean isExpanded = false;
boolean isPremium = true;
Element premiumContent() { return div("Premium content"); }
Element guestContent() { return div("Guest content"); }

// condition ? ifTrue : ifFalse
when(isAdmin, adminDashboard(), userDashboard())

// Status display
when(user.isActive(),
    span(cls("text-green"), "Active"),
    span(cls("text-red"), "Inactive"))

// Toggle button text — a String branch is text
button(when(isExpanded, "Show Less", "Show More"))

// Lambdas when a branch is expensive to build
when(isPremium, () -> premiumContent(), () -> guestContent())"""),

                h3Title("The Chained Form"),
                para("When a branch runs several lines, the chain puts the two outcomes "
                     + "under each other instead of inside an argument list. A chain left "
                     + "without otherwise() is still an element — it renders the matched "
                     + "branch, or nothing."),
                codeBlock("""
boolean active = true;
String name = "java";
boolean isPremium = true;
Element premiumContent() { return div("Premium content"); }
boolean isRegistered = true;
Element basicContent() { return div("Basic content"); }
Element guestContent() { return div("Guest content"); }

when(active)
    .then(span(cls("chip chip-on"), name))
    .otherwise(a(href("/tag/" + name), cls("chip"), name))

// Nested choices read better as a chain than as nested arguments
when(isPremium)
    .then(premiumContent())
    .otherwise(when(isRegistered, basicContent(), guestContent()))"""),

                h3Title("Java's Ternary Still Works"),
                para("For a value rather than an element — a colour token, a number — the "
                     + "ternary is shorter and stays in the language."),
                codeBlock("""
boolean isError = false;
CSSValue RED = hex("#dc2626");
CSSValue GREEN = hex("#16a34a");
String message = "Saved";

// A style value
div(style().color(isError ? RED : GREEN), message)

// A class token
div(cls(isError ? "error" : "success"), message)"""))
        );
    }
}
