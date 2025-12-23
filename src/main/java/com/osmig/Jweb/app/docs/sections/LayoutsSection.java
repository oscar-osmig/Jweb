package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class LayoutsSection {
    private LayoutsSection() {}

    public static Element render() {
        return section(
            docTitle("Layouts"),
            para("Page layouts wrap content with common structure (nav, footer)."),

            docSubtitle("Basic Layout"),
            codeBlock("""
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
// In Routes.java - set default layout
app.layout(Layout.class);

// Pages automatically wrapped
app.pages(
    "/", HomePage.class,
    "/about", AboutPage.class
);"""),

            docSubtitle("Theme Tokens"),
            codeBlock("""
public final class Theme {
    // Colors
    public static final CSSValue PRIMARY = hex("#6366f1");
    public static final CSSValue TEXT = hex("#1e293b");

    // Spacing
    public static final CSSValue SP_4 = rem(1);
    public static final CSSValue SP_8 = rem(2);
}

// Usage
div(attrs().style().color(PRIMARY).padding(SP_4).done())"""),

            docTip("Define design tokens in Theme.java for consistent styling across your app.")
        );
    }
}
