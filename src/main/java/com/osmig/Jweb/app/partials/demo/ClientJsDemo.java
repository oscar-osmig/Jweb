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
import static com.osmig.Jweb.framework.styles.Styles.style;
import com.osmig.Jweb.framework.styles.Style;
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
                    .attr("onclick", "startTimer()")
                    .style(btnStyle(hex("#10b981"))),
                    text("Start")),
                button(attrs()
                    .id("stop-btn")
                    .attr("onclick", "stopTimer()")
                    .style(btnStyle(hex("#ef4444"))),
                    text("Stop")),
                button(attrs()
                    .id("reset-btn")
                    .attr("onclick", "resetTimer()")
                    .style(btnStyle(BG_LIGHT).color(TEXT_LIGHT)),
                    text("Reset"))
            ),
            inlineScript(timerScript())
        );
    }

    private String timerScript() {
        // Functions defined separately - clean and readable
        Func formatTime = func("formatTime", "seconds")
            .var_("hrs", floor(v("seconds").div(3600)))
            .var_("mins", floor(v("seconds").mod(3600).div(60)))
            .var_("secs", v("seconds").mod(60))
            .ret(v("hrs").padStart(2, "0")
                .plus(":")
                .plus(v("mins").padStart(2, "0"))
                .plus(":")
                .plus(v("secs").padStart(2, "0")));

        Func updateDisplay = func("updateDisplay")
            .set(el("timer-display").text(), call("formatTime", v("timerSeconds")));

        Func startTimer = func("startTimer")
            .if_(v("timerRunning"), ret())
            .set("timerRunning", true)
            .set("timerInterval", setInterval(
                callback().inc("timerSeconds").call("updateDisplay"),
                1000
            ));

        Func stopTimer = func("stopTimer")
            .set("timerRunning", false)
            .if_(v("timerInterval"),
                clearInterval(v("timerInterval")))
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

    private Style btnStyle(CSSValue bgColor) {
        return style()
            .padding(SPACE_SM, SPACE_LG)
            .fontSize(FONT_SM)
            .fontWeight(600)
            .backgroundColor(bgColor)
            .color(white)
            .border(px(0), solid, transparent)
            .borderRadius(RADIUS_MD)
            .cursor(pointer)
            .transition("transform", ms(150), ease);
    }
}
