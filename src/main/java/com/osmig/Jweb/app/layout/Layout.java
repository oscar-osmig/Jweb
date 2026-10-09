package com.osmig.Jweb.app.layout;

import jweb.Cls;
import jweb.Element;
import jweb.DevServer;
import jweb.Template;
import jweb.css.Selector;
import jweb.css.Stylesheet;

import static jweb.El.*;
import static jweb.Css.*;

/**
 * Main layout wrapper with nav and footer.
 */
public class Layout implements Template {

    /**
     * Class names the record-driven form DSL ({@link jweb.Form}, outside
     * this package) emits on every generated field and submit button;
     * declared here — the one in-scope file that styles them — so the
     * selector is typed once instead of retyped as a string.
     */
    public static final Cls FORM_LABEL = cls("jweb-label");
    public static final Cls FORM_CONTROL = cls("jweb-control");
    public static final Cls FORM_SUBMIT = cls("jweb-submit");

    /** The admin sign-in card's wrapper class (declared in {@code AdminLoginPage}, outside this package). */
    public static final Cls ADMIN_LOGIN_FORM = cls("admin-login-form");

    private final String title;
    private final Element content;
    /** The page inside, when the layout wraps a Template — its hooks are forwarded. */
    private final Template page;

    public Layout(String title, Element content) {
        this.title = title;
        this.content = content;
        this.page = content instanceof Template t ? t : null;
    }

    /**
     * Wraps a page, taking the title from its {@code pageTitle()} — for the
     * routes that build the page by hand instead of the page table:
     * {@code return new Layout(new AdminLoginPage(error, submitted));}
     */
    public Layout(Template page) {
        this(page.pageTitle() != null ? page.pageTitle() : "JWeb", page);
    }

    @Override
    public String pageTitle() {
        return title;
    }

    @Override
    public String description() {
        return page != null ? page.description() : null;
    }

    @Override
    public java.util.Optional<Element> extraHead() {
        return page != null ? page.extraHead() : java.util.Optional.empty();
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
            .rule(Selector.any().or(Selector.any().before()).or(Selector.any().after()), style()
                .boxSizing(borderBox)
                .margin(zero)
                .padding(zero))
            .rule(Selector.type("html"), style()
                .fontSize(px(16))
                .scrollBehavior(smooth))
            .rule(Selector.type("body"), style()
                .fontFamily(systemUi, font("-apple-system"), sansSerif)
                .lineHeight(1.6)
                .color(Theme.TEXT)
                .backgroundColor(Theme.BG)
                .height(vh(100))
                .overflow(hidden)
                .display(flex)
                .flexDirection(column))
            // Dynamic viewport height: tracks mobile browser chrome as it
            // collapses; browsers without dvh keep the 100vh above.
            .rule(Selector.type("body"), style().height(dvh(100)))
            .rule(Selector.type("img").or(Selector.type("video")), style().maxWidth(percent(100)))
            .rule(Selector.type("a"), style()
                .color(Theme.PRIMARY)
                .textDecoration(none))
            .rule(Selector.type("a").hover(), style()
                .color(Theme.PRIMARY_DARK))
            .add(media().prefersReducedMotion()
                .rule(Selector.any(), style()
                    .animationDuration(ms(0))
                    .transitionDuration(ms(0))))
            .add(Theme.GRADIENT_SHIFT)
            // The record-driven forms bring their own base rules and class
            // names; this is the one place the app dresses them.
            .rule(FORM_LABEL, style().color(Theme.TEXT))
            .rule(FORM_CONTROL, style().border(px(1), solid, Theme.BORDER))
            .rule(FORM_CONTROL.focus(), style().borderColor(Theme.PRIMARY))
            .rule(FORM_SUBMIT, style().width(percent(100)).backgroundColor(Theme.PRIMARY))
            // The admin card's sign-in button carries the brand gradient
            .rule(ADMIN_LOGIN_FORM.descendant(FORM_SUBMIT), style().apply(Theme.brandFlow()));
    }
}
