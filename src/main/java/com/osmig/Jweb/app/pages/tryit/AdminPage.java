package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.pages.tryit.TryItTheme.*;
import static com.osmig.Jweb.app.pages.tryit.AdminComponents.*;

/** Admin page for managing access requests. */
public class AdminPage implements Template {

    @Override
    public Element render() {
        return div(attrs().style().width(percent(100)).flex(num(1)).display(flex).flexDirection(column)
            .backgroundColor(GRAY_50).overflow(hidden).done(),
            adminContent(), loginModal(), actionModal(),
            script("https://cdn.jsdelivr.net/npm/@emailjs/browser@4/dist/email.min.js"),
            inlineScript("if(typeof emailjs!=='undefined')emailjs.init('jBoWfcyb20GACTvti');"),
            inlineScript(AdminScripts.adminHandlers())
        );
    }

    private Element adminContent() {
        // Hidden by default, shown after successful login
        return div(attrs().id("admin-content").style().display(none).flexDirection(column).flex(num(1)).done(),
            pageHeader(), tabs(), requestsContainer()
        );
    }

    private Element pageHeader() {
        return div(attrs().style().padding(rem(1.5), rem(2)).backgroundColor(GRAY_50)
            .borderBottom(px(1), solid, GRAY_200).flexShrink(0).done(),
            div(attrs().style().maxWidth(px(1200)).margin(zero, auto).done(),
                h1(attrs().style().fontSize(rem(1.75)).fontWeight(700).color(GRAY_900).margin(zero).done(),
                    text("Access Request Management")),
                p(attrs().style().color(GRAY_600).margin(zero).marginTop(rem(0.25)).done(),
                    text("Review and approve JWeb framework access requests"))
            )
        );
    }

    private Element tabs() {
        return div(attrs().style().padding(zero, rem(2)).backgroundColor(GRAY_50).flexShrink(0).done(),
            div(attrs().style().maxWidth(px(1200)).margin(zero, auto).done(),
                div(attrs().style().display(flex).gap(rem(0.5)).borderBottom(px(1), solid, GRAY_200).done(),
                    tabBtn("pending", "Pending", true), tabBtn("all", "All Requests", false)
                )
            )
        );
    }

    private Element requestsContainer() {
        return div(attrs().id("requests-container").style().flex(num(1)).overflowY(scroll)
            .padding(rem(1), rem(2), rem(2), rem(2)).minHeight(zero).maxHeight(vh(60)).done(),
            div(attrs().style().maxWidth(px(1200)).margin(zero, auto).done(),
                div(attrs().style().textAlign(center).padding(rem(3)).color(GRAY_500).done(), text("Loading..."))
            )
        );
    }
}
