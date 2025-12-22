package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class IntroSection {
    private IntroSection() {}

    public static Element render() {
        return section(
            title("Introduction"),
            text("JWeb is a Java web framework for building complete web applications " +
                 "entirely in Java. No JavaScript, no templates, no build tools - just Java."),

            subtitle("Why JWeb?"),
            list(
                "Type-safe HTML - Compile-time verification, IDE autocomplete",
                "Fluent CSS - Write styles in Java with full IDE support",
                "No npm/webpack - Just Maven, like any Java project",
                "Spring Boot powered - Production-ready from day one",
                "Reactive state - Built-in state management for dynamic UIs"
            ),

            subtitle("Quick Example"),
            code("""
public class HomePage implements Template {
    public Element render() {
        return div(attrs().style()
                .padding(rem(2))
                .backgroundColor(hex("#f5f5f5")).done(),
            h1("Welcome to JWeb"),
            p("Build web apps in pure Java"),
            a(attrs().href("/docs"), text("Get Started"))
        );
    }
}"""),

            subtitle("How It Works"),
            text("JWeb renders Java objects to HTML on the server. Elements are " +
                 "type-safe methods that generate HTML. Styles are fluent builders " +
                 "that generate CSS. Routes map URLs to handlers that return elements.")
        );
    }
}
