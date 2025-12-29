package com.osmig.Jweb.app.pages.tryit.generator;

/** Generates layout component files for the starter project. */
public final class LayoutTemplates {
    private LayoutTemplates() {}

    public static String layout() {
        return """
            package com.example.app.layout;

            import com.example.framework.core.Element;
            import com.example.framework.dev.DevServer;
            import com.example.framework.template.Template;
            import static com.example.framework.elements.Elements.*;
            import static com.example.framework.styles.CSS.*;
            import static com.example.framework.styles.CSSUnits.*;
            import static com.example.framework.styles.CSSColors.*;

            public class Layout implements Template {
                private final String title;
                private final Element content;

                public Layout(String title, Element content) {
                    this.title = title;
                    this.content = content;
                }

                @Override
                public Element render() {
                    return html(
                        head(
                            meta(attrs().charset("UTF-8")),
                            meta(attrs().name("viewport").content("width=device-width, initial-scale=1")),
                            title(title),
                            style(globalStyles())
                        ),
                        body(attrs().style().margin(zero).fontFamily("system-ui, sans-serif")
                                .height(vh(100)).display(flex).flexDirection(column).overflow(hidden).done(),
                            new Nav().render(),
                            main(attrs().style().flex(num(1)).display(flex).done(), content),
                            new Footer().render(),
                            DevServer.script()
                        )
                    );
                }

                private String globalStyles() {
                    return styles(
                        rule("*").boxSizing(borderBox),
                        rule("a").textDecoration(none),
                        rule("a:hover").opacity(0.8)
                    );
                }
            }
            """;
    }

    public static String nav() {
        return """
            package com.example.app.layout;

            import com.example.framework.core.Element;
            import com.example.framework.template.Template;
            import static com.example.framework.elements.Elements.*;
            import static com.example.framework.styles.CSS.*;
            import static com.example.framework.styles.CSSUnits.*;
            import static com.example.framework.styles.CSSColors.*;

            public class Nav implements Template {
                @Override
                public Element render() {
                    return nav(attrs().style()
                            .backgroundColor(hex("#0ea5e9")).padding(rem(1), rem(2))
                            .display(flex).alignItems(center).justifyContent(spaceBetween).done(),
                        a(attrs().href("/").style().color(white).fontSize(rem(1.25)).fontWeight(700).done(),
                            text("My App")),
                        a(attrs().href("/").style().color(rgba(255, 255, 255, 0.9)).done(),
                            text("Home"))
                    );
                }
            }
            """;
    }

    public static String footer() {
        return """
            package com.example.app.layout;

            import com.example.framework.core.Element;
            import com.example.framework.template.Template;
            import static com.example.framework.elements.Elements.*;
            import static com.example.framework.styles.CSS.*;
            import static com.example.framework.styles.CSSUnits.*;
            import static com.example.framework.styles.CSSColors.*;

            public class Footer implements Template {
                @Override
                public Element render() {
                    return footer(attrs().style()
                            .backgroundColor(hex("#f0f9ff")).padding(rem(1), rem(2))
                            .textAlign(center).fontSize(rem(0.875)).color(hex("#0369a1")).done(),
                        p("Built with JWeb Framework")
                    );
                }
            }
            """;
    }
}
