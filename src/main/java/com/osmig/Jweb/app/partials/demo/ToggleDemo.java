package com.osmig.Jweb.app.partials.demo;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.elements.Elements;
import com.osmig.Jweb.framework.js.JS.Func;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.app.Theme.*;
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.js.JS.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

/**
 * Toggle/switch component - pure client-side JS.
 */
public class ToggleDemo implements Template {

    @Override
    public Element render() {
        return DemoCard.wrap("Toggle", "\uD83D\uDD18",
            Elements.style(toggleCss()),
            div(attrs().style().textAlign(center).done(),
                p(attrs().style().marginBottom(SPACE_MD).fontSize(FONT_LG).color(TEXT_LIGHT).done(),
                    text("Status: "),
                    span(attrs().id("toggle-status")
                        .style().fontWeight(600).done(),
                        text("OFF"))
                ),
                button(attrs()
                    .id("toggle-btn")
                    .class_("toggle-btn")
                    .set("onclick", "toggle()"),
                    div(attrs().class_("toggle-knob"))
                )
            ),
            inlineScript(toggleScript())
        );
    }

    private String toggleScript() {
        Func toggle = func("toggle")
            .set("toggleState", variable("toggleState").not())
            .call("updateToggle");

        Func updateToggle = func("updateToggle")
            .if_(variable("toggleState")).then_(
                elem("toggle-btn").dot("classList").dot("add").invoke(str("toggle-on")),
                elem("toggle-status").text().assign(str("ON"))
            ).else_(
                elem("toggle-btn").dot("classList").dot("remove").invoke(str("toggle-on")),
                elem("toggle-status").text().assign(str("OFF"))
            );

        return script()
            .var_("toggleState", false)
            .add(updateToggle)
            .add(toggle)
            .build();
    }

    private String toggleCss() {
        return styles(
            rule(".toggle-btn")
                .width(px(60))
                .height(px(32))
                .borderRadius(px(9999))
                .backgroundColor(hex("#ccc"))
                .border(none)
                .cursor(pointer)
                .position(relative)
                .transition("background-color", ms(200), ease)
                .padding(zero),
            rule(".toggle-btn.toggle-on")
                .backgroundColor(hex("#10b981")),
            rule(".toggle-knob")
                .width(px(26))
                .height(px(26))
                .borderRadius(px(9999))
                .backgroundColor(white)
                .position(absolute)
                .top(px(3))
                .left(px(3))
                .transition("left", ms(200), ease)
                .boxShadow(px(0), px(2), px(4), rgba(0, 0, 0, 0.2)),
            rule(".toggle-btn.toggle-on .toggle-knob")
                .left(px(31))
        );
    }
}
