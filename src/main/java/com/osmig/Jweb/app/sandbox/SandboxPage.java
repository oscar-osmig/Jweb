package com.osmig.Jweb.app.sandbox;

import jweb.Element;
import jweb.Template;
import com.osmig.Jweb.app.sandbox.SandboxDsl.Result;
import com.osmig.Jweb.app.sandbox.SandboxFiles.Mode;
import com.osmig.Jweb.app.sandbox.SandboxFiles.SandboxFile;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * The playground v2: a collapsible starter-project tree, a real editor whose
 * code is interpreted live ({@link SandboxDsl}), knobs derived from whatever
 * the user typed, a draggable code/preview split, and preview traffic lights
 * that actually do things. Layout styling lives in class rules (not inline)
 * so the phone media query can restack it.
 */
public class SandboxPage implements Template {
    private final String file;

    public SandboxPage(String file) {
        this.file = file;
    }

    @Override
    public Element render() {
        SandboxFile f = SandboxFiles.byId(file);
        Result r = f.mode() == Mode.DSL ? SandboxDsl.run(f.source()) : null;
        return div(class_("sandbox-layout"),
            style(sandboxStyles()),
            tree(f.id()),
            div(class_("sandbox-panes"),
                div(attrs().id("sandbox-dynbar").class_("sandbox-knobs"),
                    SandboxPanes.initialDynbar(f, r)),
                div(attrs().id("sandbox-split").class_("sandbox-split"),
                    div(attrs().id("sandbox-code").class_("sandbox-code"),
                        div(class_("sandbox-code-head"),
                            button(attrs().id("sandbox-tree-toggle").class_("sandbox-tree-toggle")
                                .type("button").title("hide files")
                                .aria("label", "Toggle file tree"), text("«")),
                            span(attrs().id("sandbox-path"), text("☕ " + f.path()))),
                        div(class_("sandbox-editwrap"),
                            div(attrs().id("sandbox-lines").class_("sandbox-lines")
                                .aria("hidden", "true")),
                            textarea(attrs().id("sandbox-editor").class_("sandbox-editor")
                                    .set("spellcheck", "false").set("autocomplete", "off")
                                    .set("autocapitalize", "off")
                                    .aria("label", "Code editor"),
                                text(f.source())),
                            div(attrs().id("sandbox-mirror").class_("sandbox-mirror")
                                .aria("hidden", "true"))),
                        div(attrs().id("sandbox-status").class_("sandbox-status ok"),
                            text("✓ ready — edit the code, the preview follows"))),
                    div(attrs().id("sandbox-gutter").class_("sandbox-gutter")
                        .aria("hidden", "true")),
                    div(attrs().id("sandbox-preview").class_("sandbox-preview"),
                        div(class_("sandbox-preview-head"),
                            button(attrs().class_("sandbox-dot sandbox-dot-r").type("button")
                                .title("nice try").aria("label", "Close (not really)")),
                            button(attrs().class_("sandbox-dot sandbox-dot-y").type("button")
                                .title("restore").aria("label", "Exit full screen")),
                            button(attrs().class_("sandbox-dot sandbox-dot-g").type("button")
                                .title("full screen").aria("label", "Toggle full screen")),
                            span(class_("sandbox-url"), text("localhost:8085"))),
                        div(class_("sandbox-stage"),
                            div(attrs().id("sandbox-view"),
                                SandboxPanes.initialView(f, r)))))),
            inlineScript(SandboxScript.build())
        );
    }

    // ==================== file tree ====================

