package com.osmig.Jweb.app.partials.demo;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.js.JS.Func;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.app.Theme.*;
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.js.JS.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import com.osmig.Jweb.framework.styles.CSS.StyleBuilder;

/**
 * Live input binding - pure client-side JS.
 */
public class InputDemo implements Template {

    @Override
    public Element render() {
        return DemoCard.wrap("Live Input", "\u270D\uFE0F",
            div(attrs().style().marginBottom(SPACE_MD).padding(SPACE_MD)
                    .backgroundColor(BG_LIGHT).borderRadius(RADIUS_MD).done(),
                p(attrs().style().fontSize(FONT_LG).color(TEXT).done(),
                    text("Hello, "),
                    span(attrs().id("name-display")
                        .style().fontWeight(600).color(PRIMARY).done(),
                        text("World")),
                    text("!"))
            ),
            input(attrs()
                .id("name-input")
                .type("text")
                .value("World")
                .placeholder("Enter your name")
                .set("oninput", "updateName()")
                .style(inputStyle())),
            inlineScript(inputScript())
        );
    }

    private String inputScript() {
        Func updateName = func("updateName")
            .var_("val", elem("name-input").value())
            .set(elem("name-display").text(),
                variable("val").eq("").ternary(str("..."), variable("val")));

        return script()
            .add(updateName)
            .build();
    }

    private StyleBuilder inputStyle() {
        return style().width(percent(100)).padding(SPACE_SM, SPACE_MD)
            .fontSize(FONT_BASE).border(px(1), solid, BORDER)
            .borderRadius(RADIUS_MD).backgroundColor(white);
    }
}
