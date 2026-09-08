package com.osmig.Jweb.app.sandbox;

import jweb.Func;
import jweb.Val;
import jweb.js.JSOperators;
import jweb.js.JSRegex;
import jweb.js.JSUrl;

import static jweb.Js.*;

/**
 * Client side of the live editor, in the DSL. The textarea is authoritative:
 * renders go out as debounced POSTs and only the dynbar/view/status fragments
 * are swapped back in — the editor itself is never replaced. Knob edits patch
 * the editor text at server-reported offsets, then re-render. All handlers are
 * delegated to .sandbox-layout where possible.
 *
 * <p>Two behaviors carry what used to be the bulk of this file: {@code
 * lineGutter} owns the wrap-accurate line numbers (and re-measures itself on
 * every width change, so the split drag and the tree toggle need no callback),
 * and {@code splitPane} owns the draggable divider.</p>
 */
final class SandboxScript {
    private SandboxScript() {}

    private static final String EDITOR = "#sandbox-editor";
    private static final String LAYOUT = ".sandbox-layout";

    private static final Val E = v("E");
    private static final Val CUR = v("cur");
    private static final Val EDITS = v("edits");
    private static final Val SOURCES = v("sources");
    private static final Val BASE = v("sbKnobBase");
    private static final Val PV = v("PV");

