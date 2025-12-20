package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Homepage with hero and features.
 */
public class HomePage implements Template {

    @Override
    public Element render() {
        return div(
            hero(),
            features()
        );
    }

    private Element hero() {
        return section(attrs().style()
                .padding(rem(6), SP_8).textAlign(center)
                .prop("background", "linear-gradient(180deg, #f8fafc 0%, #fff 100%)")
            .done(),
            h1(attrs().style().fontSize(TEXT_4XL).fontWeight(800).color(TEXT).done(),
                text("Build Web Apps in Pure Java")),
            p(attrs().style().fontSize(TEXT_LG).color(TEXT_LIGHT)
                .maxWidth(px(600)).prop("margin", "1.5rem auto 0").done(),
                text("A lightweight framework for building modern web applications with type-safe syntax.")),
            a(attrs().href("/docs").style()
                .display(inlineBlock).marginTop(SP_6).padding(SP_3, SP_6)
                .backgroundColor(PRIMARY).color(white).borderRadius(ROUNDED)
                .fontWeight(600).textDecoration(none)
            .done(), text("Get Started"))
        );
    }

    private Element features() {
        return section(attrs().style().padding(SP_12, SP_8).maxWidth(px(900)).margin(zero, auto).done(),
            div(attrs().style().display(grid).prop("grid-template-columns", "repeat(3, 1fr)")
                .gap(SP_6).done(),
                feature("Type-Safe", "Compile-time checks"),
                feature("Lightweight", "No dependencies, just Java."),
                feature("Modern", "Fluent API with a clean syntax.")
            )
        );
    }

    private Element feature(String title, String desc) {
        return div(attrs().style().padding(SP_6).backgroundColor(hex("#f8fafc"))
            .borderRadius(ROUNDED_LG).textAlign(center).done(),
            h3(attrs().style().fontSize(TEXT_LG).fontWeight(600).color(TEXT).done(), text(title)),
            p(attrs().style().fontSize(TEXT_SM).color(TEXT_LIGHT).marginTop(SP_2).done(), text(desc))
        );
    }
}
