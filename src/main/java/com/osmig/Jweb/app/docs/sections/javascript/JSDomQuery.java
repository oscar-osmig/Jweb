package com.osmig.Jweb.app.docs.sections.javascript;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSDomQuery {
    private JSDomQuery() {}

    public static Element render() {
        return section(
            h3Title("DOM Query Builder"),
            para("Fluent API for querying and manipulating DOM elements."),
            codeBlock("""
import static jweb.Actions.*;

// Query single element
dom("#status")
    .setText("Updated!")
    .addClass("success")

// Query with attribute selector
dom("[data-active='true']")
    .removeClass("hidden")
    .addClass("visible")"""),

            h3Title("Chained Operations"),
            para("Chain multiple operations on selected elements."),
            codeBlock("""
dom("#user-panel")
    .removeClass("loading")
    .addClass("loaded")
    .attr("data-ready", "true")
    .show()

// Style manipulation
dom(".card")
    .style("background", "#f0f0f0")
    .style("border-radius", "8px")
    .addClass("elevated")

// Content manipulation
dom("#message")
    .setText("Hello, World!")
    .removeClass("error")
    .addClass("success")"""),

            h3Title("Query All"),
            para("Select and manipulate multiple elements."),
            codeBlock("""
// Query all matching elements
domAll(".notification").addClass("fade-out")

// Hide all items
domAll(".list-item").hide()

// Toggle class on all
domAll(".tab").removeClass("active")

// Then activate one
dom("#tab-1")
    .addClass("active")"""),

            h3Title("Element State"),
            para("Check and modify element state."),
            codeBlock("""
// Visibility
dom("#modal").show()
dom("#modal").hide()
toggle("panel")  // toggle by element id

// Classes
dom("#btn")
    .addClass("loading")
    .removeClass("enabled")
    .toggleClass("expanded")

// Attributes
dom("#input")
    .attr("disabled", "true")
    .attr("placeholder", "Enter value...")
    .removeAttr("readonly")"""),

            h3Title("In Actions Context"),
            para("Use query builder within action handlers."),
            codeBlock("""
windowFunc("togglePanel")
    .does(
        toggle("panel"),
        dom("#toggle-btn").toggleClass("active")
    )

onClick("search-btn").then(all(
    dom("#search-input").addClass("loading"),
    fetch("/api/search?q=").appendVar("searchQuery")
        .ok(all(
            dom("#results").setHtml(response("html")),
            dom("#search-input").removeClass("loading")
        ))
))"""),

            docTip("Use dom() for a single element, domAll() to operate on many.")
        );
    }
}
