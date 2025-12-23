package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsMedia {
    private ElementsMedia() {}

    public static Element render() {
        return section(
            h3Title("Images"),
            para("Display images with required alt text for accessibility."),
            codeBlock("""
// Basic image
img(attrs().src("/images/logo.png").alt("Company Logo"))

// Responsive image
img(attrs()
    .src("/images/hero.jpg")
    .alt("Hero image")
    .class_("responsive")
    .loading("lazy"))

// Figure with caption
figure(
    img(attrs().src("/images/chart.png").alt("Sales chart")),
    figcaption("Q4 2024 Sales Performance")
)"""),

            h3Title("Links"),
            para("Create anchor links for navigation."),
            codeBlock("""
// Internal link
a(attrs().href("/about"), text("About Us"))

// External link (opens in new tab)
a(attrs()
    .href("https://github.com")
    .target("_blank")
    .rel("noopener noreferrer"),
    text("GitHub"))

// Download link
a(attrs()
    .href("/files/report.pdf")
    .download("report.pdf"),
    text("Download Report"))

// Email link
a(attrs().href("mailto:contact@example.com"), text("Email Us"))""")
        );
    }
}
