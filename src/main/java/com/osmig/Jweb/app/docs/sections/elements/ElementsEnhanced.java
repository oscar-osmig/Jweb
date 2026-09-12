package com.osmig.Jweb.app.docs.sections.elements;

import jweb.Element;
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
// The core attributes are on El; PopoverElements duplicates them under its
// own names (importing both is ambiguous), so its extra helpers — like
// popoverHideButton — are reached by qualifying the type instead.
import static jweb.El.*;

// Auto popover (dismisses on click outside or Escape)
button(popovertarget("my-popup"), "Toggle Menu"),
div(popover(), id("my-popup"),
    ul(
        li("Option 1"),
        li("Option 2"),
        li("Option 3")
    )
)

// Manual popover (only dismisses programmatically)
button(popovertarget("info-pop"), popovertargetaction("show"), "Show Info"),
jweb.el.PopoverElements.popoverHideButton("info-pop", "Close"),
div(popover("manual"), id("info-pop"),
    p("This stays open until explicitly closed.")
)

// Popover attributes on custom elements (Attr args)
div(popover(), id("custom-pop"),
    p("Custom popover content")
)
button(popovertarget("custom-pop"),
    "Toggle Custom"
)

// JavaScript control
// showPopover("my-popup")
// hidePopover("my-popup")
// togglePopover("my-popup")"""),

            h3Title("Responsive Images"),
            para("Art direction and format selection for optimized image delivery."),
            codeBlock("""
// picture/source/srcset/type/loading are on El already; PictureElements
// duplicates them under its own names, so importing its wildcard alongside
// El is ambiguous — import just the attributes El doesn't have by name.
import static jweb.El.*;
import static jweb.el.PictureElements.media;
import static jweb.el.PictureElements.width;
import static jweb.el.PictureElements.height;
import static jweb.el.PictureElements.eagerLoad;
import static jweb.el.PictureElements.fetchpriority;
import static jweb.el.PictureElements.decoding;

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
img(src("photo.jpg"), alt("Photo"), srcset("photo-2x.jpg 2x"))
// Renders: <img src="photo.jpg" srcset="photo.jpg 1x,photo-2x.jpg 2x">

// Lazy-loaded image with CLS prevention
img(src("hero.jpg"), alt("Hero"), width(800), height(600), loading("lazy"))
// Renders: <img src="hero.jpg" loading="lazy" width="800" height="600">

// Loading attributes
loading("lazy") // loading="lazy"
eagerLoad()      // loading="eager"
fetchpriority("high")
decoding("async")"""),

            h3Title("Figure and Caption"),
            para("Semantic container for self-contained content with optional captions."),
            codeBlock("""
import static jweb.El.*;

// Image with caption
figure(
    img("chart.png", "Sales chart"),
    figcaption("Figure 1: Quarterly sales data")
)

// Code listing with caption
figure(class_("code-example"),
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
import static jweb.El.*;

// Glossary
dl(
    dt("HTML"), dd("HyperText Markup Language"),
    dt("CSS"),  dd("Cascading Style Sheets"),
    dt("JS"),   dd("JavaScript")
)

// Metadata / key-value pairs
dl(class_("metadata"),
    dt("Author"),    dd("Jane Doe"),
    dt("Published"), dd("2026-01-29"),
    dt("Category"),  dd("Technology")
)"""),

            h3Title("Interactive Text Elements"),
            para("Semantic inline elements for abbreviations, citations, quotations, keyboard input, and text annotations."),
            codeBlock("""
import static jweb.El.*;

// Abbreviation with tooltip expansion
p("The ", abbr(attr("title", "HyperText Markup Language"), "HTML"), " spec")
// Hovering shows "HyperText Markup Language"

// Definition term
p("A ", dfn("closure"), " captures its lexical scope.")

// Citation (title of a work)
p("As described in ", cite("The Art of Programming"), "...")

// Inline quotation (auto-adds quotes)
p("She said, ", q("Hello World"), " and it ran.")

// Blockquote with source URL
blockquote(attrs().cite("https://example.com"),
    p("Knowledge is power.")
)

// Keyboard input
p("Press ", kbd("Ctrl"), "+", kbd("C"), " to copy.")

// Sample output
p("The program outputs: ", samp("Hello World"))

// Variable
p("Let ", tag("var", "x"), " = 5")

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
import static jweb.El.*;

// Autocomplete with datalist
input(attrs().list("browsers").name("browser")),
datalist(id("browsers"),
    option("Chrome"),
    option("Firefox"),
    option("Safari"),
    option("Edge")
)

// Grouped options in select
select(name("car"),
    optgroup(attrs().label("Swedish Cars"),
        option(value("volvo"), "Volvo"),
        option(value("saab"), "Saab")),
    optgroup(attrs().label("German Cars"),
        option(value("bmw"), "BMW"),
        option(value("audi"), "Audi"))
)

// Fieldset with legend (groups related controls)
fieldset(
    legend("Personal Information"),
    label("Name:"),
    input(type("text"), name("name")),
    label("Email:"),
    input(type("email"), name("email"))
)

// Specialized input types — a plain input() with the right type attribute
input(type("color"), name("theme-color"), value("#3b82f6"))            // Color picker
input(type("date"), name("birthday"))                                   // Date picker
input(type("date"), name("event"), attrs().min("2026-01-01").max("2026-12-31")) // With range
input(type("time"), name("meeting"))                                    // Time picker
input(type("datetime-local"), name("appointment"))                      // Date + time
input(type("month"), name("start-month"))                              // Month picker
input(type("week"), name("sprint-week"))                                // Week picker
input(type("range"), name("volume"), attrs().min(0).max(100).value("50"))          // Slider
input(type("range"), name("opacity"), attrs().min(0).max(100).value("50").step(5)) // Slider with step"""),

            docTip("Core elements (figure, dl, blockquote, fieldset, etc.) and the popover/picture attributes " +
                   "are all available via El.* static import; PopoverElements and PictureElements add a few " +
                   "action helpers (popoverHideButton, showPopover, ...) on top. There is no FormEnhancements " +
                   "module any more — a specialized input (date, time, range, color, ...) is a plain input() " +
                   "with the matching type attribute.")
        );
    }
}
