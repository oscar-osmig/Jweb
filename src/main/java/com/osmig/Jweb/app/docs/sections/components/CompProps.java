package com.osmig.Jweb.app.docs.sections.components;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CompProps {
    private CompProps() {}

    public static Element render() {
        return section(
            h3Title("Props via Constructor"),
            para("Pass data to components through constructor parameters."),
            codeBlock("""
public class UserCard implements Template {
    private final User user;
    private final boolean showEmail;

    public UserCard(User user) {
        this(user, true);
    }

    public UserCard(User user, boolean showEmail) {
        this.user = user;
        this.showEmail = showEmail;
    }

    public Element render() {
        return div(attrs().class_("user-card"),
            img(attrs().src(user.getAvatar()).alt(user.getName())),
            h3(user.getName()),
            when(showEmail, () -> p(user.getEmail())),
            when(user.isAdmin(), () -> badge("Admin"))
        );
    }
}

// Usage
new UserCard(user)
new UserCard(user, false)  // Hide email"""),

            h3Title("Builder Pattern"),
            para("For components with many optional props."),
            codeBlock("""
public class Button implements Template {
    private final String text;
    private String variant = "primary";
    private String size = "medium";
    private boolean disabled = false;

    public Button(String text) { this.text = text; }

    public Button variant(String v) { variant = v; return this; }
    public Button size(String s) { size = s; return this; }
    public Button disabled() { disabled = true; return this; }

    public Element render() {
        return button(attrs()
            .class_("btn btn-" + variant + " btn-" + size)
            .disabled(disabled),
            text(text));
    }
}

// Usage
new Button("Submit")
new Button("Cancel").variant("secondary")
new Button("Save").size("large").disabled()""")
        );
    }
}