    private Element tree(String activeId) {
        return div(class_("sandbox-tree"),
            div(class_("sandbox-tree-inner"),
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
            div(class_("sandbox-tree-foot"),
                button(attrs().id("sandbox-tree-collapse").class_("sandbox-tree-collapse")
                    .type("button").title("hide files")
                    .aria("label", "Hide file tree"), text("«"))));
    }

    private Element folder(int depth, String key, String name) {
        return div(attrs().class_("sandbox-folder sandbox-depth-" + depth).data("folder", key),
            span(class_("sandbox-chev"), text("▾ ")),
            text("📁 " + name));
    }

    private Element kids(String key, Element... children) {
        return div(attrs().class_("sandbox-kids").data("kids", key), fragment(children));
    }

    private Element file(int depth, String id, String name, String activeId) {
        SandboxFile f = SandboxFiles.byId(id);
        return div(attrs()
                .class_("sandbox-file sandbox-depth-" + depth)
                .classIf("active", id.equals(activeId))
                .data("file", id).data("path", f.path()),
            text(name));
    }

    // ==================== styles ====================

    private String sandboxStyles() {
        return stylesheet()
            .rule(".sandbox-layout", style()
                .display(flex).height(percent(100)).minHeight(num(0)))
            // Collapsible file tree. .sandbox-kids uses display:contents so
            // nesting adds no layout box — collapsing is one class toggle.
            .rule(".sandbox-tree", style()
                .width(px(250)).padding(SP_4).flexShrink(0)
                .display(flex).flexDirection(column).minHeight(num(0))
                .borderRight(px(1), solid, BORDER)
                .backgroundColor(hex("#f8fafc")))
            .rule(".sandbox-tree-inner", style()
                .flex(1).minHeight(num(0)).overflowY(auto))
            .rule(".sandbox-tree-foot", style()
                .display(flex).justifyContent(flexEnd).paddingTop(SP_2))
            .rule(".sandbox-tree-collapse", style()
                .padding(px(1), px(8))
                .backgroundColor(BG).color(TEXT_LIGHT)
                .border(px(1), solid, BORDER)
                .borderRadius(ROUNDED).cursor(pointer)
                .fontSize(TEXT_SM).lineHeight(1.4))
            .rule(".sandbox-tree-collapse:hover", style()
                .color(PRIMARY).backgroundColor(hex("#eef2ff")))
            .rule(".sandbox-kids", style().prop("display", "contents"))
            .rule(".sandbox-kids.collapsed", style().display(none))
            .rule(".sandbox-folder", style()
                .color(TEXT_LIGHT).fontSize(TEXT_SM).cursor(pointer)
                .padding(px(4), SP_2).borderRadius(ROUNDED).whiteSpace(nowrap))
            .rule(".sandbox-folder:hover", style().backgroundColor(hex("#eef2ff")))
            .rule(".sandbox-chev", style()
                .prop("display", "inline-block")
                .prop("transition", "transform 0.15s ease"))
            .rule(".sandbox-folder.closed .sandbox-chev", style()
                .prop("transform", "rotate(-90deg)"))
            .rule(".sandbox-file", style()
                .fontSize(TEXT_SM).color(TEXT).cursor(pointer)
                .padding(px(5), SP_2).borderRadius(ROUNDED).whiteSpace(nowrap))
            .rule(".sandbox-file:hover", style().backgroundColor(hex("#eef2ff")))
            .rule(".sandbox-file.active", style()
                .backgroundColor(hex("#eef2ff")).color(PRIMARY).fontWeight(600))
            .rule(".sandbox-depth-1", style().paddingLeft(rem(1.1)))
            .rule(".sandbox-depth-2", style().paddingLeft(rem(2)))
            .rule(".sandbox-depth-3", style().paddingLeft(rem(2.9)))
            // Panes column
            .rule(".sandbox-panes", style()
                .flex(1).minWidth(zero).minHeight(num(0))
                .display(flex).flexDirection(column))
            .rule(".sandbox-knobs", style()
                .display(flex).flexWrap(wrap).alignItems(center).gap(SP_3)
                .padding(SP_3, SP_4).borderBottom(px(1), solid, BORDER))
            .rule(".sandbox-blurb", style()
                .color(TEXT_LIGHT).fontSize(TEXT_SM).width(percent(100)))
            .rule(".sandbox-controls", style()
                .display(flex).flexWrap(wrap).alignItems(center).gap(SP_3))
            .rule(".sandbox-knob-label", style()
                .display(flex).alignItems(center).gap(SP_2)
                .fontSize(TEXT_SM).color(TEXT_LIGHT))
            .rule(".sandbox-knob", style()
                .padding(px(6), px(10)).border(px(1), solid, BORDER)
                .borderRadius(ROUNDED).fontSize(TEXT_SM)
                .color(TEXT).backgroundColor(BG))
            .rule("input.sandbox-knob[type=color]", style()
                .padding(px(2)).width(px(38)).height(px(30)).cursor(pointer))
            .rule("input.sandbox-knob[type=number]", style().width(px(80)))
            .rule(".sandbox-knob:focus", style()
                .prop("outline", "2px solid #4f46e5").prop("outline-offset", "1px"))
            .rule(".sandbox-chip", style()
                .padding(px(5), px(12)).borderRadius(px(999))
                .border(px(1), solid, hex("#c7d2fe"))
                .backgroundColor(hex("#eef2ff")).color(PRIMARY)
                .fontSize(TEXT_SM).cursor(pointer))
            .rule(".sandbox-chip:hover", style().backgroundColor(hex("#e0e7ff")))
            // Editor pane
            .rule(".sandbox-split", style().display(flex).flex(1).minHeight(num(0)))
            .rule(".sandbox-code", style()
                .flex(1).minWidth(zero).minHeight(num(0))
                .display(flex).flexDirection(column)
                .backgroundColor(hex("#1e293b")))
            .rule(".sandbox-code-head", style()
                .display(flex).alignItems(center).gap(SP_2)
                .color(hex("#94a3b8")).fontSize(TEXT_SM)
                .padding(SP_2, SP_4)
                .borderBottom(px(1), solid, rgba(255, 255, 255, 0.08)))
            .rule(".sandbox-tree-toggle", style()
                .padding(px(1), px(8))
                .backgroundColor(transparent).color(hex("#94a3b8"))
                .border(px(1), solid, rgba(255, 255, 255, 0.15))
                .borderRadius(ROUNDED).cursor(pointer)
                .fontSize(TEXT_SM).lineHeight(1.4))
            .rule(".sandbox-tree-toggle:hover", style()
                .color(hex("#e2e8f0")).backgroundColor(rgba(255, 255, 255, 0.08)))
            .rule(".sandbox-tree.hidden", style().display(none))
            .rule(".sandbox-editwrap", style()
                .display(flex).flex(1).minHeight(num(0))
                .position(relative).overflow(hidden))
            // The gutter's per-line heights are measured off #sandbox-mirror,
            // so numbers stay aligned even when long lines soft-wrap.
            .rule(".sandbox-lines", style()
                .flexShrink(0).overflow(hidden)
                .padding(SP_4, SP_2, SP_4, SP_3)
                .minWidth(rem(2.4)).textAlign(right)
                .color(hex("#475569"))
                .fontSize(TEXT_SM).lineHeight(1.6)
                .fontFamily("ui-monospace, SFMono-Regular, Menlo, monospace")
                .userSelect(none)
                .borderRight(px(1), solid, rgba(255, 255, 255, 0.06)))
            .rule(".sandbox-lines .errline", style()
                .color(hex("#fca5a5")).fontWeight(700))
            .rule(".sandbox-mirror", style()
                .position(absolute).top(zero).left(px(-10000))
                .visibility(hidden).pointerEvents(none)
                .fontSize(TEXT_SM).lineHeight(1.6)
                .fontFamily("ui-monospace, SFMono-Regular, Menlo, monospace")
                .prop("white-space", "pre-wrap").prop("overflow-wrap", "break-word")
                .prop("tab-size", "4"))
            .rule(".sandbox-editor", style()
                .flex(1).minWidth(zero).minHeight(num(0))
                .padding(SP_4, SP_4, SP_4, SP_3).overflow(auto)
                .backgroundColor(transparent).color(hex("#e2e8f0"))
                .fontSize(TEXT_SM).lineHeight(1.6)
                .fontFamily("ui-monospace, SFMono-Regular, Menlo, monospace")
                .prop("border", "none").prop("resize", "none")
                .prop("white-space", "pre-wrap").prop("overflow-wrap", "break-word")
                .prop("tab-size", "4")
                .prop("caret-color", "#6ee7b7"))
            // Scroll containers keep scrolling, just without visible bars
            .rule(".sandbox-editor, .sandbox-tree-inner, .sandbox-stage", style()
                .prop("scrollbar-width", "none"))
            .rule(".sandbox-editor::-webkit-scrollbar, .sandbox-tree-inner::-webkit-scrollbar, "
                + ".sandbox-stage::-webkit-scrollbar", style().display(none))
            .rule(".sandbox-editor:focus", style().prop("outline", "none"))
            .rule(".sandbox-status", style()
                .padding(SP_2, SP_4).fontSize(rem(0.8))
                .fontFamily("ui-monospace, SFMono-Regular, Menlo, monospace")
                .borderTop(px(1), solid, rgba(255, 255, 255, 0.08))
                .whiteSpace(nowrap).overflow(hidden).prop("text-overflow", "ellipsis"))
            .rule(".sandbox-status.ok", style().color(hex("#6ee7b7")))
            .rule(".sandbox-status.err", style().color(hex("#fca5a5")))
            // Drag gutter
            .rule(".sandbox-gutter", style()
                .width(px(6)).flexShrink(0)
                .prop("cursor", "col-resize")
                .backgroundColor(transparent)
                .prop("transition", "background-color 0.15s ease"))
            .rule(".sandbox-gutter:hover, .sandbox-gutter.dragging", style()
                .backgroundColor(hex("#c7d2fe")))
            .rule(".sandbox-split.dragging", style().prop("user-select", "none"))
            // Preview pane with working traffic lights
            .rule(".sandbox-preview", style()
                .flex(1).minWidth(zero).minHeight(num(0))
                .display(flex).flexDirection(column)
                .backgroundColor(BG)
                .borderLeft(px(1), solid, BORDER))
            .rule(".sandbox-preview.sandbox-max", style()
                .prop("position", "fixed").inset(zero)
                .zIndex(2000).prop("border-left", "none"))
            .rule(".sandbox-preview-head", style()
                .display(flex).alignItems(center).gap(SP_2)
                .padding(SP_2, SP_4)
                .borderBottom(px(1), solid, BORDER)
                .backgroundColor(hex("#f8fafc")))
            .rule(".sandbox-dot", style()
                .width(px(12)).height(px(12)).borderRadius(percent(50))
                .prop("border", "none").padding(zero).cursor(pointer)
                .prop("transition", "filter 0.15s ease"))
            .rule(".sandbox-dot:hover", style().prop("filter", "brightness(0.85)"))
            .rule(".sandbox-dot-r", style().backgroundColor(hex("#f87171")))
            .rule(".sandbox-dot-y", style().backgroundColor(hex("#fbbf24")))
            .rule(".sandbox-dot-g", style().backgroundColor(hex("#34d399")))
            .rule(".sandbox-url", style()
                .marginLeft(SP_2).color(TEXT_LIGHT).fontSize(TEXT_SM)
                .fontFamily("ui-monospace, SFMono-Regular, Menlo, monospace"))
            .rule(".sandbox-stage", style()
                .flex(1).overflow(auto).padding(SP_8)
                .prop("background-image", "radial-gradient(circle, #e2e8f0 1px, transparent 1px)")
                .prop("background-size", "16px 16px"))
            .rule(".sandbox-shake", style()
                .animation("sbShake", s(0.4), linear, s(0), num(1)))
            // Phone: tree becomes a flat horizontal strip; panes stack
            .add(media().maxWidth(px(767))
                .rule(".sandbox-layout", style().flexDirection(column))
                .rule(".sandbox-tree", style()
                    .width(auto).padding(SP_2, SP_4)
                    .flexDirection(row)
                    .prop("border-right", "none")
                    .borderBottom(px(1), solid, BORDER))
                .rule(".sandbox-tree-inner", style()
                    .display(flex).alignItems(center).gap(SP_2)
                    .overflowX(auto).overflowY(hidden))
                .rule(".sandbox-tree-foot", style().display(none))
                .rule(".sandbox-kids.collapsed", style().prop("display", "contents"))
                .rule(".sandbox-folder", style().display(none))
                .rule(".sandbox-depth-1, .sandbox-depth-2, .sandbox-depth-3", style()
                    .paddingLeft(SP_2))
                .rule(".sandbox-split", style().flexDirection(column))
                .rule(".sandbox-gutter", style().display(none))
                .rule(".sandbox-code, .sandbox-preview", style().minHeight(px(320)))
                .rule(".sandbox-preview", style()
                    .prop("border-left", "none")
                    .borderTop(px(1), solid, BORDER))
                .rule(".sandbox-stage", style().padding(SP_4)))
            .add(keyframes("sbShake")
                .at(0, style().prop("transform", "translateX(0)"))
                .at(25, style().prop("transform", "translateX(-5px)"))
                .at(50, style().prop("transform", "translateX(5px)"))
                .at(75, style().prop("transform", "translateX(-3px)"))
                .at(100, style().prop("transform", "translateX(0)")))
            .build();
    }
}
