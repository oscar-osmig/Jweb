package com.osmig.Jweb.app.sandbox;

import jweb.Action;
import jweb.CsrfToken;
import jweb.Func;
import jweb.Val;
import jweb.css.Selector;
import jweb.js.JSOperators;
import jweb.js.JSRegex;
import jweb.js.JSUrl;

import static jweb.Js.*;
import static com.osmig.Jweb.app.sandbox.SandboxCss.*;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.ADD_SNIPPET;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.KNOB;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.RESET;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.RX_DYNBAR;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.RX_STATUS;
import static com.osmig.Jweb.app.sandbox.SandboxPanes.RX_VIEW;

/**
 * Client side of the live editor, in the DSL. The textarea is authoritative:
 * renders go out as debounced POSTs and only the dynbar/view/status fragments
 * are swapped back in — the editor itself is never replaced. Knob edits patch
 * the editor text at server-reported offsets, then re-render. All handlers are
 * delegated to the layout where possible; every element is addressed through
 * the handles in {@link SandboxCss} and {@link SandboxPanes}.
 *
 * <p>Two behaviors carry what used to be the bulk of this file: {@code
 * lineGutter} owns the wrap-accurate line numbers (and re-measures itself on
 * every width change, so the split drag and the tree toggle need no callback),
 * and {@code splitPane} owns the draggable divider.</p>
 */
final class SandboxScript {
    private SandboxScript() {}

    private static final Val E = v("E");
    private static final Val CUR = v("cur");
    private static final Val EDITS = v("edits");
    private static final Val SOURCES = v("sources");
    private static final Val BASE = v("sbKnobBase");
    private static final Val PV = v("PV");

    static Action build() {
        return actions()
            .does(guard("__sandboxInit")
                .var_("E", byId(EDITOR))
                .var_("PV", byId(PREVIEW))
                .var_("cur", currentFile())
                .var_("edits", obj())
                .var_("sources", obj())
                .var_("sbT", null_())
                .var_("sbKnobBase", E.dot("value"))
                .does(SOURCES.at(CUR).assign(E.dot("value")))
                .add(status())
                .add(focusKnob())
                .add(dynbar())
                .add(view())
                .add(render())
                .add(queue())
                .add(source())
                .add(newline())
                .add(tree())
                .does(
                    lineGutter(EDITOR.build(), LINES.build()).mirror(MIRROR.build())
                        .errorClass(ERRLINE.name()),
                    splitPane(GUTTER.build(), CODE.build())
                        .container(SPLIT.build()).minPercent(20).maxPercent(80),
                    editorInput(),
                    editorKeys(),
                    fileClicks(),
                    folderClicks(),
                    treeToggleClick(),
                    treeCollapseClick(),
                    knobEdits(),
                    resetClick(),
                    addSnippetClick(),
                    snippetCancelClick(),
                    maximise(),
                    restore(),
                    shake(),
                    onKey("Escape", callback("e")
                        .does(PV.dot("classList").call("remove", MAX.name())))));
    }

    /** The file the server rendered as active, or the default one. */
    private static Val currentFile() {
        return query(with(FILE, ACTIVE)).or(obj("dataset", obj()))
            .dot("dataset").dot("file").or(str("home"));
    }

    // ==================== the render loop ====================

    /** The CSRF token from the page's {@code <meta>}, for the hand-written POST. */
    private static Val csrfToken() {
        return query(Selector.type("meta").attr("name", "csrf-token")).dot("content");
    }

    /** POST the editor's text; fold the three returned fragments back in. */
    private static Func render() {
        Val f = v("f"), body = v("body"), sent = v("sent");
        return func("sbRender")
            .var_("sent", E.dot("value"))
            .var_("body", JSUrl.params())
            .does(body.call("set", "file", CUR),
                  body.call("set", "code", sent),
                  body.call("set", CsrfToken.TOKEN_PARAM_NAME, csrfToken()))
            .does(fetch(str("/sandbox/render")).post().sameOriginCredentials()
                .body(body).text()
                .then(callback("html")
                    .var_("f", parseHtml(v("html")))
                    .var_("st", f.call("querySelector", RX_STATUS.build()))
                    .var_("db", f.call("querySelector", RX_DYNBAR.build()))
                    .var_("vw", f.call("querySelector", RX_VIEW.build()))
                    .if_(v("st"), call("sbStatus", v("st")))
                    .if_(v("db"), call("sbDynbar", v("db")))
                    .if_(v("vw"), call("sbView", v("vw"), sent)))
                // A failed render leaves the last good preview on screen
                .catch_(callback())
                .toVal());
    }

    /** Debounces the render — every edit path queues instead of firing. */
    private static Func queue() {
        return func("sbQueue", "ms")
            .if_(v("sbT"), clearTimeout(v("sbT")))
            .does(v("sbT").assign(call("setTimeout", v("sbRender"), v("ms"))));
    }

