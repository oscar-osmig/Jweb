package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsModern {
    private ElementsModern() {}

    public static Element render() {
        return section(
            h3Title("Modern HTML5 Elements"),
            para("Modern interactive elements with built-in browser functionality."),

            h3Title("Dialog (Modal)"),
            para("Native modal dialogs with backdrop and close behavior."),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.DialogHelper.*;

// Define the dialog
dialog(attrs().id("confirm-dialog"),
    h2("Confirm Action"),
    p("Are you sure you want to proceed?"),
    div(class_("dialog-actions"),
        button(attrs().onclick(close("confirm-dialog")), "Cancel"),
        button(attrs().onclick(close("confirm-dialog", "confirmed")), "Confirm")
    )
)

// Open as modal (with backdrop)
button(attrs().onclick(showModal("confirm-dialog")), "Open Modal")

// Open as non-modal
button(attrs().onclick(show("confirm-dialog")), "Open Non-Modal")

// Toggle
button(attrs().onclick(toggle("confirm-dialog")), "Toggle")

// Close on backdrop click
dialog(attrs()
    .id("my-dialog")
    .onclick(closeOnBackdropClick("my-dialog")),
    div(class_("dialog-content"),
        h2("Click outside to close"),
        p("Dialog content here")
    )
)"""),

            h3Title("Details & Summary (Accordion)"),
            para("Expandable content sections with native toggle behavior."),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.DetailsHelper.*;

// Basic collapsible
details(
    summary("Click to expand"),
    p("Hidden content revealed when expanded")
)

// Open by default
details(attrs().open(),
    summary("Already expanded"),
    p("Visible content")
)

// FAQ Accordion (exclusive - using name attribute)
div(class_("faq"),
    details(attrs().name("faq"),
        summary("What is JWeb?"),
        p("JWeb is a pure Java web framework...")
    ),
    details(attrs().name("faq"),
        summary("How do I get started?"),
        p("Add the dependency and create a page...")
    ),
    details(attrs().name("faq"),
        summary("Is it production ready?"),
        p("Yes, JWeb is production ready...")
    )
)

// Control with JavaScript helpers
button(attrs().onclick(openAll("faq")), "Expand All")
button(attrs().onclick(closeAll("faq")), "Collapse All")
button(attrs().onclick(openExclusive("faq-1", "faq")), "Open First Only")"""),

            h3Title("Progress Bar"),
            para("Display progress of a task."),
            codeBlock("""
// Determinate progress (known completion)
progress(attrs().value(70).set("max", "100"))

// Shorthand
progress(70, 100)  // 70% complete

// Indeterminate (unknown completion)
progressIndeterminate()  // Animated, no value

// Styled with label
div(class_("progress-container"),
    label("Uploading..."),
    progress(45, 100),
    span("45%")
)"""),

            h3Title("Meter"),
            para("Display a scalar measurement within a known range."),
            codeBlock("""
// Basic meter
meter(attrs()
    .value(0.6)
    .set("min", "0")
    .set("max", "1")
)

// Shorthand
meter(0.6, 0, 1)  // 60% of range

// With thresholds (browser applies colors)
meter(attrs()
    .value(75)
    .set("min", "0")
    .set("max", "100")
    .low(25)     // Below this is "low"
    .high(75)    // Above this is "high"
    .optimum(50) // Optimal value
)

// Examples
div(
    label("Disk usage: "),
    meter(0.8, 0, 1),   // 80% - shown as warning (high)
    span(" 80%")
)

div(
    label("Battery: "),
    meter(0.2, 0, 1),   // 20% - shown as danger (low)
    span(" 20%")
)"""),

            h3Title("Template & Slot (Web Components)"),
            para("Define reusable HTML templates and content slots."),
            codeBlock("""
// Template (not rendered, used by JavaScript)
template(attrs().id("card-template"),
    div(class_("card"),
        h3(class_("card-title")),
        p(class_("card-content")),
        slot("actions")  // Named slot
    )
)

// Slot for web components
slot()                  // Default slot
slot("header")          // Named slot

// Output (for calculation results)
form(attrs().oninput("result.value = a.valueAsNumber + b.valueAsNumber"),
    input(attrs().type("number").name("a").value("0")),
    text(" + "),
    input(attrs().type("number").name("b").value("0")),
    text(" = "),
    output(attrs().name("result").set("for", "a b"), "0")
)"""),

            h3Title("Time & Data"),
            para("Semantic elements for machine-readable dates and values."),
            codeBlock("""
// Time with machine-readable datetime
timeWithDatetime("2026-01-21", "January 21, 2026")
// Renders: <time datetime="2026-01-21">January 21, 2026</time>

// Event time
p("The concert starts at ",
    timeWithDatetime("20:00", "8 PM"),
    ".")

// Duration
p("Flight duration: ",
    timeWithDatetime("PT2H30M", "2 hours 30 minutes")
)

// Data (machine-readable value)
data("12345", "Product Name")
// Renders: <data value="12345">Product Name</data>

// Useful for product IDs, prices, etc.
p("Price: ", data("99.99", "$99.99"))"""),

            h3Title("Text Direction"),
            para("Control text direction for internationalization."),
            codeBlock("""
// BDI - Bi-Directional Isolation
// Isolates text that might have different direction
p("User ", bdi(username), " posted this comment")
// Prevents RTL usernames from affecting surrounding text

// BDO - Bi-Directional Override
// Force specific direction
bdo(attrs().dir("rtl"), "This text is right-to-left")
bdo(attrs().dir("ltr"), "This text is left-to-right")"""),

            h3Title("Ruby Annotation"),
            para("For East Asian typography - pronunciation guides above characters."),
            codeBlock("""
// Ruby annotation for Japanese/Chinese
ruby(
    text("漢"),
    rp("("),
    rt("かん"),  // Reading/pronunciation
    rp(")")
)

// Multiple characters
ruby(
    text("東京"),
    rp("("),
    rt("とうきょう"),
    rp(")")
)
// Renders with pronunciation above the characters"""),

            docTip("Modern HTML5 elements work in all modern browsers. Dialog and details have native keyboard support and accessibility built-in.")
        );
    }
}
