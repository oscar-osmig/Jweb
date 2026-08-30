package com.osmig.Jweb.app.docs.sections.conditionals;

import jweb.Element;
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

            h3Title("Iterating With an Index"),
            para("Iterate over an index range to access positions."),
            codeBlock("""
// Numbered list
ol(each(IntStream.range(0, items.size()).boxed().toList(), i ->
    li((i + 1) + ". " + items.get(i))
))

// Alternating rows
table(tbody(each(IntStream.range(0, users.size()).boxed().toList(), i ->
    tr(attrs().class_(i % 2 == 0 ? "even" : "odd"),
        td(users.get(i).getName()),
        td(users.get(i).getEmail())
    )
)))

// Separator between items
div(each(IntStream.range(0, items.size()).boxed().toList(), i ->
    span(
        text(items.get(i)),
        when(i < items.size() - 1, () -> text(", "))
    )
))"""),

            h3Title("Empty State"),
            codeBlock("""
// Show empty state when list is empty
when(items.isEmpty())
    .then(div(attrs().class_("empty"), text("No items found")))
    .otherwise(ul(each(items, item -> li(item))))""")
        );
    }
}
