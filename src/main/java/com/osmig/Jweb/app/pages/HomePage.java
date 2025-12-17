package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import static com.osmig.Jweb.framework.elements.Elements.*;
import com.osmig.Jweb.app.layouts.MainLayout;
import com.osmig.Jweb.app.partials.Card;

/**
 * Home page template.
 */
public class HomePage implements Template {

    public HomePage() {
    }

    public static HomePage create() {
        return new HomePage();
    }

    @Override
    public Element render() {
        return new MainLayout("Home - JWeb",
        div(
            h1("Welcome to JWeb"),
            p(class_("lead"), "Build web apps in pure Java with templates!"),

            h2("Features"),
            new Card(
                "Templates",
                "Define reusable UI components in .jweb files"
            ),
            new Card(
                "Composable",
                "Templates can include other templates"
            ),
            new Card(
                "Type Safe",
                "Compile-time validation of template parameters"
            )
        )
    );
    }
}
