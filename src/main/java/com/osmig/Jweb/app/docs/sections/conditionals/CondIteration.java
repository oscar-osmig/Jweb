package com.osmig.Jweb.app.docs.sections.conditionals;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CondIteration {
    private CondIteration() {}

    public static Element render() {
        return section(
            h3Title("each() - List Iteration"),
            para("Render elements for each item in a collection."),
            codeBlock("""
// Simple list
List<String> items = List.of("Apple", "Banana", "Cherry");
ul(each(items, item -> li(item)))

// Complex items
List<User> users = userService.findAll();
div(each(users, user ->
    div(attrs().class_("user-card"),
        h3(user.getName()),
        p(user.getEmail()),
        when(user.isAdmin(), () -> badge("Admin"))
    )
))"""),

            h3Title("eachWithIndex() - With Index"),
            para("Access the index while iterating."),
            codeBlock("""
// Numbered list
ol(eachWithIndex(items, (item, index) ->
    li((index + 1) + ". " + item)
))

// Alternating rows
table(tbody(eachWithIndex(users, (user, i) ->
    tr(attrs().class_(i % 2 == 0 ? "even" : "odd"),
        td(user.getName()),
        td(user.getEmail())
    )
)))

// First/last handling
div(eachWithIndex(items, (item, i) ->
    span(
        text(item),
        when(i < items.size() - 1, () -> text(", "))
    )
))"""),

            h3Title("Empty State"),
            codeBlock("""
// Show empty state when list is empty
cond(items.isEmpty(),
    div(attrs().class_("empty"), text("No items found")),
    ul(each(items, item -> li(item)))
)""")
        );
    }
}
