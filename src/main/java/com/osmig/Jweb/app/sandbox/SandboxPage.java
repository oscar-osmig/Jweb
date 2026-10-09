package com.osmig.Jweb.app.sandbox;

import jweb.Action;
import jweb.Csrf;
import jweb.Element;
import jweb.Template;
import jweb.css.Keyframes;
import jweb.css.Selector;
import com.osmig.Jweb.app.sandbox.SandboxDsl.Result;
import com.osmig.Jweb.app.sandbox.SandboxFiles.Mode;
import com.osmig.Jweb.app.sandbox.SandboxFiles.SandboxFile;

import java.util.Optional;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import static com.osmig.Jweb.app.sandbox.SandboxCss.*;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.BLURB;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.CHIP;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.CHIP_PRIMARY;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.CONTROLS;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.KNOB;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.KNOB_LABEL;

/**
 * The playground v2: a collapsible starter-project tree, a real editor whose
 * code is interpreted live ({@link SandboxDsl}), knobs derived from whatever
 * the user typed, a draggable code/preview split, and preview traffic lights
 * that actually do things. Layout styling lives in class rules (not inline)
 * so the phone media query can restack it. Every class and id is a handle in
 * {@link SandboxCss}, shared with the stylesheet and {@link SandboxScript}.
 */
public class SandboxPage implements Template {
    private final String file;

    public SandboxPage(String file) {
        this.file = file;
    }

    @Override
    public String pageTitle() {
        return "Sandbox - JWeb";
    }

    /** The CSRF token the editor's render POST reads and sends as {@code _csrf}. */
    @Override
    public Optional<Element> extraHead() {
        Element meta = Csrf.tokenMeta();
        return Optional.of(meta);
    }

    @Override
    public Optional<Action> scripts() {
        return Optional.of(SandboxScript.build());
    }

    @Override
    public Element render() {
        SandboxFile f = SandboxFiles.byId(file);
        Result r = f.mode() == Mode.DSL ? SandboxDsl.run(f.source()) : null;
        return div(LAYOUT,
            tree(f.id()),
            div(PANES,
                div(DYNBAR, KNOBS, SandboxPanes.initialDynbar(f, r)),
                snippetPanel(),
                div(SPLIT, SPLIT_CLS,
                    div(CODE, CODE_CLS,
                        div(CODE_HEAD,
                            button(TREE_TOGGLE, TREE_TOGGLE_CLS, attrs()
                                .type("button").title("hide files")
                                .aria("label", "Toggle file tree"), "«"),
                            span(PATH, "☕ " + f.path())),
                        div(EDITWRAP,
                            div(LINES, LINES_CLS, attrs().aria("hidden", "true")),
                            textarea(EDITOR, EDITOR_CLS, attrs()
                                    .set("spellcheck", "false").set("autocomplete", "off")
                                    .set("autocapitalize", "off")
                                    .aria("label", "Code editor"),
                                f.source()),
                            div(MIRROR, MIRROR_CLS, attrs().aria("hidden", "true"))),
                        div(STATUS, STATUS_CLS, OK,
                            "✓ ready — edit the code, the preview follows")),
                    div(SandboxCss.GUTTER, GUTTER_CLS, attrs().aria("hidden", "true")),
                    div(PREVIEW, PREVIEW_CLS,
                        div(PREVIEW_HEAD,
                            button(DOT, DOT_R, attrs().type("button")
                                .title("nice try").aria("label", "Close (not really)")),
                            button(DOT, DOT_Y, attrs().type("button")
                                .title("restore").aria("label", "Exit full screen")),
                            button(DOT, DOT_G, attrs().type("button")
                                .title("full screen").aria("label", "Toggle full screen")),
                            span(URL, "localhost:8085")),
                        div(STAGE,
                            div(VIEW, SandboxPanes.initialView(f, r))))))
        );
    }

    // ==================== add snippet ====================

