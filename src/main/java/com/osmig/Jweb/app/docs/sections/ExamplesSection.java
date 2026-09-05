package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ExamplesSection {
    private ExamplesSection() {}

    public static Element render() {
        return section(
            docTitle("Examples"),
            para("Complete examples showing JWeb patterns."),

            docSubtitle("Counter"),
            codeBlock("""
State<Integer> count = useState(0);

div(
    h1("Count: " + count.get()),
    button(onClick(e -> count.update(c -> c + 1)), "+"),
    button(onClick(e -> count.update(c -> c - 1)), "-")
)"""),

            docSubtitle("User Card"),
            codeBlock("""
public class UserCard implements Template {
    private final User user;

    public UserCard(User user) { this.user = user; }

    public Element render() {
        return div(style()
                .padding(rem(1)).backgroundColor(white)
                .borderRadius(px(8)).boxShadow(px(0), px(2), px(8), rgba(0,0,0,0.1)),
            h3(user.getName()),
            p(user.getEmail()),
            when(user.isAdmin(), () -> badge("Admin"))
        );
    }
}"""),

            docSubtitle("Data Table"),
            codeBlock("""
table(
    thead(tr(th("Name"), th("Email"), th("Actions"))),
    tbody(each(users, user -> tr(
        td(user.getName()),
        td(user.getEmail()),
        td(
            button(onClick(call("editUser", String.valueOf(user.getId()))), "Edit"),
            button(onClick(call("deleteUser", String.valueOf(user.getId()))), "Delete")
        )
    )))
)"""),

            docTip("More examples coming soon.")
        );
    }
}
