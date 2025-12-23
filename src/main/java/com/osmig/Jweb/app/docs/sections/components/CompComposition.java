package com.osmig.Jweb.app.docs.sections.components;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CompComposition {
    private CompComposition() {}

    public static Element render() {
        return section(
            h3Title("Component Composition"),
            para("Build complex UIs by composing smaller components."),
            codeBlock("""
// Small, focused components
public class Avatar implements Template {
    private final String src, alt;
    public Avatar(String src, String alt) { this.src = src; this.alt = alt; }
    public Element render() {
        return img(attrs().src(src).alt(alt).class_("avatar"));
    }
}

public class UserName implements Template {
    private final String name;
    public UserName(String name) { this.name = name; }
    public Element render() {
        return span(attrs().class_("username"), text(name));
    }
}

// Composed component
public class UserBadge implements Template {
    private final User user;
    public UserBadge(User user) { this.user = user; }
    public Element render() {
        return div(attrs().class_("user-badge"),
            new Avatar(user.getAvatar(), user.getName()),
            new UserName(user.getName())
        );
    }
}"""),

            h3Title("Component Reuse"),
            codeBlock("""
// Reusable across the app
header(
    new Logo(),
    new Navigation(),
    new UserBadge(currentUser)
)

// In a list
div(each(comments, c ->
    div(
        new UserBadge(c.getAuthor()),
        p(c.getText()),
        new Timestamp(c.getCreatedAt())
    )
))""")
        );
    }
}