    /**
     * The "Add snippet" panel: a title and a name, with the editor's text in a
     * hidden field that {@link SandboxScript} fills when the panel opens. It
     * posts to /snippets/submit as a progressive swap and the status lands
     * under the form; the snippet goes to the admin's review queue and shows
     * on /snippets once approved.
     */
    private Element snippetPanel() {
        return div(SNIPPET_PANEL, SNIPPET,
            form(SNIPPET_FORM, SNIPPET_FORM_CLS, attrs()
                    .action("/snippets/submit").method("post")
                    .swapForm("/snippets/submit", SNIPPET_STATUS.build()),
                Csrf.tokenField(),
                input(SNIPPET_CODE, attrs().type("hidden").name("code")),
                label(SNIPPET_FIELD,
                    span("Title"),
                    input(SNIPPET_TITLE, attrs().type("text").name("title")
                        .set("placeholder", "Gradient card").maxlength(80).required(true))),
                label(SNIPPET_FIELD,
                    span("Your name"),
                    input(SNIPPET_AUTHOR, attrs().type("text").name("author")
                        .set("placeholder", "Ada").maxlength(80).required(true))),
                button(CHIP, CHIP_PRIMARY, attrs().type("submit"), "Send for review"),
                button(SNIPPET_CANCEL, CHIP, attrs().type("button"), "Cancel")),
            p(SNIPPET_HINT,
                "Sends this file's code to the admin for review. Approved snippets appear on ",
                a(href("/snippets"), "/snippets"), "."),
            div(SNIPPET_STATUS));
    }

    // ==================== file tree ====================

    private Element tree(String activeId) {
        return div(TREE,
            div(TREE_INNER,
                folder(0, "root", "demo/"),
                kids("root",
                    file(1, "pom", "📄 pom.xml", activeId),
                    folder(1, "src", "src/main/java/demo/"),
                    kids("src",
                        file(2, "app", "☕ App.java", activeId),
                        file(2, "routes", "☕ Routes.java", activeId),
                        folder(2, "pages", "pages/"),
                        kids("pages",
                            file(3, "home", "☕ HomePage.java", activeId)),
                        folder(2, "comp", "components/"),
                        kids("comp",
                            file(3, "greeting", "☕ GreetingCard.java", activeId),
                            file(3, "buttons", "☕ Buttons.java", activeId))))),
            div(TREE_FOOT,
                button(TREE_COLLAPSE, TREE_COLLAPSE_CLS, attrs()
                    .type("button").title("hide files")
                    .aria("label", "Hide file tree"), "«")));
    }

    private Element folder(int depth, String key, String name) {
        return div(FOLDER, depth(depth), attrs().data("folder", key),
            span(CHEV, "▾ "),
            "📁 " + name);
    }

    private Element kids(String key, Element... children) {
        return div(KIDS, attrs().data("kids", key), fragment(children));
    }

    private Element file(int depth, String id, String name, String activeId) {
        SandboxFile f = SandboxFiles.byId(id);
        return div(FILE, depth(depth), id.equals(activeId) ? ACTIVE : null,
            attrs().data("file", id).data("path", f.path()),
            name);
    }

    // ==================== styles ====================

    /** The red light's "no": declared once, named by the rule and the sheet. */
    private static final Keyframes SB_SHAKE = keyframes("sbShake")
        .at(0, style().transform(translateX(zero)))
        .at(25, style().transform(translateX(px(-5))))
        .at(50, style().transform(translateX(px(5))))
        .at(75, style().transform(translateX(px(-3))))
        .at(100, style().transform(translateX(zero)));

