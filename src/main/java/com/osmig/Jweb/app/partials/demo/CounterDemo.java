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
 * Interactive counter component - pure client-side JS.
 */
public class CounterDemo implements Template {

    @Override
    public Element render() {
        return DemoCard.wrap("Counter", "\uD83D\uDD22",
            div(attrs().style().textAlign(center).marginBottom(SPACE_MD).done(),
                span(attrs().id("counter-display")
                    .style().fontSize(rem(3)).fontWeight(700).color(PRIMARY).done(),
                    text("0"))
            ),
            div(attrs().style().display(flex).gap(SPACE_SM).justifyContent(center).done(),
                button(attrs()
                    .set("onclick", "decrement()")
                    .style(btnStyle()), text("-")),
                button(attrs()
                    .set("onclick", "resetCounter()")
                    .style(resetBtnStyle()), text("Reset")),
                button(attrs()
                    .set("onclick", "increment()")
                    .style(btnStyle()), text("+"))
            ),
            inlineScript(counterScript())
        );
    }

    private String counterScript() {
        Func updateDisplay = func("updateCounterDisplay")
            .set(getElem("counter-display").text(), variable("counterValue"));

        Func increment = func("increment")
            .inc("counterValue")
            .call("updateCounterDisplay");

        Func decrement = func("decrement")
            .dec("counterValue")
            .call("updateCounterDisplay");

        Func reset = func("resetCounter")
            .set("counterValue", 0)
            .call("updateCounterDisplay");

        return script()
            .var_("counterValue", 0)
            .add(updateDisplay)
            .add(increment)
            .add(decrement)
            .add(reset)
            .build();
    }

    private StyleBuilder btnStyle() {
        return style().padding(SPACE_SM, SPACE_LG).fontSize(FONT_LG).fontWeight(600)
            .backgroundColor(PRIMARY).color(white).border(px(0), solid, transparent)
            .borderRadius(RADIUS_MD).cursor(pointer);
    }

    private StyleBuilder resetBtnStyle() {
        return style().padding(SPACE_SM, SPACE_MD).fontSize(FONT_SM)
            .backgroundColor(BG_LIGHT).color(TEXT_LIGHT).border(px(0), solid, transparent)
            .borderRadius(RADIUS_MD).cursor(pointer);
    }
}
