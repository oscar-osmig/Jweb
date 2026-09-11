package com.osmig.Jweb.app.layout;

import jweb.Element;
import jweb.DevServer;
import jweb.Template;
import jweb.css.Stylesheet;

import static jweb.El.*;
import static jweb.Css.*;

/**
 * Main layout wrapper with nav and footer.
 */
public class Layout implements Template {
    private final String title;
    private final Element content;

    public Layout(String title, Element content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Element render() {
        // A Template is an Element, so components drop straight in as children.
        return html(
            new Head(title),
            // The body shell (fixed height, flex column) lives in this
            // layout's stylesheet so the dvh fallback can apply; main is the
            // one scroll container, so any page taller than a phone screen
            // just scrolls.
            body(
                new Nav(),
                main(style().flex(1).minHeight(zero).overflowY(auto), content),
                new Footer(),
                DevServer.script() // Hot reload - only active when jweb.dev.hot-reload=true
            )
        );
    }

    /**
     * The app's global stylesheet — design tokens, the reset, and the
     * document shell. The render collects it and emits it in {@code <head>};
     * no page has to remember to include it.
     */
    @Override
    public Stylesheet styles() {
        return stylesheet()
            .add(Theme.TOKENS)
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
                .color(Theme.TEXT)
                .backgroundColor(Theme.BG)
                .height(vh(100))
                .overflow(hidden)
                .display(flex)
                .flexDirection(column))
            // Dynamic viewport height: tracks mobile browser chrome as it
            // collapses; browsers without dvh keep the 100vh above.
            .rule("body", style().height(dvh(100)))
            .rule("img, video", style().maxWidth(percent(100)))
            .rule("a", style()
                .color(Theme.PRIMARY)
                .textDecoration(none))
            .rule("a:hover", style()
                .color(Theme.PRIMARY_DARK))
            .add(media().prefersReducedMotion()
                .rule("*", style()
                    .animationDuration(ms(0))
                    .transitionDuration(ms(0))))
            .add(keyframes("gradientShift")
                .at(0, style().backgroundPosition(percent(0), percent(50)))
                .at(50, style().backgroundPosition(percent(100), percent(50)))
                .at(100, style().backgroundPosition(percent(0), percent(50))))
            // The record-driven forms bring their own base rules and class
            // names; this is the one place the app dresses them.
            .rule(".jweb-label", style().color(Theme.TEXT))
            .rule(".jweb-control", style().border(px(1), solid, Theme.BORDER))
            .rule(".jweb-control:focus", style().borderColor(Theme.PRIMARY))
            .rule(".jweb-submit", style().width(percent(100)).backgroundColor(Theme.PRIMARY))
            // The admin card's sign-in button carries the brand gradient
            .rule(".admin-login-form .jweb-submit", style().apply(Theme.brandFlow()));
    }
}
