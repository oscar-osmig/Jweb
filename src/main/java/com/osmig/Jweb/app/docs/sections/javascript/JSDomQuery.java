package com.osmig.Jweb.app.docs.sections.javascript;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSDomQuery {
    private JSDomQuery() {}

    public static Element render() {
        return section(
            h3Title("DOM Query Builder"),
            para("Fluent API for querying and manipulating DOM elements."),
            codeBlock("""
import static com.osmig.Jweb.framework.js.Actions.*;

// Query single element
query("#status")
    .setText("Updated!")
    .addClass("success")

// Query with attribute selector
query("[data-active='true']")
    .removeClass("hidden")
    .addClass("visible")"""),

            h3Title("Chained Operations"),
            para("Chain multiple operations on selected elements."),
            codeBlock("""
query("#user-panel")
    .removeClass("loading")
    .addClass("loaded")
    .attr("data-ready", "true")
    .show("flex")

// Style manipulation
query(".card")
    .setStyle("background", "#f0f0f0")
    .setStyle("border-radius", "8px")
    .addClass("elevated")

// Content manipulation
query("#message")
    .setText("Hello, World!")
    .removeClass("error")
    .addClass("success")"""),

            h3Title("Query All"),
            para("Select and manipulate multiple elements."),
            codeBlock("""
// Query all matching elements
queryAll(".notification")
    .forEach(el -> el.addClass("fade-out"))

// Hide all items
queryAll(".list-item")
    .forEach(el -> el.hide())

// Toggle class on all
queryAll(".tab")
    .forEach(el -> el.removeClass("active"))

// Then activate one
query("#tab-1")
    .addClass("active")"""),

            h3Title("Element State"),
            para("Check and modify element state."),
            codeBlock("""
// Visibility
query("#modal").show()
query("#modal").show("flex")  // with display mode
query("#modal").hide()
query("#panel").toggle()

// Classes
query("#btn")
    .hasClass("active")       // returns boolean
    .addClass("loading")
    .removeClass("enabled")
    .toggleClass("expanded")

// Attributes
query("#input")
    .attr("disabled", "true")
    .attr("placeholder", "Enter value...")
    .removeAttr("readonly")"""),

            h3Title("In Actions Context"),
            para("Use query builder within action handlers."),
            codeBlock("""
windowFunc("togglePanel")
    .params("panelId")
    .does(
        raw(query("#" + "panelId").toggle()),
        query("#toggle-btn").toggleClass("active")
    )

onClick("search-btn").then(all(
    query("#search-input").addClass("loading"),
    fetch("/api/search?q=").appendVar("searchQuery")
        .ok(all(
            query("#results").setHtml("_data.html"),
            query("#search-input").removeClass("loading")
        ))
))"""),

            docTip("Use query() for single elements, queryAll() when you need to operate on multiple.")
        );
    }
}