    /**
     * The sandbox's own stylesheet — collected by the render and emitted in
     * {@code <head>} with the rest of the page's CSS.
     */
    @Override
    public jweb.css.Stylesheet styles() {
        return stylesheet()
            .rule(LAYOUT, style()
                .display(flex).height(percent(100)).minHeight(num(0)))
            // Collapsible file tree. The kids wrapper uses display:contents so
            // nesting adds no layout box — collapsing is one class toggle.
            .rule(TREE, style()
                .width(px(250)).padding(SP_4).flexShrink(0)
                .display(flex).flexDirection(column).minHeight(num(0))
                .borderRight(px(1), solid, BORDER)
                .backgroundColor(hex("#f8fafc")))
            .rule(TREE_INNER, style()
                .flex(1).minHeight(num(0)).overflowY(auto))
            .rule(TREE_FOOT, style()
                .display(flex).justifyContent(flexEnd).paddingTop(SP_2))
            .rule(TREE_COLLAPSE_CLS, style()
                .padding(px(1), px(8))
                .backgroundColor(BG).color(TEXT_LIGHT)
                .border(px(1), solid, BORDER)
                .borderRadius(ROUNDED).cursor(pointer)
                .fontSize(TEXT_SM).lineHeight(1.4))
            .rule(TREE_COLLAPSE_CLS.hover(), style()
                .color(PRIMARY).backgroundColor(hex("#eef2ff")))
            .rule(KIDS, style().display(contents))
            .rule(with(KIDS, COLLAPSED), style().display(none))
            .rule(FOLDER, style()
                .color(TEXT_LIGHT).fontSize(TEXT_SM).cursor(pointer)
                .padding(px(4), SP_2).borderRadius(ROUNDED).whiteSpace(nowrap))
            .rule(FOLDER.hover(), style().backgroundColor(hex("#eef2ff")))
            .rule(CHEV, style()
                .display(inlineBlock)
                .transition(propTransform, s(0.15), ease))
            .rule(with(FOLDER, CLOSED).descendant(CHEV), style()
                .transform(rotate(deg(-90))))
            .rule(FILE, style()
                .fontSize(TEXT_SM).color(TEXT).cursor(pointer)
                .padding(px(5), SP_2).borderRadius(ROUNDED).whiteSpace(nowrap))
            .rule(FILE.hover(), style().backgroundColor(hex("#eef2ff")))
            .rule(with(FILE, ACTIVE), style()
                .backgroundColor(hex("#eef2ff")).color(PRIMARY).fontWeight(600))
            .rule(DEPTH_1, style().paddingLeft(rem(1.1)))
            .rule(DEPTH_2, style().paddingLeft(rem(2)))
            .rule(DEPTH_3, style().paddingLeft(rem(2.9)))
            // Panes column
            .rule(PANES, style()
                .flex(1).minWidth(zero).minHeight(num(0))
                .display(flex).flexDirection(column))
            .rule(KNOBS, style()
                .display(flex).flexWrap(wrap).alignItems(center).gap(SP_3)
                .padding(SP_3, SP_4).borderBottom(px(1), solid, BORDER))
            .rule(BLURB, style()
                .color(TEXT_LIGHT).fontSize(TEXT_SM).width(percent(100)))
            .rule(CONTROLS, style()
                .display(flex).flexWrap(wrap).alignItems(center).gap(SP_3))
            .rule(KNOB_LABEL, style()
                .display(flex).alignItems(center).gap(SP_2)
                .fontSize(TEXT_SM).color(TEXT_LIGHT))
            .rule(KNOB, style()
                .padding(px(6), px(10)).border(px(1), solid, BORDER)
                .borderRadius(ROUNDED).fontSize(TEXT_SM)
                .color(TEXT).backgroundColor(BG))
            .rule(Selector.type("input").cls(KNOB.name()).attr("type", "color"), style()
                .padding(px(2)).width(px(38)).height(px(30)).cursor(pointer))
            .rule(Selector.type("input").cls(KNOB.name()).attr("type", "number"), style()
                .width(px(80)))
            .rule(KNOB.focus(), style()
                .outline(px(2), solid, PRIMARY).outlineOffset(px(1)))
            .rule(CHIP, style()
                .padding(px(5), px(12)).borderRadius(px(999))
                .border(px(1), solid, hex("#c7d2fe"))
                .backgroundColor(hex("#eef2ff")).color(PRIMARY)
                .fontSize(TEXT_SM).cursor(pointer))
            .rule(CHIP.hover(), style().backgroundColor(hex("#e0e7ff")))
            .rule(CHIP_PRIMARY, style()
                .apply(brandFlow()).color(white).border(none))
            .rule(CHIP_PRIMARY.hover(), style().filter(brightness(1.08)))
            // "Add snippet" panel — hidden until its chip opens it
            .rule(SNIPPET, style()
                .display(none).padding(SP_3, SP_4)
                .borderBottom(px(1), solid, BORDER)
                .backgroundColor(hex("#f8fafc")))
            .rule(with(SNIPPET, OPEN), style().display(block))
            .rule(SNIPPET_FORM_CLS, style()
                .display(flex).flexWrap(wrap).alignItems(flexEnd).gap(SP_3))
            .rule(SNIPPET_FIELD, style()
                .display(flex).flexDirection(column).gap(px(4))
                .fontSize(TEXT_SM).color(TEXT_LIGHT))
            .rule(SNIPPET_FIELD.descendant(Selector.type("input")), style()
                .padding(px(6), px(10)).minWidth(px(200))
                .border(px(1), solid, BORDER).borderRadius(ROUNDED)
                .fontSize(TEXT_SM).color(TEXT).backgroundColor(BG))
            .rule(SNIPPET_FIELD.descendant(Selector.type("input").focus()), style()
                .outline(px(2), solid, PRIMARY).outlineOffset(px(1)))
            .rule(SNIPPET_HINT, style()
                .marginTop(SP_2).fontSize(rem(0.8)).color(TEXT_LIGHT))
            .rule(SNIPPET_STATUS.not(new Selector().empty()), style().marginTop(SP_2))
            // Editor pane
            .rule(SPLIT_CLS, style().display(flex).flex(1).minHeight(num(0)))
            .rule(CODE_CLS, style()
                .flex(1).minWidth(zero).minHeight(num(0))
                .display(flex).flexDirection(column)
                .backgroundColor(hex("#1e293b")))
            .rule(CODE_HEAD, style()
                .display(flex).alignItems(center).gap(SP_2)
                .color(hex("#94a3b8")).fontSize(TEXT_SM)
                .padding(SP_2, SP_4)
                .borderBottom(px(1), solid, rgba(255, 255, 255, 0.08)))
            .rule(TREE_TOGGLE_CLS, style()
                .padding(px(1), px(8))
                .backgroundColor(transparent).color(hex("#94a3b8"))
                .border(px(1), solid, rgba(255, 255, 255, 0.15))
                .borderRadius(ROUNDED).cursor(pointer)
                .fontSize(TEXT_SM).lineHeight(1.4))
            .rule(TREE_TOGGLE_CLS.hover(), style()
                .color(hex("#e2e8f0")).backgroundColor(rgba(255, 255, 255, 0.08)))
            .rule(with(TREE, TREE_HIDDEN), style().display(none))
            .rule(EDITWRAP, style()
                .display(flex).flex(1).minHeight(num(0))
                .position(relative).overflow(hidden))
            // The gutter's per-line heights are measured off the mirror, so
            // numbers stay aligned even when long lines soft-wrap.
            .rule(LINES_CLS, style()
                .flexShrink(0).overflow(hidden)
                .padding(SP_4, SP_2, SP_4, SP_3)
                .minWidth(rem(2.4)).textAlign(right)
                .color(hex("#475569"))
                .fontSize(TEXT_SM).lineHeight(1.6)
                .apply(mono())
                .userSelect(none)
                .borderRight(px(1), solid, rgba(255, 255, 255, 0.06)))
            .rule(LINES_CLS.descendant(ERRLINE), style()
                .color(hex("#fca5a5")).fontWeight(700))
            .rule(MIRROR_CLS, style()
                .position(absolute).top(zero).left(px(-10000))
                .visibility(hidden).pointerEvents(none)
                .fontSize(TEXT_SM).lineHeight(1.6)
                .apply(mono())
                .whiteSpace(preWrap).overflowWrap(breakWord)
                .tabSize(4))
            .rule(EDITOR_CLS, style()
                .flex(1).minWidth(zero).minHeight(num(0))
                .padding(SP_4, SP_4, SP_4, SP_3).overflow(auto)
                .backgroundColor(transparent).color(hex("#e2e8f0"))
                .fontSize(TEXT_SM).lineHeight(1.6)
                .apply(mono())
                .border(none).resize(none)
                .whiteSpace(preWrap).overflowWrap(breakWord)
                .tabSize(4)
                .caretColor(hex("#6ee7b7")))
            // Scroll containers keep scrolling, just without visible bars
            .rule(EDITOR_CLS.or(TREE_INNER).or(STAGE), style()
                .scrollbarWidth(none))
            .rule(EDITOR_CLS.pseudoEl("-webkit-scrollbar")
                    .or(TREE_INNER.pseudoEl("-webkit-scrollbar"))
                    .or(STAGE.pseudoEl("-webkit-scrollbar")),
                style().display(none))
            .rule(EDITOR_CLS.focus(), style().outline(none))
            .rule(STATUS_CLS, style()
                .padding(SP_2, SP_4).fontSize(rem(0.8))
                .apply(mono())
                .borderTop(px(1), solid, rgba(255, 255, 255, 0.08))
                .whiteSpace(nowrap).overflow(hidden).textOverflow(ellipsis))
            .rule(with(STATUS_CLS, OK), style().color(hex("#6ee7b7")))
            .rule(with(STATUS_CLS, ERR), style().color(hex("#fca5a5")))
            // Drag gutter
            .rule(GUTTER_CLS, style()
                .width(px(6)).flexShrink(0)
                .cursor(colResize)
                .backgroundColor(transparent)
                .transition(propBackgroundColor, s(0.15), ease))
            .rule(GUTTER_CLS.hover().or(with(GUTTER_CLS, DRAGGING)), style()
                .backgroundColor(hex("#c7d2fe")))
            .rule(with(SPLIT_CLS, DRAGGING), style().userSelect(none))
            // Preview pane with working traffic lights
            .rule(PREVIEW_CLS, style()
                .flex(1).minWidth(zero).minHeight(num(0))
                .display(flex).flexDirection(column)
                .backgroundColor(BG)
                .borderLeft(px(1), solid, BORDER))
            .rule(with(PREVIEW_CLS, MAX), style()
                .position(fixed).inset(zero)
                .zIndex(2000).borderLeft(none))
            .rule(PREVIEW_HEAD, style()
                .display(flex).alignItems(center).gap(SP_2)
                .padding(SP_2, SP_4)
                .borderBottom(px(1), solid, BORDER)
                .backgroundColor(hex("#f8fafc")))
            .rule(DOT, style()
                .width(px(12)).height(px(12)).borderRadius(percent(50))
                .border(none).padding(zero).cursor(pointer)
                .transition(propFilter, s(0.15), ease))
            .rule(DOT.hover(), style().filter(brightness(0.85)))
            .rule(DOT_R, style().backgroundColor(hex("#f87171")))
            .rule(DOT_Y, style().backgroundColor(hex("#fbbf24")))
            .rule(DOT_G, style().backgroundColor(hex("#34d399")))
            .rule(URL, style()
                .marginLeft(SP_2).color(TEXT_LIGHT).fontSize(TEXT_SM)
                .apply(mono()))
            .rule(STAGE, style()
                .flex(1).overflow(auto).padding(SP_8)
                .backgroundImage(radialGradient("circle",
                    stop(hex("#e2e8f0"), px(1)), stop(transparent, px(1))))
                .backgroundSize(px(16), px(16)))
            .rule(SHAKE, style()
                .animation(SB_SHAKE, s(0.4), linear, s(0), num(1)))
            // Phone: tree becomes a flat horizontal strip; panes stack
            .add(media().maxWidth(px(767))
                .rule(LAYOUT, style().flexDirection(column))
                .rule(TREE, style()
                    .width(auto).padding(SP_2, SP_4)
                    .flexDirection(row)
                    .borderRight(none)
                    .borderBottom(px(1), solid, BORDER))
                .rule(TREE_INNER, style()
                    .display(flex).alignItems(center).gap(SP_2)
                    .overflowX(auto).overflowY(hidden))
                .rule(TREE_FOOT, style().display(none))
                .rule(with(KIDS, COLLAPSED), style().display(contents))
                .rule(FOLDER, style().display(none))
                .rule(DEPTH_1.or(DEPTH_2).or(DEPTH_3), style().paddingLeft(SP_2))
                .rule(SPLIT_CLS, style().flexDirection(column))
                .rule(GUTTER_CLS, style().display(none))
                .rule(CODE_CLS.or(PREVIEW_CLS), style().minHeight(px(320)))
                .rule(PREVIEW_CLS, style()
                    .borderLeft(none)
                    .borderTop(px(1), solid, BORDER))
                .rule(STAGE, style().padding(SP_4)))
            .add(SB_SHAKE);
    }
}
