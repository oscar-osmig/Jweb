package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsAttributes {
    private ElementsAttributes() {}

    public static Element render() {
        return section(
            h3Title("Attributes API"),
            para("The attrs() builder provides type-safe, fluent attribute building."),

            h3Title("Basic Attributes"),
            codeBlock("""
// Core attributes
div(attrs()
    .id("main")
    .class_("container")
    .title("Tooltip text")
)

// Shorthand for common attributes
div(id("main"))           // Just id
div(class_("container"))  // Just class
a(href("/home"), "Home")  // Just href"""),

            h3Title("Multiple Classes"),
            para("Add classes individually, conditionally, or as a list."),
            codeBlock("""
// Add classes
attrs().class_("btn").addClass("primary").addClass("lg")

// Multiple classes at once
attrs().classes("btn", "primary", "lg")

// Conditional classes
attrs()
    .class_("btn")
    .classIf("active", isActive)       // Add if true
    .classIf("disabled", isDisabled)

// Toggle between classes
attrs().classToggle(isOpen, "open", "closed")

// Complex conditional
attrs()
    .class_("card")
    .classIf("featured", product.isFeatured())
    .classIf("soldout", product.getStock() == 0)
    .classToggle(expanded, "expanded", "collapsed")"""),

            h3Title("Inline Styles"),
            para("Three ways to add inline styles."),
            codeBlock("""
// 1. Lambda syntax (recommended - no .done() needed)
div(attrs()
    .class_("card")
    .style(s -> s
        .display(flex)
        .padding(rem(1))
        .backgroundColor(white)
        .borderRadius(px(8))
    ),
    content
)

// 2. Continuing attributes after style with .done()
a(attrs()
    .style()
        .color(blue)
        .textDecoration(none)
    .done()
    .href("/home")
    .class_("nav-link"),
    "Home"
)

// 3. Pass a Style object
Style cardStyle = style()
    .padding(rem(1.5))
    .backgroundColor(white)
    .borderRadius(px(8));

div(attrs().style(cardStyle), content)"""),

            h3Title("Layout Shortcuts"),
            para("Quick flexbox and grid setup."),
            codeBlock("""
// Flexbox helpers
attrs().flexCenter()              // Flex + center items
attrs().flexColumn("1rem")        // Column with gap
attrs().flexRow("1rem")           // Row with gap
attrs().flexBetween()             // Space-between + center

// Grid helpers
attrs().gridCols(3, "1rem")       // 3-column grid with gap
attrs().gridCols(4)               // 4-column grid without gap

// Example
div(attrs().flexRow("1rem"),
    card1,
    card2,
    card3
)

div(attrs().gridCols(3, "2rem"),
    each(products, this::renderCard)
)"""),

            h3Title("Data & ARIA Attributes"),
            para("Custom data attributes and accessibility."),
            codeBlock("""
// Data attributes
attrs()
    .data("user-id", "123")
    .data("role", "admin")
    .data("active", "true")
// Renders: data-user-id="123" data-role="admin" data-active="true"

// ARIA attributes
attrs()
    .aria("label", "Close dialog")
    .aria("expanded", "true")
    .aria("controls", "menu-panel")
// Renders: aria-label="Close dialog" aria-expanded="true" ...

// Role
attrs().role("button")
attrs().role("navigation")
attrs().role("dialog")

// Combining for accessible button
button(attrs()
    .class_("icon-btn")
    .aria("label", "Close")
    .title("Close window"),
    icon("x")
)"""),

            h3Title("Boolean Attributes"),
            para("Attributes that don't need values."),
            codeBlock("""
// Form attributes
input(attrs()
    .disabled()           // Cannot interact
    .readonly()           // Can't edit, can submit
    .required()           // Must fill before submit
    .checked()            // For checkboxes/radios
    .autofocus()          // Focus on page load
    .multiple()           // Allow multiple selections
)

// Media attributes
video(attrs()
    .controls()           // Show playback controls
    .autoplay()           // Start playing
    .loop()               // Loop playback
    .muted()              // Start muted
)

// Other
details(attrs().open())   // Expanded by default
script(attrs().defer())   // Defer loading
script(attrs().async())   // Async loading
option(attrs().selected()) // Pre-selected option"""),

            h3Title("Link & Navigation"),
            para("Attributes for links and navigation."),
            codeBlock("""
// External link (secure)
a(attrs()
    .href("https://external.com")
    .targetBlank()        // target="_blank" rel="noopener noreferrer"
)

// Download link
a(attrs()
    .href("/file.pdf")
    .download()           // Download instead of navigate
)

// Download with filename
a(attrs()
    .href("/report-2026.pdf")
    .download("annual-report.pdf")  // Custom filename
)

// Rel attributes
a(attrs()
    .href("/next")
    .rel("next")          // Pagination hint
)

a(attrs()
    .href("https://sponsor.com")
    .rel("sponsored")     // Paid link (SEO)
)"""),

            h3Title("Image Attributes"),
            para("Comprehensive image handling."),
            codeBlock("""
// Basic with alt
img(attrs()
    .src("/images/hero.jpg")
    .alt("Hero image description")
)

// Lazy loading
img(attrs()
    .src("/images/photo.jpg")
    .alt("Photo")
    .loading("lazy")      // Lazy load when near viewport
)

// Dimensions (prevents layout shift)
img(attrs()
    .src("/images/product.jpg")
    .alt("Product")
    .width("800")
    .height("600")
)

// Responsive images
img(attrs()
    .src("/images/photo.jpg")
    .srcset("/images/photo-2x.jpg 2x, /images/photo-3x.jpg 3x")
    .alt("Photo")
)

// Picture element for art direction
picture(
    source(attrs().media("(min-width: 800px)").srcset("/large.jpg")),
    source(attrs().media("(min-width: 400px)").srcset("/medium.jpg")),
    img(attrs().src("/small.jpg").alt("Responsive image"))
)"""),

            h3Title("Event Handlers"),
            para("Attach JavaScript event handlers."),
            codeBlock("""
// Click handlers
button(attrs().onclick("handleClick()"), "Click")
button(attrs().onclick("deleteItem(" + id + ")"), "Delete")

// Form events
form(attrs().onsubmit("return handleSubmit(event)"))
input(attrs().onchange("updateValue(this.value)"))
input(attrs().oninput("search(this.value)"))
input(attrs().onfocus("showHint()"))
input(attrs().onblur("hideHint()"))

// Mouse events
div(attrs()
    .onmouseenter("showTooltip()")
    .onmouseleave("hideTooltip()")
)

// Keyboard events
input(attrs().onkeydown("handleKey(event)"))
input(attrs().onkeyup("handleKeyUp(event)"))

// Using JS DSL actions
import static com.osmig.Jweb.framework.js.Actions.*;

button(attrs().onclick(show("panel")), "Show")
button(attrs().onclick(hide("modal")), "Close")
button(attrs().onclick(toggle("dropdown")), "Toggle")"""),

            h3Title("Custom Attributes"),
            para("Set any attribute not in the API."),
            codeBlock("""
// Generic set method
attrs().set("custom-attr", "value")

// HTMX attributes
attrs()
    .set("hx-get", "/api/data")
    .set("hx-target", "#result")
    .set("hx-swap", "innerHTML")

// Alpine.js
attrs()
    .set("x-data", "{ open: false }")
    .set("x-show", "open")
    .set("@click", "open = !open")

// Any attribute
attrs()
    .set("itemscope", "")
    .set("itemtype", "https://schema.org/Product")"""),

            docTip("Use class_() instead of class() because 'class' is a reserved word in Java.")
        );
    }
}
