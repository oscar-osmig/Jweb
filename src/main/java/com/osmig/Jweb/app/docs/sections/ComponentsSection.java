package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.components.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ComponentsSection {
    private ComponentsSection() {}

    public static Element render() {
        return section(
            docTitle("Components"),
            para("Components are reusable UI pieces that implement the Template interface. " +
                 "They encapsulate structure, styling, and behavior into self-contained units."),

            docSubtitle("Overview"),
            para("Create a component by implementing Template and its render() method. " +
                 "Pass data via constructor parameters."),
            codeBlock("""
public class MyComponent implements Template {
    private final String prop;

    public MyComponent(String prop) {
        this.prop = prop;
    }

    public Element render() {
        return div(text(prop));
    }
}

// Usage: new MyComponent("Hello")"""),

            CompBasics.render(),
            CompProps.render(),
            CompChildren.render(),
            CompComposition.render()
        );
    }
}