    /** The compile message, plus the gutter mark for the line it names. */
    private static Func status() {
        Val st = v("st"), out = v("out"), ok = v("ok"), lm = v("lm");
        return func("sbStatus", "st")
            .var_("out", byId(STATUS))
            .var_("ok", st.dot("dataset").dot("ok").eq("1"))
            .does(out.dot("textContent").assign(st.dot("textContent")),
                  out.dot("className").assign(
                      str(STATUS_CLS.name() + " ").plus(ok.ternary(OK.name(), ERR.name()))))
            .var_("lm", ok.ternary(null_(), JSRegex.regex("line (\\d+)").match(st.dot("textContent"))))
            .does(markLine(EDITOR.build(), lm.ternary(parseInt(lm.at(1)), 0)));
    }

    /** Knobs are re-rendered wholesale, so the focused one is put back by id. */
    private static Func dynbar() {
        Val ae = v("ae");
        return func("sbDynbar", "db")
            .var_("ae", activeElement())
            .var_("fid", ae.and(ae.dot("classList"))
                .and(ae.dot("classList").call("contains", KNOB.name()))
                .ternary(ae.dot("dataset").dot("id"), null_()))
            // Only a text knob has a caret to remember
            .var_("pos", v("fid").and(ae.dot("type").eq("text"))
                .ternary(ae.dot("selectionStart"), 0))
            .does(byId(DYNBAR).setHtml(v("db").dot("innerHTML")))
            .call("sbFocusKnob", v("fid"), v("pos"));
    }

    private static Func focusKnob() {
        Val fid = v("fid"), pos = v("pos"), k = v("k");
        return func("sbFocusKnob", "fid", "pos")
            .if_(fid.not(), return_())
            .var_("k", arrayFrom(queryAll(KNOB))
                .find(callback("n").return_(v("n").dot("dataset").dot("id").eq(fid))))
            .if_(k.not(), return_())
            .does(k.call("focus"))
            .if_(k.dot("type").eq("text"), k.call("setSelectionRange", pos, pos));
    }

    /** The preview, and the text the knob offsets were measured against. */
    private static Func view() {
        return func("sbView", "vw", "sent")
            .does(byId(VIEW).setHtml(v("vw").dot("innerHTML")),
                  BASE.assign(v("sent")));
    }

    /** An edited file wins over the server's copy; both are cached. */
    private static Func source() {
        Val id = v("id");
        return func("sbSrc", "id", "cb")
            .if_(EDITS.hasOwnProperty(id), return_(call("cb", EDITS.at(id))))
            .if_(SOURCES.hasOwnProperty(id), return_(call("cb", SOURCES.at(id))))
            .does(fetch(str("/sandbox/source?file=").plus(JSUrl.encode(id)))
                .sameOriginCredentials().text()
                .then(callback("txt").does(
                    SOURCES.at(id).assign(v("txt")),
                    call("cb", v("txt"))))
                .catch_(callback())
                .toVal());
    }

    // ==================== the editor ====================

    private static Val editorInput() {
        return E.call("addEventListener", "input", callback().does(
            EDITS.at(CUR).assign(E.dot("value")),
            syncSnippetCode(),
            call("sbQueue", 300)));
    }

    /**
     * The "Add snippet" form's hidden field always carries the editor's text.
     * Typing fires an input event; a file switch, a reset and a knob edit set
     * the editor's value directly, so they call this themselves.
     */
    private static jweb.js.Stmt syncSnippetCode() {
        return byId(SNIPPET_CODE).dot("value").assign(E.dot("value"));
    }

    private static Val editorKeys() {
        Val key = v("e").dot("key");
        return E.call("addEventListener", "keydown", callback("e")
            .if_(key.eq("Tab"), preventDefault(), insertText(E, "    "))
            .elif(key.eq("Enter"), preventDefault(), call("sbNewline")));
    }

    /** Enter keeps the current line's indentation. */
    private static Func newline() {
        Val head = v("head"), line = v("line");
        return func("sbNewline")
            .var_("head", E.dot("value").call("slice", 0, E.dot("selectionStart")))
            .var_("line", head.call("slice", head.lastIndexOf("\n").plus(1)))
            .var_("indent", JSRegex.regex("^ *").match(line).or(array("")).at(0))
            .does(insertText(E, str("\n").plus(v("indent"))));
    }

    // ==================== add snippet ====================

    /** "＋ Add snippet": the editor's text rides in the hidden field, then the panel opens. */
    private static Val addSnippetClick() {
        return delegate(LAYOUT, "click", ADD_SNIPPET).handler(callback("e", "t")
            .does(syncSnippetCode(),
                  byId(SNIPPET_PANEL).addClass(OPEN),
                  byId(SNIPPET_TITLE).call("focus")));
    }

    private static Val snippetCancelClick() {
        return delegate(LAYOUT, "click", SNIPPET_CANCEL).handler(callback("e", "t")
            .does(byId(SNIPPET_PANEL).removeClass(OPEN)));
    }

    // ==================== the file tree ====================

