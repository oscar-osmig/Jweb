package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.app.layouts.MainLayout;
import com.osmig.Jweb.app.partials.demo.*;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.app.Theme.*;
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

/**
 * Interactive demo page showcasing JWeb's state management.
 */
public class DemoPage implements Template {

    @Override
    public Element render() {
        return new MainLayout("Interactive Demo - JWeb",
            div(attrs().style()
                    .maxWidth(px(900))
                    .margin(zero, auto)
                    .padding(SPACE_XL)
                .done(),
                // Page header
                header(),
                // Demo components grid
                div(attrs().style()
                        .display(grid)
                        .gridTemplateColumns("repeat(auto-fit, minmax(280px, 1fr))")
                        .gap(SPACE_LG)
                        .marginTop(SPACE_XL)
                    .done(),
                    new CounterDemo().render(),
                    new ToggleDemo().render(),
                    new InputDemo().render(),
                    new ClientJsDemo().render()
                )
            )
        );
    }

    private Element header() {
        return div(attrs().style().textAlign(center).marginBottom(SPACE_LG).done(),
            h1(attrs().class_("gradient-text").style()
                .fontSize(FONT_2XL).fontWeight(700).marginBottom(SPACE_SM)
            .done(), text("Interactive Demo")),
            p(attrs().style().color(TEXT_LIGHT).fontSize(FONT_LG).done(),
                text("Experience JWeb's reactive state management"))
        );
    }
}
