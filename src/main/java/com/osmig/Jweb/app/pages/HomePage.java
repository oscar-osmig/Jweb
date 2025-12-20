package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSGrid.*;
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
                .padding(rem(6), SP_8)
                .textAlign(center)
                .background(linearGradient(deg(180), hex("#f8fafc"), white))
            .done(),
            h1(attrs().style()
                    .fontSize(TEXT_4XL)
                    .fontWeight(800)
                    .color(TEXT)
                .done(),
                text("Java Web Framework")),
            p(attrs().style()
                    .fontSize(TEXT_LG)
                    .color(TEXT_LIGHT)
                    .maxWidth(px(600))
                    .margin(rem(1.5), auto, zero)
                .done(),
                text("Build complete web applications entirely in Java. Type-safe components, fluent APIs, and zero frontend tooling required.")),
            a(attrs().href("/docs").style()
                    .display(inlineBlock)
                    .marginTop(SP_6)
                    .padding(SP_3, SP_6)
                    .background(linearGradient("90deg",
                        hex("#6366f1"), hex("#8b5cf6"), hex("#a855f7"),
                        hex("#ec4899"), hex("#8b5cf6"), hex("#6366f1")))
                    .backgroundSize(() -> "300% 100%")
                    .prop("animation", "gradientShift 8s ease infinite")
                    .color(white)
                    .borderRadius(ROUNDED)
                    .fontWeight(600)
                    .textDecoration(none)
                .done(),
                text("Get Started"))
        );
    }

    private Element features() {
        return section(attrs().style()
                .padding(SP_12, SP_8)
                .maxWidth(px(900))
                .margin(zero, auto)
            .done(),
            div(attrs().style()
                    .display(grid)
                    .gridTemplateColumns(repeat(3, fr(1)))
                    .gap(SP_6)
                .done(),
                feature("Type-Safe", "Compile-time verified components and routes"),
                feature("Pure Java", "No templates, no build tools, just Maven"),
                feature("Component-Based", "Reusable, composable UI components")
            )
        );
    }

    private Element feature(String title, String desc) {
        return div(attrs().style()
                .padding(SP_6)
                .backgroundColor(hex("#f8fafc"))
                .borderRadius(ROUNDED_LG)
                .textAlign(center)
            .done(),
            h3(attrs().style()
                    .fontSize(TEXT_LG)
                    .fontWeight(600)
                    .color(TEXT)
                .done(),
                text(title)),
            p(attrs().style()
                    .fontSize(TEXT_SM)
                    .color(TEXT_LIGHT)
                    .marginTop(SP_2)
                .done(),
                text(desc))
        );
    }
}
