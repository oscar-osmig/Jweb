package com.osmig.Jweb.app.docs.sections.components;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CompBasics {
    private CompBasics() {}

    public static Element render() {
        return section(
            h3Title("Basic Component"),
            para("Components implement the Template interface."),
            codeBlock("""
public class Card implements Template {
    private final String title;
    private final String content;

    public Card(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Element render() {
        return div(attrs().class_("card"),
            h3(title),
            p(content)
        );
    }
}

// Usage
new Card("Welcome", "Hello, world!")"""),

            h3Title("Using Components"),
            para("Components are used like any other element."),
            codeBlock("""
div(
    h1("Dashboard"),
    new Card("Users", "Manage your users"),
    new Card("Settings", "Configure the app"),
    new Card("Reports", "View analytics")
)

// In a list
List<Product> products = productService.findAll();
div(each(products, p ->
    new ProductCard(p)
))""")
        );
    }
}
