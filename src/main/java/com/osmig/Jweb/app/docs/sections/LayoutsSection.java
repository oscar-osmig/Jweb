package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class LayoutsSection {
    private LayoutsSection() {}

    public static Element render() {
        return section(
            title("Layouts"),
            text("Page layouts wrap content with common structure."),

            subtitle("Basic Layout"),
            code("""
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
                            body(attrs().style()
                                    .display(flex)
                                    .flexDirection(column)
                                    .minHeight(vh(100)).done(),
                                new Nav().render(),
                                main(content),
                                new Footer().render()
                            )
                        );
                    }
                }"""),

            subtitle("Using Layouts"),
            code("""
                // In Routes.java
                app.get("/about", ctx ->
                    new Layout("About", new AboutPage().render()).render()
                );"""),

            subtitle("Theme Design Tokens"),
            code("""
                public final class Theme {
                    // Colors
                    public static final CSSValue PRIMARY = hex("#6366f1");
                    public static final CSSValue TEXT = hex("#1e293b");

                    // Spacing
                    public static final CSSValue SP_4 = rem(1);
                    public static final CSSValue SP_8 = rem(2);

                    // Typography
                    public static final CSSValue TEXT_LG = rem(1.125);
                }

                // Usage
                div(attrs().style().color(PRIMARY).padding(SP_4).done())""")
        );
    }
}