    private static Val fileClicks() {
        Val t = v("t"), id = v("id"), el = v("el");
        return delegate(LAYOUT, "click", FILE).handler(callback("e", "t")
            .var_("id", t.dot("dataset").dot("file"))
            .if_(id.not().or(id.eq(CUR)), return_())
            .does(CUR.assign(id),
                queryAll(FILE).forEach(callback("el").does(
                    el.dot("classList").call("toggle", ACTIVE.name(),
                        el.dot("dataset").dot("file").eq(id)))),
                byId(PATH).setText(
                    str("☕ ").plus(t.dot("dataset").dot("path").or(str("")))))
            .call("sbSrc", id, callback("src").does(
                E.dot("value").assign(v("src")),
                BASE.assign(v("src")),
                syncSnippetCode(),
                relineGutter(EDITOR.build()),
                call("sbRender"))));
    }

    /** A folder click folds it; its kids wrapper is the one carrying its key. */
    private static Val folderClicks() {
        Val t = v("t"), kids = v("kids"), k = v("k");
        return delegate(LAYOUT, "click", FOLDER).handler(callback("e", "t")
            .does(t.dot("classList").call("toggle", CLOSED.name()))
            .var_("kids", arrayFrom(queryAll(KIDS)).find(callback("k")
                .return_(k.dot("dataset").dot("kids").eq(t.dot("dataset").dot("folder")))))
            .does(kids.and(kids.dot("classList").call("toggle", COLLAPSED.name()))));
    }

    /** Shared by the code-head «/» toggle and the sidebar's own « button. */
    private static Func tree() {
        Val hidden = v("hidden"), b = v("b");
        return func("sbTree", "hidden")
            .does(query(TREE).toggleClass(TREE_HIDDEN.name(), hidden))
            .var_("b", byId(TREE_TOGGLE))
            .does(b.dot("textContent").assign(hidden.ternary("»", "«")),
                  b.dot("title").assign(hidden.ternary("show files", "hide files")),
                  relineGutter(EDITOR.build()));
    }

    private static Val treeToggleClick() {
        Val hidden = query(TREE).hasClass(TREE_HIDDEN);
        return delegate(LAYOUT, "click", TREE_TOGGLE)
            .handler(callback("e", "t").call("sbTree", hidden.not()));
    }

    private static Val treeCollapseClick() {
        return delegate(LAYOUT, "click", TREE_COLLAPSE)
            .handler(callback("e", "t").call("sbTree", true));
    }

    // ==================== knobs ====================

    /** A knob edit patches the source at the offsets the server reported. */
    private static Val knobEdits() {
        Val t = v("t"), start = v("start"), len = v("len"), val = v("val"),
            delta = v("delta"), kind = t.dot("dataset").dot("kind"), k = v("k");
        return delegate(LAYOUT, "input", KNOB).handler(callback("e", "t")
            // Typing in the editor invalidates the offsets — re-render instead
            .if_(E.dot("value").neq(BASE), call("sbQueue", 150), return_())
            .var_("start", parseInt(t.dot("dataset").dot("start")))
            .var_("len", parseInt(t.dot("dataset").dot("len")))
            .var_("val", t.dot("value"))
            .if_(kind.eq("number").and(val.eq("")), return_())
            .if_(kind.eq("color").and(JSRegex.regex("^#[0-9a-fA-F]{6}$").test(val).not()), return_())
            .does(E.dot("value").assign(
                E.dot("value").call("slice", 0, start)
                    .plus(val)
                    .plus(E.dot("value").call("slice", start.plus(len)))))
            .var_("delta", val.length().minus(len))
            .does(t.dot("dataset").dot("len").assign(val.length()),
                queryAll(KNOB).forEach(callback("k")
                    .if_(k.neq(t).and(parseInt(k.dot("dataset").dot("start")).gt(start)),
                        k.dot("dataset").dot("start")
                            .assign(parseInt(k.dot("dataset").dot("start")).plus(delta)))),
                EDITS.at(CUR).assign(E.dot("value")),
                BASE.assign(E.dot("value")),
                syncSnippetCode(),
                relineGutter(EDITOR.build()),
                call("sbQueue", 250)));
    }

    private static Val resetClick() {
        return delegate(LAYOUT, "click", RESET).handler(callback("e", "t")
            .does(JSOperators.delete(EDITS, CUR))
            .call("sbSrc", CUR, callback("src").does(
                E.dot("value").assign(v("src")),
                BASE.assign(v("src")),
                syncSnippetCode(),
                relineGutter(EDITOR.build()),
                call("sbRender"))));
    }

    // ==================== preview traffic lights ====================

    private static Val maximise() {
        return delegate(LAYOUT, "click", DOT_G).handler(callback("e", "t")
            .does(PV.dot("classList").call("toggle", MAX.name())));
    }

    private static Val restore() {
        return delegate(LAYOUT, "click", DOT_Y).handler(callback("e", "t")
            .does(PV.dot("classList").call("remove", MAX.name())));
    }

    private static Val shake() {
        Val classes = PV.dot("classList");
        return delegate(LAYOUT, "click", DOT_R).handler(callback("e", "t")
            .does(classes.call("add", SHAKE.name()),
                setTimeout(callback().does(classes.call("remove", SHAKE.name())), 450)));
    }
}
