package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class IntroSection {
    private IntroSection() {}

    public static Element render() {
        return section(
            title("Introduction"),
            text("Build complete web applications entirely in Java. Type-safe components, " +
                 "fluent styling, and zero frontend tooling."),

            subtitle("Quick Example"),
            code("""
                // A complete page in JWeb
                public class HelloPage implements Template {
                    public Element render() {
                        return div(
                            h1("Hello, World!"),
                            p("Built with JWeb")
                        );
                    }
                }"""),

            subtitle("Features"),
            list(
                "Type-safe HTML with compile-time verification",
                "Fluent CSS with IDE autocomplete",
                "Reactive state management",
                "No webpack, npm, or build tools - just Maven"
            )
        );
    }
}
