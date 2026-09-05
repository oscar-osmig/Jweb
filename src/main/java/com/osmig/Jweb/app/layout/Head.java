package com.osmig.Jweb.app.layout;

import jweb.Element;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
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
            metaCharset(),
            metaViewport(),
            com.osmig.Jweb.framework.seo.Seo
                .of(pageTitle, "Build complete web applications entirely in Java — "
                    + "type-safe components, fluent DSL, zero frontend tooling.")
                .siteName("JWeb")
                .render(),
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
                .scrollBehavior(smooth))
            .rule("body", style()
                .fontFamily("system-ui, -apple-system, sans-serif")
                .lineHeight(1.6)
                .color(TEXT)
                .backgroundColor(BG)
                .height(vh(100))
                .overflow(hidden)
                .display(flex)
                .flexDirection(column))
            // Dynamic viewport height: tracks mobile browser chrome as it
            // collapses; browsers without dvh keep the 100vh above.
            .rule("body", style().height(dvh(100)))
            .rule("img, video", style().maxWidth(percent(100)))
            .rule("a", style()
                .color(PRIMARY)
                .textDecoration(none))
            .rule("a:hover", style()
                .color(PRIMARY_DARK))
            .add(media().prefersReducedMotion()
                .rule("*", style()
                    .animationDuration(ms(0))
                    .transitionDuration(ms(0))))
            .add(keyframes("gradientShift")
                .at(0, style().backgroundPosition(percent(0), percent(50)))
                .at(50, style().backgroundPosition(percent(100), percent(50)))
                .at(100, style().backgroundPosition(percent(0), percent(50))))
            .build();
    }
}
