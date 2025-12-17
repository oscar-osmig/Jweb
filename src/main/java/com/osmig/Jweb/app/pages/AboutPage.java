package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import static com.osmig.Jweb.framework.elements.Elements.*;
import com.osmig.Jweb.app.layouts.MainLayout;

/**
 * About page template.
 */
public class AboutPage implements Template {

    public AboutPage() {
    }

    public static AboutPage create() {
        return new AboutPage();
    }

    @Override
    public Element render() {
        return new MainLayout("About - JWeb",
        div(
            h1("About JWeb"),
            p("JWeb is a Java web framework that lets you build web applications entirely in Java."),

            h2("Why JWeb?"),
            ul(
                li("No JavaScript required"),
                li("Type-safe templates"),
                li("Component-based architecture"),
                li("Easy to learn and use")
            ),

            p(a("/", "\u2190 Back Home"))
        )
    );
    }
}
