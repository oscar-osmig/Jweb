package com.osmig.Jweb.app.docs.sections.routing;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingBasics {
    private RoutingBasics() {}

    public static Element render() {
        return section(
            h3Title("Page Routes"),
            para("Register page classes that implement Template interface."),
            codeBlock("""
@Component
public class Routes implements JWebRoutes {
    public void configure(JWeb app) {
        app.pages(
            "/", HomePage.class,
            "/about", AboutPage.class,
            "/contact", ContactPage.class,
            "/pricing", PricingPage.class
        );
    }
}"""),

            h3Title("Page Template"),
            para("Each page is a class implementing Template."),
            codeBlock("""
public class HomePage implements Template {
    public Element render() {
        return main(
            section(
                h1("Welcome to JWeb"),
                p("Build web apps in pure Java")
            )
        );
    }
}

public class AboutPage implements Template {
    public Element render() {
        return main(
            h1("About Us"),
            p("We build Java web frameworks")
        );
    }
}""")
        );
    }
}
