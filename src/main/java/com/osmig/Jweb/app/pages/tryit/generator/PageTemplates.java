package com.osmig.Jweb.app.pages.tryit.generator;

/** Generates page component files for the starter project. */
public final class PageTemplates {
    private PageTemplates() {}

    public static String homePage() {
        return """
            package com.jweb.app.pages;

            import com.jweb.framework.core.Element;
            import com.jweb.framework.template.Template;
            import static com.jweb.framework.elements.Elements.*;
            import static com.jweb.framework.styles.CSS.*;
            import static com.jweb.framework.styles.CSSUnits.*;
            import static com.jweb.framework.styles.CSSColors.*;

            public class HomePage implements Template {

                @Override
                public Element render() {
                    return div(attrs().style()
                            .display(flex).flexDirection(column).justifyContent(center).alignItems(center)
                            .flex(num(1)).textAlign(center).padding(rem(2))
                        .done(),
                        h1(attrs().style()
                                .fontSize(rem(3)).fontWeight(700).color(hex("#1e293b")).marginBottom(rem(1))
                            .done(),
                            text("Welcome to JWeb")),
                        p(attrs().style()
                                .fontSize(rem(1.25)).color(hex("#64748b")).maxWidth(px(500))
                            .done(),
                            text("Build modern web applications entirely in Java. Type-safe, simple, powerful."))
                    );
                }
            }
            """;
    }
}
