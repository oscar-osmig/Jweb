package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsEnhanced {
    private ElementsEnhanced() {}

    public static Element render() {
        return section(
            h3Title("Enhanced HTML Elements"),
            para("Additional semantic elements for popovers, responsive images, definitions, text annotations, and forms."),

            h3Title("Popover API"),
            para("Native popover elements with auto-dismiss and backdrop behavior."),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.PopoverElements.*;
import static com.osmig.Jweb.framework.elements.El.*;

// Auto popover (dismisses on click outside or Escape)
popoverToggleButton("my-popup", "Toggle Menu"),
autoPopover("my-popup",
    ul(
        li("Option 1"),
        li("Option 2"),
        li("Option 3")
    )
)

// Manual popover (only dismisses programmatically)
popoverShowButton("info-pop", "Show Info"),
popoverHideButton("info-pop", "Close"),
manualPopover("info-pop",
    p("This stays open until explicitly closed.")
)

// Popover with attributes on custom elements
div(attrs().attr(popover()).id("custom-pop"),
    p("Custom popover content")
)
button(attrs().attr(popoverTarget("custom-pop")),
    "Toggle Custom"
)

// JavaScript control
// showPopover("my-popup")
// hidePopover("my-popup")
// togglePopover("my-popup")"""),

            h3Title("Responsive Images"),
            para("Art direction and format selection for optimized image delivery."),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.PictureElements.*;
import static com.osmig.Jweb.framework.elements.El.*;

// Art direction: different images per viewport
picture(
    source(srcset("hero-wide.jpg"), media("(min-width: 1024px)")),
    source(srcset("hero-medium.jpg"), media("(min-width: 640px)")),
    img("hero-small.jpg", "Hero image")
)

// Format selection: modern formats with fallback
picture(
    source(srcset("photo.avif"), type("image/avif")),
    source(srcset("photo.webp"), type("image/webp")),
    img("photo.jpg", "Photo")
)

// Responsive with density descriptors
responsiveImg("photo.jpg", "Photo", "photo-2x.jpg")
// Renders: <img src="photo.jpg" srcset="photo.jpg 1x,photo-2x.jpg 2x">

// Lazy-loaded image with CLS prevention
lazyImg("hero.jpg", "Hero", 800, 600)
// Renders: <img src="hero.jpg" loading="lazy" width="800" height="600">

// Loading attributes
lazyLoad()       // loading="lazy"
eagerLoad()      // loading="eager"
fetchPriority("high")
decoding("async")"""),

            h3Title("Figure and Caption"),
            para("Semantic container for self-contained content with optional captions."),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.El.*;

// Image with caption
figure(
    img("chart.png", "Sales chart"),
    figcaption("Figure 1: Quarterly sales data")
)

// Code listing with caption
figure(attrs().class_("code-example"),
    pre(code("const greeting = 'Hello World';")),
    figcaption("Example: Variable declaration")
)

// Blockquote with attribution
figure(
    blockquote("To be or not to be, that is the question."),
    figcaption("William Shakespeare")
)"""),

            h3Title("Definition Lists"),
            para("Semantic element for term-definition pairs, glossaries, and key-value data."),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.El.*;

// Glossary
dl(
    dt("HTML"), dd("HyperText Markup Language"),
    dt("CSS"),  dd("Cascading Style Sheets"),
    dt("JS"),   dd("JavaScript")
)

// Metadata / key-value pairs
dl(attrs().class_("metadata"),
    dt("Author"),    dd("Jane Doe"),
    dt("Published"), dd("2026-01-29"),
    dt("Category"),  dd("Technology")
)"""),

            h3Title("Interactive Text Elements"),
            para("Semantic inline elements for abbreviations, citations, quotations, keyboard input, and text annotations."),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.El.*;

// Abbreviation with tooltip expansion
p("The ", abbr("HTML", "HyperText Markup Language"), " spec")
// Hovering shows "HyperText Markup Language"

// Definition term
p("A ", dfn("closure"), " captures its lexical scope.")

// Citation (title of a work)
p("As described in ", cite("The Art of Programming"), "...")

// Inline quotation (auto-adds quotes)
p("She said, ", q("Hello World"), " and it ran.")

// Blockquote with source URL
blockquote("https://example.com",
    p("Knowledge is power.")
)

// Keyboard input
p("Press ", kbd("Ctrl"), "+", kbd("C"), " to copy.")

// Sample output
p("The program outputs: ", samp("Hello World"))

// Variable
p("Let ", var_("x"), " = 5")

// Highlighted text
p("Search results for: ", mark("JWeb framework"))

// Subscript / Superscript
p("H", sub("2"), "O")         // H₂O
p("E = mc", sup("2"))         // E = mc²

// Inserted / Deleted text
p(del("Old price: $50"), " ", ins("New price: $30"))

// Strikethrough (no longer accurate)
p(s("Available in stores"), " — Now online only!")"""),

            h3Title("Form Enhancements"),
            para("Modern form elements for autocomplete, grouping, and specialized inputs."),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.FormEnhancements.*;
import static com.osmig.Jweb.framework.elements.El.*;

// Autocomplete with datalist
input(attrs().attr("list", "browsers").name("browser")),
datalist("browsers",
    option("Chrome", "Chrome"),
    option("Firefox", "Firefox"),
    option("Safari", "Safari"),
    option("Edge", "Edge")
)

// Grouped options in select
select(attrs().name("car"),
    optgroup("Swedish Cars",
        option("volvo", "Volvo"),
        option("saab", "Saab")),
    optgroup("German Cars",
        option("bmw", "BMW"),
        option("audi", "Audi"))
)

// Fieldset with legend (groups related controls)
fieldset(
    legend("Personal Information"),
    label("Name:"),
    input(attrs().type("text").name("name")),
    label("Email:"),
    input(attrs().type("email").name("email"))
)

// Specialized input types
colorInput("theme-color", "#3b82f6")     // Color picker
dateInput("birthday")                     // Date picker
dateInput("event", "2026-01-01", "2026-12-31") // With range
timeInput("meeting")                      // Time picker
datetimeInput("appointment")              // Date + time
monthInput("start-month")                // Month picker
weekInput("sprint-week")                  // Week picker
rangeInput("volume", 0, 100, 50)          // Slider
rangeInput("opacity", 0, 100, 50, 5)     // Slider with step"""),

            docTip("Core elements (figure, dl, blockquote, fieldset, etc.) are available via El.* static import. " +
                   "Specialized helpers (popoverToggleButton, responsiveImg, colorInput, rangeInput, etc.) " +
                   "require importing from their specific module: PopoverElements, PictureElements, or FormEnhancements.")
        );
    }
}
