package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSGrid.*;
import static com.osmig.Jweb.app.layout.Theme.*;

public class HomePage implements Template {

    @Override
    public Element render() {
        return div(style()
                .display(flex).flexDirection(column).justifyContent(center).alignItems(center)
                .flex(num(1)).padding(SP_8),
            hero(),
            features()
        );
    }

    private Element hero() {
        return section(style().textAlign(center).marginBottom(SP_8),
            h1(style().fontSize(TEXT_4XL).fontWeight(800).color(TEXT),
                text("Java Web Framework")),
            p(style()
                .fontSize(TEXT_LG).color(TEXT_LIGHT)
                .maxWidth(px(600)).margin(rem(1.5), auto, zero),
                text("Build complete web applications entirely in Java. " +
                     "Type-safe components, fluent DSL, and zero frontend tooling required.")),
            a(attrs().href("/docs").style()
                .display(inlineBlock).marginTop(SP_6).padding(SP_3, SP_6)
                .apply(brandFlow())
                .color(white).borderRadius(ROUNDED).fontWeight(600).textDecoration(none)
            .done(), text("Get Started"))
        );
    }

    private Element features() {
        return section(style().maxWidth(px(1000)).margin(zero, auto),
            div(style().display(grid).gridTemplateColumns(repeat(3, fr(1))).gap(SP_6),
                feature("Type-Safe", "Compile-time verified components and routes"),
                feature("Pure Java", "No templates, no build tools, just Maven"),
                feature("Component-Based", "Reusable, composable UI components")
            )
        );
    }

    private Element feature(String title, String desc) {
        return div(style()
                .position(relative)
                .padding(SP_6)
                .backgroundColor(white)
                .borderRadius(ROUNDED_LG)
                .textAlign(center)
                .overflow(hidden),
            brandBorder(ROUNDED_LG),
            div(style().position(relative).zIndex(1),
                h2(style().fontSize(TEXT_LG).fontWeight(600).color(PRIMARY),
                    text(title)),
                p(style().fontSize(TEXT_SM).color(TEXT_LIGHT).marginTop(SP_2),
                    text(desc))
            )
        );
    }
}
