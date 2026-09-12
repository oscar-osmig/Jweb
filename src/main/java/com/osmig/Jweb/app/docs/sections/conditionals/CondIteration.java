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
class User {
    String getName() { return "Ada"; }
    String getEmail() { return "ada@example.com"; }
    boolean isAdmin() { return true; }
}
class UserService { List<User> findAll() { return List.of(new User()); } }
UserService userService = new UserService();

// Simple list
List<String> items = List.of("Apple", "Banana", "Cherry");
ul(each(items, item -> li(item)))

// Complex items
List<User> users = userService.findAll();
div(each(users, user ->
    div(class_("user-card"),
        h3(user.getName()),
        p(user.getEmail()),
        when(user.isAdmin(), () -> UI.badge("Admin"))
    )
))"""),

            h3Title("Iterating With an Index"),
            para("Iterate over an index range to access positions."),
            codeBlock("""
class User {
    String getName() { return "Ada"; }
    String getEmail() { return "ada@example.com"; }
}
List<String> items = List.of("Apple", "Banana", "Cherry");
List<User> users = List.of(new User());

// Numbered list
ol(each(IntStream.range(0, items.size()).boxed().toList(), i ->
    li((i + 1) + ". " + items.get(i))
))

// Alternating rows
table(tbody(each(IntStream.range(0, users.size()).boxed().toList(), i ->
    tr(class_(i % 2 == 0 ? "even" : "odd"),
        td(users.get(i).getName()),
        td(users.get(i).getEmail())
    )
)))

// Separator between items
div(each(IntStream.range(0, items.size()).boxed().toList(), i ->
    span(items.get(i), i < items.size() - 1 ? ", " : "")
))"""),

            h3Title("Empty State"),
            codeBlock("""
List<String> items = List.of("Apple", "Banana", "Cherry");

// Show empty state when list is empty
items.isEmpty()
    ? div(class_("empty"), "No items found")
    : ul(each(items, item -> li(item)))""")
        );
    }
}
