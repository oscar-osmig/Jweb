package com.osmig.Jweb.app.pages.tryit.generator;

/** Generates page component files for the starter project. */
public final class PageTemplates {
    private PageTemplates() {}

    public static String homePage() {
        return """
            package com.example.app.pages;

            import com.example.framework.core.Element;
            import com.example.framework.state.State;
            import com.example.framework.template.Template;
            import static com.example.framework.elements.Elements.*;
            import static com.example.framework.styles.CSS.*;
            import static com.example.framework.styles.CSSUnits.*;
            import static com.example.framework.styles.CSSColors.*;

            public class HomePage implements Template {
                private final State<Integer> count = State.of(0);

                @Override
                public Element render() {
                    return div(attrs().style().maxWidth(px(800)).margin(zero, auto).textAlign(center).done(),
                        h1(attrs().style().fontSize(rem(2.5)).marginBottom(rem(1)).done(),
                            text("Welcome to JWeb!")),
                        p(attrs().style().color(gray(600)).marginBottom(rem(2)).done(),
                            text("Build web applications in pure Java")),
                        counterSection(),
                        gettingStartedSection()
                    );
                }

                private Element counterSection() {
                    return div(attrs().style().padding(rem(2)).backgroundColor(gray(50)).borderRadius(px(8)).done(),
                        h2("Interactive Counter"),
                        p(attrs().style().fontSize(rem(3)).fontWeight(700).done(),
                            text(String.valueOf(count.get()))),
                        div(attrs().style().display(flex).gap(rem(1)).justifyContent(center).done(),
                            button(attrs().onClick(e -> count.update(n -> n - 1))
                                .style().padding(rem(0.5), rem(1.5)).fontSize(rem(1.25)).cursor(pointer).done(),
                                text("-")),
                            button(attrs().onClick(e -> count.update(n -> n + 1))
                                .style().padding(rem(0.5), rem(1.5)).fontSize(rem(1.25)).cursor(pointer).done(),
                                text("+"))
                        )
                    );
                }

                private Element gettingStartedSection() {
                    return div(attrs().style().marginTop(rem(3)).done(),
                        h3("Get Started"),
                        ul(attrs().style().textAlign(left).maxWidth(px(400)).margin(zero, auto).done(),
                            li("Edit ", code("src/main/java/com/example/app/pages/HomePage.java")),
                            li("Create new pages in the ", code("pages"), text(" folder")),
                            li("Add routes in ", code("Routes.java")),
                            li("Check out the JWeb documentation")
                        )
                    );
                }
            }
            """;
    }
}
