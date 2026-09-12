package com.osmig.Jweb.app.docs.sections.components;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CompComposition {
    private CompComposition() {}

    public static Element render() {
        return section(
            h3Title("Component Composition"),
            para("Build complex UIs by composing smaller components."),
            codeBlock("""
record User(String getAvatar, String getName) {}

// Small, focused components
public class Avatar implements Template {
    private final String src, alt;
    public Avatar(String src, String alt) { this.src = src; this.alt = alt; }
    public Element render() {
        return img(src(src), alt(alt), class_("avatar"));
    }
}

public class UserName implements Template {
    private final String name;
    public UserName(String name) { this.name = name; }
    public Element render() {
        return span(class_("username"), name);
    }
}

// Composed component
public class UserBadge implements Template {
    private final User user;
    public UserBadge(User user) { this.user = user; }
    public Element render() {
        return div(class_("user-badge"),
            new Avatar(user.getAvatar(), user.getName()),
            new UserName(user.getName())
        );
    }
}"""),

            h3Title("Component Reuse"),
            codeBlock("""
record User(String getAvatar, String getName) {}
class Logo implements Template { public Element render() { return img("/logo.png", "Logo"); } }
class Navigation implements Template { public Element render() { return nav(a(href("/"), "Home")); } }
record UserBadge(User user) implements Template {
    public Element render() { return div(class_("user-badge"), user.getName()); }
}
User currentUser = new User("/a.png", "Ada");
record Comment(User getAuthor, String getText, String getCreatedAt) {}
List<Comment> comments = List.of(new Comment(currentUser, "Nice!", "2026-01-01"));
record Timestamp(String value) implements Template {
    public Element render() { return span(class_("timestamp"), value); }
}

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
