package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Stylesheet.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.Keyframes.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Document head with meta tags and global styles.
 */
public class Head implements Template {
    private final String pageTitle;

    public Head(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public Element render() {
        return head(
            meta(attr("charset", "UTF-8")),
            meta(name("viewport"), attr("content", "width=device-width, initial-scale=1")),
            title(pageTitle),
            style(globalStyles())
        );
    }

    private String globalStyles() {
        return stylesheet()
            .rule("*, *::before, *::after", style()
                .boxSizing(borderBox)
                .margin(zero)
                .padding(zero))
            .rule("html", style()
                .fontSize(px(16))
                .prop("scroll-behavior", "smooth"))
            .rule("body", style()
                .fontFamily("system-ui, -apple-system, sans-serif")
                .lineHeight(1.6)
                .color(TEXT)
                .backgroundColor(BG)
                .minHeight(vh(100))
                .display(flex)
                .flexDirection(column))
            .rule("a", style()
                .color(PRIMARY)
                .textDecoration(none))
            .rule("a:hover", style()
                .color(PRIMARY_DARK))
            .keyframes(keyframes("gradientShift")
                .at(0, style().prop("background-position", "0% 50%"))
                .at(50, style().prop("background-position", "100% 50%"))
                .at(100, style().prop("background-position", "0% 50%")))
            .build();
    }
}
