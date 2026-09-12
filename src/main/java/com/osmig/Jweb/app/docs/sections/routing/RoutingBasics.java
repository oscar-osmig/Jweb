package com.osmig.Jweb.app.docs.sections.routing;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingBasics {
    private RoutingBasics() {}

    public static Element render() {
        return section(
            h3Title("Page Routes"),
            para("Register page classes that implement Template interface. A path may carry " +
                 ":param segments and a * wildcard."),
            codeBlock("""
import jweb.api.Component;

record User(String name) {}
class Users { User find(long id) { return new User("Guest"); } }
private Users users = new Users();
class HomePage implements Template { public Element render() { return div(); } }
class AboutPage implements Template { public Element render() { return div(); } }
class ContactPage implements Template { public Element render() { return div(); } }
class DocsPage implements Template { public Element render() { return div(); } }

@Component
public class Routes implements JWebRoutes {
    public void configure(JWeb app) {
        app.pages(
            "/", HomePage.class,
            "/about", AboutPage.class,
            "/contact", ContactPage.class,
            "/users/:id", UserPage.class,
            "/docs/*", DocsPage.class
        );
    }
}

// The page reads its params in beforeRender, or through pathParam()
public class UserPage implements Template {
    private User user;

    public void beforeRender(Request req) {
        user = users.find(req.requireParamLong("id"));
    }

    public Element render() {
        return div(h1(user.name()), p("Profile " + pathParam("id")));
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
