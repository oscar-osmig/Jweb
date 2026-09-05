package com.osmig.Jweb.app.docs.sections.elements;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsTables {
    private ElementsTables() {}

    public static Element render() {
        return section(
            h3Title("Tables"),
            para("Create data tables with headers, body, and footer."),
            codeBlock("""
table(
    thead(
        tr(th("Name"), th("Email"), th("Role"))
    ),
    tbody(
        tr(td("John"), td("john@test.com"), td("Admin")),
        tr(td("Jane"), td("jane@test.com"), td("User"))
    ),
    tfoot(
        tr(td(attrs().colspan("3"), "2 users total"))
    )
)"""),

            h3Title("Dynamic Tables"),
            para("Render table rows from data collections."),
            codeBlock("""
List<User> users = userService.findAll();

table(
    thead(tr(th("ID"), th("Name"), th("Email"), th("Actions"))),
    tbody(each(users, user -> tr(
        td(String.valueOf(user.getId())),
        td(user.getName()),
        td(user.getEmail()),
        td(
            button(class_("btn-edit"), "Edit"),
            button(class_("btn-delete"), "Delete")
        )
    )))
)"""),

            h3Title("Sortable Table Headers"),
            codeBlock("""
th(attrs()
    .class_("sortable")
    .data("sort", "name")
    .set("onclick", "sortTable('name')"),
    "Name ", span(class_("sort-icon")))""")
        );
    }
}
