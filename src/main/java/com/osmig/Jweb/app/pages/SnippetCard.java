package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.app.sandbox.SandboxDsl;
import com.osmig.Jweb.app.sandbox.SandboxDsl.Result;
import jweb.Cls;
import jweb.CSSValue;
import jweb.Doc;
import jweb.Element;
import jweb.css.Selector;
import jweb.css.Stylesheet;

import java.text.SimpleDateFormat;
import java.util.Date;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * One snippet as a three-pane card — the code on the left, who sent it in
 * the middle, and on the right what it renders to. The preview runs the code
 * through the Sandbox's whitelist interpreter ({@link SandboxDsl}): real DSL
 * output through the normal escaping pipeline, never compiled or reflected.
 * Shared by the public gallery and the admin review page; {@link #RULES} is
 * the stylesheet both pages pick up through {@link #render}.
 */
public final class SnippetCard {
    private SnippetCard() {}

    public static final Cls CARD = cls("snip-card");
    public static final Cls CODE = cls("snip-code"), CODE_HEAD = cls("snip-code-head");
    public static final Cls COPY = cls("snip-copy"), COPIED = cls("copied");
    public static final Cls INFO = cls("snip-info"), TITLE = cls("snip-title"),
        META = cls("snip-meta"), ACTIONS = cls("snip-actions");
    public static final Cls PREVIEW = cls("snip-preview"), PREVIEW_HEAD = cls("snip-preview-head");
    public static final Cls DOT = cls("snip-dot"), DOT_R = cls("snip-dot-r"),
        DOT_Y = cls("snip-dot-y"), DOT_G = cls("snip-dot-g");
    public static final Cls URL = cls("snip-url"), STAGE = cls("snip-stage"), ERROR = cls("snip-error");

    private static final CSSValue[] MONO = {uiMonospace, font("SFMono-Regular"), font("Menlo"), monospace};

    /** The card. Extra elements (the admin's approve/reject forms) go under the title. */
    public static Element render(Doc snippet, Element... actions) {
        String src = snippet.getString("code", "");
        return styled(RULES, article(CARD, data("id", snippet.getId()),
            codePane(src),
            infoPane(snippet, actions),
            previewPane(preview(src))));
    }

    /** The dark code pane with its copy button. */
    public static Element codePane(String src) {
        return div(CODE,
            div(CODE_HEAD,
                span("☕ Java"),
                button(COPY, attrs().type("button")
                    .aria("label", "Copy code to clipboard"), "Copy")),
            pre(code(src)));
    }

    private static Element infoPane(Doc s, Element... actions) {
        String author = s.getString("author", "");
        return div(INFO,
            h3(TITLE, s.getString("title", "Untitled")),
            p(META,
                author.isBlank() ? "Anonymous" : author,
                " · ",
                formatDate(s.get("createdAt"))),
            when(actions.length > 0, () -> div(ACTIONS, fragment((Object[]) actions))));
    }

    /** The preview pane: a small browser chrome around the rendered output. */
    public static Element previewPane(Element content) {
        return div(PREVIEW,
            div(PREVIEW_HEAD,
                span(DOT, DOT_R),
                span(DOT, DOT_Y),
                span(DOT, DOT_G),
                span(URL, "preview")),
            div(STAGE, content));
    }

    /** The code's output, or the interpreter's message when it does not compile. */
    public static Element preview(String src) {
        Result r = SandboxDsl.run(src);
        return r.isOk() ? r.element() : div(ERROR, "✗ " + message(r));
    }

    /** The interpreter's message for code that does not compile, or null when it does. */
    public static String compileError(String src) {
        Result r = SandboxDsl.run(src);
        return r.isOk() ? null : message(r);
    }

    private static String message(Result r) {
        String where = r.errorLine() > 0 ? "line " + r.errorLine() + ": " : "";
        return where + r.error();
    }

    static String formatDate(Object dateObj) {
        if (dateObj instanceof Date date) {
            return new SimpleDateFormat("MMM d, yyyy").format(date);
        }
        return dateObj != null ? dateObj.toString() : "";
    }

    // ==================== styles ====================

    /** The card's rules — attached through {@link #render} via {@code styled(...)}. */
    static final Stylesheet RULES = stylesheet()
        .rule(CARD, style()
            .display(grid)
            .gridTemplateColumns(minmax(zero, fr(6)), minmax(zero, fr(4)), minmax(zero, fr(5)))
            .backgroundColor(white)
            .borderRadius(ROUNDED_LG)
            .border(px(1), solid, BORDER)
            .overflow(hidden)
            .boxShadow(shadow(0, px(1), px(2), rgba(15, 23, 42, 0.04)),
                shadow(0, px(12), px(32), px(-16), rgba(79, 70, 229, 0.35))))
        // Code pane — the sandbox editor's palette
        .rule(CODE, style()
            .display(flex).flexDirection(column).minWidth(zero)
            .backgroundColor(hex("#1e293b")))
        .rule(CODE_HEAD, style()
            .display(flex).alignItems(center).justifyContent(spaceBetween)
            .padding(SP_2, SP_4)
            .color(hex("#94a3b8")).fontSize(TEXT_SM)
            .borderBottom(px(1), solid, rgba(255, 255, 255, 0.08)))
        .rule(COPY, style()
            .padding(px(2), px(10))
            .backgroundColor(transparent).color(hex("#94a3b8"))
            .border(px(1), solid, rgba(255, 255, 255, 0.15))
            .borderRadius(ROUNDED).cursor(pointer)
            .fontSize(rem(0.75)).lineHeight(1.5))
        .rule(COPY.hover(), style()
            .color(hex("#e2e8f0")).backgroundColor(rgba(255, 255, 255, 0.08)))
        .rule(COPY.cls(COPIED.name()), style()
            .color(hex("#6ee7b7")).borderColor(rgba(110, 231, 183, 0.5)))
        // Long lines wrap rather than run off the pane — a snippet typed
        // on one line still reads in full
        .rule(CODE.descendant(Selector.type("pre")), style()
            .flex(1).margin(zero).padding(SP_4)
            .overflow(auto).maxHeight(px(440))
            .color(hex("#e2e8f0")).fontSize(TEXT_SM).lineHeight(1.6)
            .fontFamily(MONO)
            .whiteSpace(preWrap).overflowWrap(anywhere)
            .tabSize(4).scrollbarWidth(thin))
        .rule(CODE.descendant(Selector.type("code")), style().fontFamily(MONO))
        // Info pane
        .rule(INFO, style()
            .display(flex).flexDirection(column).minWidth(zero)
            .padding(SP_6)
            .borderRight(px(1), solid, BORDER))
        .rule(TITLE, style()
            .fontSize(TEXT_LG).fontWeight(600).color(TEXT).margin(zero)
            .overflowWrap(anywhere))
        .rule(META, style()
            .fontSize(TEXT_SM).color(TEXT_LIGHT).marginTop(SP_1))
        .rule(ACTIONS, style()
            .display(flex).flexWrap(wrap).gap(SP_2)
            .marginTop(auto).paddingTop(SP_4))
        // Preview pane — the sandbox's browser chrome
        .rule(PREVIEW, style()
            .display(flex).flexDirection(column).minWidth(zero)
            .backgroundColor(BG))
        .rule(PREVIEW_HEAD, style()
            .display(flex).alignItems(center).gap(SP_2)
            .padding(SP_2, SP_4)
            .borderBottom(px(1), solid, BORDER)
            .backgroundColor(hex("#f8fafc")))
        .rule(DOT, style()
            .width(px(10)).height(px(10)).borderRadius(percent(50)))
        .rule(DOT_R, style().backgroundColor(hex("#f87171")))
        .rule(DOT_Y, style().backgroundColor(hex("#fbbf24")))
        .rule(DOT_G, style().backgroundColor(hex("#34d399")))
        .rule(URL, style()
            .marginLeft(SP_2).color(TEXT_LIGHT).fontSize(rem(0.75)).fontFamily(MONO))
        .rule(STAGE, style()
            .flex(1).padding(SP_6).overflow(auto).minHeight(px(200))
            .backgroundImage(radialGradient("circle", stop(hex("#e2e8f0"), px(1)), stop(transparent, px(1))))
            .backgroundSize(px(16), px(16)))
        .rule(ERROR, style()
            .padding(SP_3).borderRadius(ROUNDED)
            .backgroundColor(hex("#fef2f2")).color(hex("#b91c1c"))
            .fontSize(TEXT_SM).fontFamily(MONO)
            .overflowWrap(anywhere))
        // Narrow screens: the three panes stack — code, author, preview
        .add(media().maxWidth(px(960))
            .rule(CARD, style().gridTemplateColumns(minmax(zero, fr(1))))
            .rule(INFO, style()
                .borderRight(none)
                .borderTop(px(1), solid, BORDER)
                .borderBottom(px(1), solid, BORDER))
            .rule(CODE.descendant(Selector.type("pre")), style().maxHeight(px(280))));
}
