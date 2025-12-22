package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ComponentsSection {
    private ComponentsSection() {}

    public static Element render() {
        return section(
            title("Components"),
            text("Reusable UI pieces that implement the Template interface."),

            subtitle("Basic Component"),
            code("""
                public class Card implements Template {
                    private final String title;
                    private final String content;

                    public Card(String title, String content) {
                        this.title = title;
                        this.content = content;
                    }

                    public Element render() {
                        return div(attrs().style()
                                .padding(rem(1.5))
                                .backgroundColor(white)
                                .borderRadius(px(8)).done(),
                            h3(title),
                            p(content)
                        );
                    }
                }"""),

            subtitle("Using Components"),
            code("""
                div(
                    new Card("Welcome", "Hello World!"),
                    new Card("Features", "Build apps in pure Java")
                )"""),

            subtitle("Component with Children"),
            code("""
                public class Panel implements Template {
                    private final Element[] children;

                    public Panel(Element... children) {
                        this.children = children;
                    }

                    public Element render() {
                        return div(attrs().class_("panel"),
                            fragment(children)
                        );
                    }
                }

                // Usage
                new Panel(h1("Title"), p("Content"), button("Action"))""")
        );
    }
}
