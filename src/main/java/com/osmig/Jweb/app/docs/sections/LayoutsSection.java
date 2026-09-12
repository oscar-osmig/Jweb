package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import com.osmig.Jweb.app.docs.sections.layouts.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class LayoutsSection {
    private LayoutsSection() {}

    public static Element render() {
        return section(
            docTitle("Layouts"),
            para("Page layouts wrap content with common structure (nav, footer)."),

            docSubtitle("Basic Layout"),
            codeBlock("""
class Nav implements Template { public Element render() { return nav("Nav"); } }
class Footer implements Template { public Element render() { return footer("Footer"); } }

public class Layout implements Template {
    private final String title;
    private final Element content;

    public Layout(String title, Element content) {
        this.title = title;
        this.content = content;
    }

    public Element render() {
        return html(
            head(title(title)),
            body(new Nav(), main(content), new Footer())
        );
    }
}"""),

            docSubtitle("Register Layout"),
            codeBlock("""
class Layout implements Template { public Element render() { return html(); } }
class HomePage implements Template { public Element render() { return div(); } }
class AboutPage implements Template { public Element render() { return div(); } }

// In Routes.java - set default layout
app.layout(Layout.class);

// Pages automatically wrapped
app.pages(
    "/", HomePage.class,
    "/about", AboutPage.class
);"""),

            docSubtitle("The layout's stylesheet"),
            para("A layout owns the global CSS: the reset, the design tokens, the "
                 + "document shell. Return it from styles() and the render puts it in "
                 + "<head> — no page has to remember to include it."),
            codeBlock("""
class Head implements Template {
    private final String title;
    Head(String title) { this.title = title; }
    public Element render() { return head(title(title)); }
}
class Nav implements Template { public Element render() { return nav("Nav"); } }
class Footer implements Template { public Element render() { return footer("Footer"); } }

public class Layout implements Template {
    private final String title = "My App";
    private final Element content = div("content");
    private final CSSValue TEXT = hex("#1e293b");
    private final CSSValue BG = hex("#ffffff");
    private final jweb.css.Theme TOKENS = jweb.css.Theme.light();

    @Override
    public Element render() {
        return html(new Head(title), body(new Nav(), main(content), new Footer()));
    }

    @Override
    public Stylesheet styles() {
        return stylesheet()
            .add(TOKENS)
            .rule("*, *::before, *::after", style().boxSizing(borderBox).margin(zero))
            .rule("body", style().color(TEXT).backgroundColor(BG));
    }
}"""),

            docSubtitle("Theme Tokens"),
            codeBlock("""
public final class Theme {

    public static final jweb.css.Theme TOKENS = jweb.css.Theme.light()
        .color("primary", hex("#6366f1"))
        .color("text",    hex("#1e293b"))
        .space("4",       rem(1))
        .space("8",       rem(2))
        .dark(jweb.css.Theme.dark().color("text", hex("#e2e8f0")));

    // The names pages use — each one a var() reference
    public static final CSSValue PRIMARY = jweb.css.Theme.color("primary");
    public static final CSSValue TEXT    = jweb.css.Theme.color("text");
    public static final CSSValue SP_4    = jweb.css.Theme.space("4");
    public static final CSSValue SP_8    = jweb.css.Theme.space("8");
}

// Usage — unchanged (import static ...Theme.* to drop the prefix)
div(style().color(Theme.PRIMARY).padding(Theme.SP_4))"""),

            docTip("Define design tokens in Theme.java for consistent styling across your app. "
                 + "As custom properties they are visible in devtools and swappable at runtime."),

            LayoutsI18n.render()
        );
    }
}