    static String build() {
        return actions()
            .does(guard("__sandboxInit")
                .var_("E", byId("sandbox-editor"))
                .var_("PV", byId("sandbox-preview"))
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
                    lineGutter(EDITOR, "#sandbox-lines").mirror("#sandbox-mirror"),
                    splitPane("#sandbox-gutter", "#sandbox-code")
                        .container("#sandbox-split").minPercent(20).maxPercent(80),
                    editorInput(),
                    editorKeys(),
                    fileClicks(),
                    folderClicks(),
                    treeToggleClick(),
                    treeCollapseClick(),
                    knobEdits(),
                    resetClick(),
                    maximise(),
                    restore(),
                    shake(),
                    onKey("Escape", callback("e")
                        .does(PV.dot("classList").call("remove", "sandbox-max")))))
            .build();
    }

    /** The file the server rendered as active, or the default one. */
    private static Val currentFile() {
        return query(".sandbox-file.active").or(obj("dataset", obj()))
            .dot("dataset").dot("file").or(str("home"));
    }

    // ==================== the render loop ====================

    /** POST the editor's text; fold the three returned fragments back in. */
    private static Func render() {
        Val f = v("f"), body = v("body"), sent = v("sent");
        return func("sbRender")
            .var_("sent", E.dot("value"))
            .var_("body", JSUrl.params())
            .does(body.call("set", "file", CUR),
                  body.call("set", "code", sent))
            .does(fetch(str("/sandbox/render")).post().sameOriginCredentials()
                .body(body).text()
                .then(callback("html")
                    .var_("f", parseHtml(v("html")))
                    .var_("st", f.call("querySelector", "#rx-status"))
                    .var_("db", f.call("querySelector", "#rx-dynbar"))
                    .var_("vw", f.call("querySelector", "#rx-view"))
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
            .var_("out", byId("sandbox-status"))
            .var_("ok", st.dot("dataset").dot("ok").eq("1"))
            .does(out.dot("textContent").assign(st.dot("textContent")),
                  out.dot("className").assign(str("sandbox-status ").plus(ok.ternary("ok", "err"))))
            .var_("lm", ok.ternary(null_(), JSRegex.regex("line (\\d+)").match(st.dot("textContent"))))
            .does(markLine(EDITOR, lm.ternary(parseInt(lm.at(1)), 0)));
    }

    /** Knobs are re-rendered wholesale, so the focused one is put back by id. */
    private static Func dynbar() {
        Val ae = v("ae");
        return func("sbDynbar", "db")
            .var_("ae", activeElement())
            .var_("fid", ae.and(ae.dot("classList"))
                .and(ae.dot("classList").call("contains", "sandbox-knob"))
                .ternary(ae.dot("dataset").dot("id"), null_()))
            // Only a text knob has a caret to remember
            .var_("pos", v("fid").and(ae.dot("type").eq("text"))
                .ternary(ae.dot("selectionStart"), 0))
            .does(byId("sandbox-dynbar").setHtml(v("db").dot("innerHTML")))
            .call("sbFocusKnob", v("fid"), v("pos"));
    }

    private static Func focusKnob() {
        Val fid = v("fid"), pos = v("pos"), k = v("k");
        return func("sbFocusKnob", "fid", "pos")
            .if_(fid.not(), return_())
            .var_("k", arrayFrom(queryAll(".sandbox-knob"))
                .find(callback("n").return_(v("n").dot("dataset").dot("id").eq(fid))))
            .if_(k.not(), return_())
            .does(k.call("focus"))
            .if_(k.dot("type").eq("text"), k.call("setSelectionRange", pos, pos));
    }

    /** The preview, and the text the knob offsets were measured against. */
    private static Func view() {
        return func("sbView", "vw", "sent")
            .does(byId("sandbox-view").setHtml(v("vw").dot("innerHTML")),
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
            call("sbQueue", 300)));
    }

    private static Val editorKeys() {
        Val key = v("e").dot("key");
        return E.call("addEventListener", "keydown", callback("e")
            .if_(key.eq("Tab"), preventDefault(), insertText(EDITOR, "    "))
            .elif(key.eq("Enter"), preventDefault(), call("sbNewline")));
    }

    /** Enter keeps the current line's indentation. */
    private static Func newline() {
        Val head = v("head"), line = v("line");
        return func("sbNewline")
            .var_("head", E.dot("value").call("slice", 0, E.dot("selectionStart")))
            .var_("line", head.call("slice", head.lastIndexOf("\n").plus(1)))
            .var_("indent", JSRegex.regex("^ *").match(line).or(array("")).at(0))
            .does(insertText(EDITOR, str("\n").plus(v("indent"))));
    }

    // ==================== the file tree ====================

    private static Val fileClicks() {
        Val t = v("t"), id = v("id"), el = v("el");
        return delegate(LAYOUT, "click", ".sandbox-file").handler(callback("e", "t")
            .var_("id", t.dot("dataset").dot("file"))
            .if_(id.not().or(id.eq(CUR)), return_())
            .does(CUR.assign(id),
                queryAll(".sandbox-file").forEach(callback("el").does(
                    el.dot("classList").call("toggle", "active",
                        el.dot("dataset").dot("file").eq(id)))),
                byId("sandbox-path").setText(
                    str("☕ ").plus(t.dot("dataset").dot("path").or(str("")))))
            .call("sbSrc", id, callback("src").does(
                E.dot("value").assign(v("src")),
                BASE.assign(v("src")),
                relineGutter(EDITOR),
                call("sbRender"))));
    }

    private static Val folderClicks() {
        Val t = v("t"), kids = v("kids");
        return delegate(LAYOUT, "click", ".sandbox-folder").handler(callback("e", "t")
            .does(t.dot("classList").call("toggle", "closed"))
            .var_("kids", v("document").call("querySelector",
                str(".sandbox-kids[data-kids=\"")
                    .plus(t.dot("dataset").dot("folder")).plus(str("\"]"))))
            .does(kids.and(kids.dot("classList").call("toggle", "collapsed"))));
    }

    /** Shared by the code-head «/» toggle and the sidebar's own « button. */
    private static Func tree() {
        Val hidden = v("hidden"), b = v("b");
        return func("sbTree", "hidden")
            .does(query(".sandbox-tree").dot("classList").call("toggle", "hidden", hidden))
            .var_("b", byId("sandbox-tree-toggle"))
            .does(b.dot("textContent").assign(hidden.ternary("»", "«")),
                  b.dot("title").assign(hidden.ternary("show files", "hide files")),
                  relineGutter(EDITOR));
    }

    private static Val treeToggleClick() {
        Val hidden = query(".sandbox-tree").dot("classList").call("contains", "hidden");
        return delegate(LAYOUT, "click", "#sandbox-tree-toggle")
            .handler(callback("e", "t").call("sbTree", hidden.not()));
    }

    private static Val treeCollapseClick() {
        return delegate(LAYOUT, "click", "#sandbox-tree-collapse")
            .handler(callback("e", "t").call("sbTree", true));
    }

    // ==================== knobs ====================

    /** A knob edit patches the source at the offsets the server reported. */
    private static Val knobEdits() {
        Val t = v("t"), start = v("start"), len = v("len"), val = v("val"),
            delta = v("delta"), kind = t.dot("dataset").dot("kind"), k = v("k");
        return delegate(LAYOUT, "input", ".sandbox-knob").handler(callback("e", "t")
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
                queryAll(".sandbox-knob").forEach(callback("k")
                    .if_(k.neq(t).and(parseInt(k.dot("dataset").dot("start")).gt(start)),
                        k.dot("dataset").dot("start")
                            .assign(parseInt(k.dot("dataset").dot("start")).plus(delta)))),
                EDITS.at(CUR).assign(E.dot("value")),
                BASE.assign(E.dot("value")),
                relineGutter(EDITOR),
                call("sbQueue", 250)));
    }

    private static Val resetClick() {
        return delegate(LAYOUT, "click", "#sandbox-reset").handler(callback("e", "t")
            .does(JSOperators.delete(EDITS, CUR))
            .call("sbSrc", CUR, callback("src").does(
                E.dot("value").assign(v("src")),
                BASE.assign(v("src")),
                relineGutter(EDITOR),
                call("sbRender"))));
    }

    // ==================== preview traffic lights ====================

    private static Val maximise() {
        return delegate(LAYOUT, "click", ".sandbox-dot-g").handler(callback("e", "t")
            .does(PV.dot("classList").call("toggle", "sandbox-max")));
    }

    private static Val restore() {
        return delegate(LAYOUT, "click", ".sandbox-dot-y").handler(callback("e", "t")
            .does(PV.dot("classList").call("remove", "sandbox-max")));
    }

    private static Val shake() {
        Val classes = PV.dot("classList");
        return delegate(LAYOUT, "click", ".sandbox-dot-r").handler(callback("e", "t")
            .does(classes.call("add", "sandbox-shake"),
                setTimeout(callback().does(classes.call("remove", "sandbox-shake")), 450)));
    }
}
