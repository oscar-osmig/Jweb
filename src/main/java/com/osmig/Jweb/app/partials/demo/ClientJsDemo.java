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
import com.osmig.Jweb.framework.styles.CSSValue;

/**
 * Demo showcasing client-side JavaScript using the JS DSL.
 */
public class ClientJsDemo implements Template {

    @Override
    public Element render() {
        return DemoCard.wrap("Client-Side JS", "\u26A1",
            div(attrs().style().textAlign(center).marginBottom(SPACE_MD).done(),
                span(attrs().id("timer-display")
                    .style().fontSize(rem(2.5)).fontWeight(700).color(PRIMARY)
                        .fontFamily("monospace").done(),
                    text("00:00:00"))
            ),
            div(attrs().style().display(flex).gap(SPACE_SM).justifyContent(center).done(),
                button(attrs()
                    .id("start-btn")
                    .set("onclick", "startTimer()")
                    .style(btnStyle(hex("#10b981"))),
                    text("Start")),
                button(attrs()
                    .id("stop-btn")
                    .set("onclick", "stopTimer()")
                    .style(btnStyle(hex("#ef4444"))),
                    text("Stop")),
                button(attrs()
                    .id("reset-btn")
                    .set("onclick", "resetTimer()")
                    .style(btnStyle(BG_LIGHT).color(TEXT_LIGHT)),
                    text("Reset"))
            ),
            inlineScript(timerScript())
        );
    }

    private String timerScript() {
        // Functions defined separately - clean and readable
        Func formatTime = func("formatTime", "seconds")
            .var_("hrs", floor(variable("seconds").div(3600)))
            .var_("mins", floor(variable("seconds").mod(3600).div(60)))
            .var_("secs", variable("seconds").mod(60))
            .ret(variable("hrs").padStart(2, "0")
                .plus(":")
                .plus(variable("mins").padStart(2, "0"))
                .plus(":")
                .plus(variable("secs").padStart(2, "0")));

        Func updateDisplay = func("updateDisplay")
            .set(getElem("timer-display").text(), call("formatTime", variable("timerSeconds")));

        Func startTimer = func("startTimer")
            .if_(variable("timerRunning"), ret())
            .set("timerRunning", true)
            .set("timerInterval", setInterval(
                callback().inc("timerSeconds").call("updateDisplay"),
                1000
            ));

        Func stopTimer = func("stopTimer")
            .set("timerRunning", false)
            .if_(variable("timerInterval"),
                clearInterval(variable("timerInterval")))
            .set("timerInterval", null_());

        Func resetTimer = func("resetTimer")
            .call("stopTimer")
            .set("timerSeconds", 0)
            .call("updateDisplay");

        // Assemble script
        return script()
            .var_("timerSeconds", 0)
            .var_("timerInterval", null_())
            .var_("timerRunning", false)
            .add(formatTime)
            .add(updateDisplay)
            .add(startTimer)
            .add(stopTimer)
            .add(resetTimer)
            .build();
    }

    private StyleBuilder btnStyle(CSSValue bgColor) {
        return style()
            .padding(SPACE_SM, SPACE_LG)
            .fontSize(FONT_SM)
            .fontWeight(600)
            .backgroundColor(bgColor)
            .color(white)
            .border(px(0), solid, transparent)
            .borderRadius(RADIUS_MD)
            .cursor(pointer)
            .transition(propTransform, ms(150), ease);
    }
}
