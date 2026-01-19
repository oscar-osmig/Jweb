package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.pages.tryit.TryItTheme.*;

/** Try It page with request access and download forms. */
public class TryItPage implements Template {

    @Override
    public Element render() {
        return div(attrs().style()
            .width(percent(100)).flex(num(1)).display(flex).flexDirection(column)
            .alignItems(center).justifyContent(flexStart).paddingTop(vh(8))
            .padding(rem(1)).boxSizing(borderBox)
            .background(linearGradient("135deg", hex("#f5f7fa"), hex("#c3cfe2")))
        .done(),
            header(),
            div(attrs().style().display(flex).gap(rem(1.5)).flexWrap(wrap)
                .justifyContent(center).maxWidth(px(850)).width(percent(100)).done(),
                card("rocket", "Request Access", "Get a download token sent to your email", requestForm()),
                card("download", "Download Project", "Already have a token? Download now", downloadForm())
            ),
            script("https://cdn.jsdelivr.net/npm/@emailjs/browser@4/dist/email.min.js"),
            inlineScript(TryItScripts.formHandlers())
        );
    }

    private Element header() {
        return div(attrs().style().textAlign(center).marginBottom(rem(1.5)).done(),
            h1(attrs().style().fontSize(rem(2)).fontWeight(700).color(hex("#1a1a2e")).margin(zero).done(),
                text("Try JWeb Framework")),
            p(attrs().style().fontSize(rem(1)).color(GRAY_600).margin(zero).done(),
                text("Get started with the pure Java web framework"))
        );
    }

    private Element requestForm() {
        return form(attrs().id("request-form").style().display(flex).flexDirection(column).gap(rem(0.75)).done(),
            field("Email Address", "email", "email", "you@example.com"),
            textareaField("Why do you want to try JWeb?", "message", "Tell us about your project...", 3),
            div(attrs().id("request-message").style().padding(rem(0.6)).borderRadius(px(6))
                .fontSize(rem(0.8)).display(none).done()),
            submitBtn("Send Request", INDIGO)
        );
    }

    private Element downloadForm() {
        return form(attrs().id("download-form").style().display(flex).flexDirection(column).gap(rem(0.75)).done(),
            field("Email Address", "email", "email", "you@example.com"),
            field("Your Download Token", "token", "text", "Paste your token here..."),
            messageBox("download-message", "Enter your email and token to download"),
            submitBtn("Download Project", GREEN)
        );
    }
}
