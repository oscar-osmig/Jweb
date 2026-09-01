package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class IntroSection {
    private IntroSection() {}

    public static Element render() {
        return section(
            docTitle("Introduction"),
            para("JWeb is a Java web framework for building complete web applications " +
                 "entirely in Java. No direct HTML, CSS, or JavaScript writing - just Java."),

            docSubtitle("Why JWeb?"),
            docList(
                "HTML DSL - Java methods that generate HTML with compile-time safety",
                "CSS DSL - Java fluent methods that generate CSS with full IDE support",
                sinceText("v2.1.0",
                    "Three DSL - declarative 3D scenes rendered with three.js, "
                    + "with one-call shadows and clickable objects"),
                "No npm/webpack - Just Maven, like any Java project",
                "Spring Boot powered - Production-ready from day one",
                "Reactive state - Built-in state management for dynamic UIs"
            ),

            docTip("JWeb uses static imports for a clean DSL. Add: import static jweb.El.*;"),

            since("v2.1.0",
                docTip("New in v2.1.0: declarative 3D scenes — declare a three.js scene "
                       + "graph like HTML and let the runtime own the ceremony. "
                       + "See the 3D Scenes section.")),

            docSubtitle("Hello World"),
            codeBlock("""
public class HelloPage implements Template {
    public Element render() {
        return h1("Hello, World!");
    }
}"""),

            docSubtitle("With Styling"),
            codeBlock("""
public Element render() {
    return div(style()
            .padding(rem(2))
            .backgroundColor(hex("#f5f5f5")),
        h1("Welcome to JWeb"),
        p("Build web apps in pure Java")
    );
}"""),

            docSubtitle("How It Works"),
            para("JWeb renders Java objects to HTML on the server. The HTML DSL provides " +
                 "methods that generate markup. The CSS DSL uses fluent builders for styles.")
        );
    }
}
