package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsLists {
    private ElementsLists() {}

    public static Element render() {
        return section(
            h3Title("Lists"),
            para("Unordered, ordered, and definition lists."),
            codeBlock("""
// Unordered list
ul(
    li("First item"),
    li("Second item"),
    li("Third item")
)

// Ordered list
ol(
    li("Step one"),
    li("Step two"),
    li("Step three")
)

// Definition list
dl(
    dt("Term"),
    dd("Definition of the term"),
    dt("Another term"),
    dd("Its definition")
)"""),

            h3Title("Dynamic Lists"),
            para("Use each() to render lists from collections."),
            codeBlock("""
List<String> items = List.of("Apple", "Banana", "Cherry");
ul(each(items, item -> li(item)))

// With index
ul(eachWithIndex(items, (item, i) ->
    li((i + 1) + ". " + item)
))

// Complex list items
List<User> users = userService.findAll();
ul(each(users, user ->
    li(
        strong(user.getName()),
        text(" - "),
        span(user.getEmail())
    )
))""")
        );
    }
}
