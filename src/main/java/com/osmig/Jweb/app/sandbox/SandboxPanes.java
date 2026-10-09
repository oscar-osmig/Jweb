package com.osmig.Jweb.app.sandbox;

import jweb.Cls;
import jweb.Element;
import jweb.Id;
import com.osmig.Jweb.app.sandbox.SandboxDsl.Knob;
import com.osmig.Jweb.app.sandbox.SandboxDsl.Result;
import com.osmig.Jweb.app.sandbox.SandboxFiles.Mode;
import com.osmig.Jweb.app.sandbox.SandboxFiles.SandboxFile;

import java.util.List;
import java.util.Locale;

import static jweb.El.*;

/**
 * Server-rendered halves of the live editor loop. POST /sandbox/render sends
 * {file, code} and gets back three fragments by id: rx-dynbar (knobs derived
 * from the code), rx-view (the rendered preview) and rx-status (compile
 * message). On a DSL error only rx-status carries data — the client keeps
 * the last good preview on screen while the user is mid-keystroke.
 */
public final class SandboxPanes {
    private SandboxPanes() {}

    /** The three fragments a render returns; the client picks them out by id. */
    public static final Id RX_DYNBAR = id("rx-dynbar");
    public static final Id RX_VIEW = id("rx-view");
    public static final Id RX_STATUS = id("rx-status");

    /** The dynbar's markup — styled by {@link SandboxPage}, driven by {@link SandboxScript}. */
    public static final Cls BLURB = cls("sandbox-blurb");
    public static final Cls CONTROLS = cls("sandbox-controls");
    public static final Cls KNOB_LABEL = cls("sandbox-knob-label");
    public static final Cls KNOB = cls("sandbox-knob");
    public static final Cls CHIP = cls("sandbox-chip");
    public static final Cls CHIP_PRIMARY = cls("sandbox-chip-primary");
    public static final Id RESET = id("sandbox-reset");
    public static final Id ADD_SNIPPET = id("sandbox-add-snippet");

    private static final String[] PRAISE = {
        "ship it 🚀", "zero red squiggles", "mother would be proud",
        "artisanal, hand-typed Java", "builds on your machine too",
        "the JVM smiled", "not a single YAML was harmed",
    };

    /** The whole POST response. */
    public static Element renderFragment(String fileId, String code) {
        SandboxFile f = SandboxFiles.byId(fileId);
        String src = code != null ? code : f.source();
        if (f.mode() == Mode.STATIC) {
            return fragment(
                div(RX_DYNBAR, dynbarContent(f, List.of())),
                div(RX_VIEW, SandboxFiles.staticPreview(f.id())),
                status(true, "ℹ this file just boots things — write UI in pages/ or components/"));
        }
        long t0 = System.nanoTime();
        Result r = SandboxDsl.run(src);
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        if (!r.isOk()) {
            String where = r.errorLine() > 0 ? "line " + r.errorLine() + ": " : "";
            return fragment(status(false, "✗ " + where + r.error()));
        }
        String praise = PRAISE[Math.floorMod(src.hashCode(), PRAISE.length)];
        return fragment(
            div(RX_DYNBAR, dynbarContent(f, r.knobs())),
            div(RX_VIEW, r.element()),
            status(true, String.format(Locale.ROOT, "✓ compiled in %.2fms — %s", ms, praise)));
    }

    private static Element status(boolean ok, String msg) {
        return div(RX_STATUS, attrs().data("ok", ok ? "1" : "0"), msg);
    }

    /**
     * Blurb + knobs derived from the current code + the reset chip, and on a
     * DSL file the "Add snippet" chip that opens the page's submission panel.
     */
    public static Element dynbarContent(SandboxFile f, List<Knob> knobs) {
        return fragment(
            div(BLURB, f.blurb()),
            div(CONTROLS,
                each(indexed(knobs), k -> knobControl(k.knob(), k.i())),
                button(RESET, CHIP, attrs().type("button"), "↺ Reset file"),
                when(f.mode() == Mode.DSL, () ->
                    button(ADD_SNIPPET, CHIP, CHIP_PRIMARY, attrs().type("button"),
                        "＋ Add snippet"))));
    }

    private record IndexedKnob(Knob knob, int i) {}

    private static List<IndexedKnob> indexed(List<Knob> knobs) {
        return java.util.stream.IntStream.range(0, knobs.size())
            .mapToObj(i -> new IndexedKnob(knobs.get(i), i)).toList();
    }

    private static Element knobControl(Knob k, int ordinal) {
        String id = k.kind() + ":" + k.label() + ":" + ordinal;
        var a = attrs()
            .data("id", id).data("kind", k.kind())
            .data("start", String.valueOf(k.start()))
            .data("len", String.valueOf(k.len()))
            .aria("label", k.label());
        Element control = switch (k.kind()) {
            case "color" -> input(KNOB, a.type("color").value(k.value()));
            case "number" -> input(KNOB, a.type("number").value(k.value())
                .set("step", "rem".equals(k.unit()) ? "0.25" : "em".equals(k.unit()) ? "0.1" : "1")
                .set("min", "0")
                .set("max", "px".equals(k.unit()) ? "2000" : "200"));
            default -> input(KNOB, a.type("text").value(k.value()).maxlength(60));
        };
        return label(KNOB_LABEL, span(k.label()), control);
    }

    /** Initial page content for the default file (mirrors the POST response). */
    public static Element initialDynbar(SandboxFile f, Result r) {
        return dynbarContent(f, r != null && r.isOk() ? r.knobs() : List.of());
    }

    public static Element initialView(SandboxFile f, Result r) {
        if (f.mode() == Mode.STATIC) return SandboxFiles.staticPreview(f.id());
        return r != null && r.isOk() ? r.element()
            : div(BLURB, "…");
    }
}
