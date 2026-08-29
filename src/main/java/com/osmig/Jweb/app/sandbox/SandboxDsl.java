package com.osmig.Jweb.app.sandbox;

import jweb.Element;
import jweb.El;
import jweb.CSSValue;
import jweb.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static jweb.El.text;
import static jweb.Css.style;

/**
 * A safe interpreter for a subset of the JWeb DSL, powering the sandbox's
 * live editor. User code is tokenized and parsed into a tiny call-expression
 * AST, then evaluated against an explicit whitelist that builds real
 * framework Elements/Styles — so output goes through the framework's normal
 * escaping. There is no compilation, no reflection, and no way to reach any
 * class or method outside the whitelist; unknown names are compile-style
 * errors (with did-you-mean suggestions). Hard caps on source size, token
 * count, nesting depth, and node count bound the work per request.
 */
public final class SandboxDsl {
    private SandboxDsl() {}

    static final int MAX_SOURCE = 10_000;
    private static final int MAX_TOKENS = 4_000;
    private static final int MAX_DEPTH = 40;
    private static final int MAX_NODES = 600;
    private static final int MAX_KNOBS = 12;

    /** A tweakable literal found in the code: kind is text|color|number. */
    public record Knob(String kind, String label, String value, int start, int len, String unit) {}

    public record Result(Element element, List<Knob> knobs, String error, int errorLine) {
        static Result ok(Element e, List<Knob> knobs) { return new Result(e, knobs, null, 0); }
        static Result err(String msg, int line) { return new Result(null, List.of(), msg, line); }
        public boolean isOk() { return error == null; }
    }

    public static Result run(String source) {
        if (source == null) return Result.err("nothing to compile — the void does not render", 0);
        if (source.length() > MAX_SOURCE) {
            return Result.err("that's a lot of Java — keep it under " + MAX_SOURCE + " chars", 0);
        }
        try {
            List<Tok> toks = tokenize(source);
            int i = indexAfterReturn(toks);
            Parser p = new Parser(toks, i, source);
            SCall root = p.parseCall(0);
            p.expectEndOfExpression();
            Eval ev = new Eval(source);
            Object v = ev.evalElementArg(root);
            if (!(v instanceof Element el)) {
                throw new DslError("the expression must build an element like div(...)", root.pos());
            }
            return Result.ok(el, ev.knobs);
        } catch (DslError e) {
            return Result.err(e.getMessage(), lineOf(source, e.pos));
        }
    }

    private static int lineOf(String src, int pos) {
        int line = 1;
        for (int i = 0; i < Math.min(pos, src.length()); i++) if (src.charAt(i) == '\n') line++;
        return line;
    }

    // ==================== tokens ====================

    private enum T { IDENT, STR, NUM, LP, RP, COMMA, DOT, SEMI, OTHER, EOF }

    private record Tok(T t, String text, int pos, int len, boolean escaped) {}

    private static List<Tok> tokenize(String s) {
        List<Tok> out = new ArrayList<>();
        int i = 0, n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            if (c == '/' && i + 1 < n && s.charAt(i + 1) == '/') {
                while (i < n && s.charAt(i) != '\n') i++;
                continue;
            }
            if (c == '/' && i + 1 < n && s.charAt(i + 1) == '*') {
                int end = s.indexOf("*/", i + 2);
                i = end < 0 ? n : end + 2;
                continue;
            }
            if (out.size() >= MAX_TOKENS) throw new DslError("too many tokens — this is a sandbox, not a monolith", i);
            if (c == '"') {
                StringBuilder sb = new StringBuilder();
                boolean esc = false;
                int j = i + 1;
                while (j < n && s.charAt(j) != '"') {
                    char d = s.charAt(j);
                    if (d == '\\' && j + 1 < n) {
                        esc = true;
                        char e = s.charAt(j + 1);
                        sb.append(e == 'n' ? '\n' : e == 't' ? '\t' : e);
                        j += 2;
                    } else if (d == '\n') {
                        throw new DslError("unterminated string", i);
                    } else {
                        sb.append(d);
                        j++;
                    }
                }
                if (j >= n) throw new DslError("unterminated string", i);
                out.add(new Tok(T.STR, sb.toString(), i + 1, j - i - 1, esc));
                i = j + 1;
                continue;
            }
            if (Character.isDigit(c)) {
                int j = i;
                while (j < n && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.')) j++;
                out.add(new Tok(T.NUM, s.substring(i, j), i, j - i, false));
                i = j;
                continue;
            }
            if (Character.isJavaIdentifierStart(c)) {
                int j = i;
                while (j < n && Character.isJavaIdentifierPart(s.charAt(j))) j++;
                out.add(new Tok(T.IDENT, s.substring(i, j), i, j - i, false));
                i = j;
                continue;
            }
            T t = switch (c) {
                case '(' -> T.LP; case ')' -> T.RP; case ',' -> T.COMMA;
                case '.' -> T.DOT; case ';' -> T.SEMI; default -> T.OTHER;
            };
            out.add(new Tok(t, String.valueOf(c), i, 1, false));
            i++;
        }
        out.add(new Tok(T.EOF, "", n, 0, false));
        return out;
    }

    /** Files keep their class wrapper: evaluate the first return's expression.
     *  A bare expression (no return anywhere) is parsed from the top. */
    private static int indexAfterReturn(List<Tok> toks) {
        for (int i = 0; i < toks.size(); i++) {
            Tok t = toks.get(i);
            if (t.t() == T.IDENT && t.text().equals("return")) return i + 1;
        }
        return 0;
    }

    // ==================== AST + parser ====================

    private sealed interface Arg permits SCall, SStr, SNum, SIdent {}
    private record SCall(String name, int pos, List<Arg> args, List<SCall> chain) implements Arg {}
    private record SStr(String val, int start, int len, boolean escaped) implements Arg {}
    private record SNum(double val, String raw, int start, int len) implements Arg {}
    private record SIdent(String name, int pos) implements Arg {}

    static class DslError extends RuntimeException {
        final int pos;
        DslError(String msg, int pos) { super(msg); this.pos = pos; }
    }

    private static final class Parser {
        final List<Tok> toks;
        final String src;
        int i;
        int nodes = 0;

        Parser(List<Tok> toks, int start, String src) { this.toks = toks; this.i = start; this.src = src; }

        Tok peek() { return toks.get(Math.min(i, toks.size() - 1)); }
        Tok next() { return toks.get(Math.min(i++, toks.size() - 1)); }

        SCall parseCall(int depth) {
            if (depth > MAX_DEPTH) throw new DslError("nesting too deep — even Java has limits", peek().pos());
            Tok name = next();
            if (name.t() != T.IDENT) throw new DslError("expected a name like div( or style( here", name.pos());
            expect(T.LP, "expected ( after " + name.text());
            List<Arg> args = parseArgs(depth);
            List<SCall> chain = new ArrayList<>();
            while (peek().t() == T.DOT) {
                next();
                Tok m = next();
                if (m.t() != T.IDENT) throw new DslError("expected a method name after .", m.pos());
                expect(T.LP, "expected ( after ." + m.text());
                chain.add(new SCall(m.text(), m.pos(), parseArgs(depth), List.of()));
                countNode(m.pos());
            }
            countNode(name.pos());
            return new SCall(name.text(), name.pos(), args, chain);
        }

        private List<Arg> parseArgs(int depth) {
            List<Arg> args = new ArrayList<>();
            if (peek().t() == T.RP) { next(); return args; }
            while (true) {
                args.add(parseArg(depth + 1));
                Tok t = next();
                if (t.t() == T.RP) return args;
                if (t.t() != T.COMMA) throw new DslError("expected , or ) here", t.pos());
            }
        }

        private Arg parseArg(int depth) {
            Tok t = peek();
            switch (t.t()) {
                case STR -> { next(); return new SStr(t.text(), t.pos(), t.len(), t.escaped()); }
                case NUM -> {
                    next();
                    try {
                        return new SNum(Double.parseDouble(t.text()), t.text(), t.pos(), t.len());
                    } catch (NumberFormatException e) {
                        throw new DslError("that number didn't parse: " + t.text(), t.pos());
                    }
                }
                case IDENT -> {
                    if (toks.get(i + 1).t() == T.LP) return parseCall(depth);
                    next();
                    return new SIdent(t.text(), t.pos());
                }
                default -> throw new DslError("unexpected '" + t.text() + "' — arguments are calls, strings, numbers or names", t.pos());
            }
        }

        void expectEndOfExpression() {
            Tok t = peek();
            if (t.t() == T.SEMI || t.t() == T.EOF) return;
            throw new DslError("expected ; after the expression (found '" + t.text() + "')", t.pos());
        }

        private void expect(T type, String msg) {
            Tok t = next();
            if (t.t() != type) throw new DslError(msg, t.pos());
        }

        private void countNode(int pos) {
            if (++nodes > MAX_NODES) throw new DslError("too many calls — the preview pane is only so big", pos);
        }
    }

    // ==================== whitelists ====================

    private static final Set<String> ELEMENTS = Set.of(
        "div", "span", "p", "h1", "h2", "h3", "h4", "h5", "h6", "button",
        "ul", "ol", "li", "em", "strong", "section", "article", "small",
        "blockquote", "code", "pre");
    private static final Set<String> VOID_ELEMENTS = Set.of("br", "hr");

    private static final Map<String, String> IDENT_CSS = Map.ofEntries(
        Map.entry("white", "white"), Map.entry("black", "black"),
        Map.entry("transparent", "transparent"),
        Map.entry("solid", "solid"), Map.entry("dashed", "dashed"), Map.entry("dotted", "dotted"),
        Map.entry("pointer", "pointer"),
        Map.entry("center", "center"), Map.entry("left", "left"), Map.entry("right", "right"),
        Map.entry("flex", "flex"), Map.entry("block", "block"),
        Map.entry("column", "column"), Map.entry("row", "row"),
        Map.entry("flexStart", "flex-start"), Map.entry("flexEnd", "flex-end"),
        Map.entry("spaceBetween", "space-between"), Map.entry("spaceAround", "space-around"),
        Map.entry("none", "none"), Map.entry("underline", "underline"));

    private static final Set<String> UNITS = Set.of("rem", "px", "em", "percent", "vw", "vh");

    /** Style chain methods: css property + how many/which args they accept. */
    private record StyleMethod(String cssProp, ArgKind kind) {}

    private enum ArgKind { SIZES_1_2, SIZE_1, COLOR_1, BORDER_3, NUMBER_1, SHADOW_STR, ENUM_1 }

    private static final Map<String, StyleMethod> STYLE_METHODS = Map.ofEntries(
        Map.entry("padding", new StyleMethod("padding", ArgKind.SIZES_1_2)),
        Map.entry("margin", new StyleMethod("margin", ArgKind.SIZES_1_2)),
        Map.entry("paddingTop", new StyleMethod("padding-top", ArgKind.SIZE_1)),
        Map.entry("paddingBottom", new StyleMethod("padding-bottom", ArgKind.SIZE_1)),
        Map.entry("marginTop", new StyleMethod("margin-top", ArgKind.SIZE_1)),
        Map.entry("marginBottom", new StyleMethod("margin-bottom", ArgKind.SIZE_1)),
        Map.entry("background", new StyleMethod("background", ArgKind.COLOR_1)),
        Map.entry("backgroundColor", new StyleMethod("background-color", ArgKind.COLOR_1)),
        Map.entry("color", new StyleMethod("color", ArgKind.COLOR_1)),
        Map.entry("border", new StyleMethod("border", ArgKind.BORDER_3)),
        Map.entry("borderRadius", new StyleMethod("border-radius", ArgKind.SIZE_1)),
        Map.entry("fontSize", new StyleMethod("font-size", ArgKind.SIZE_1)),
        Map.entry("fontWeight", new StyleMethod("font-weight", ArgKind.NUMBER_1)),
        Map.entry("lineHeight", new StyleMethod("line-height", ArgKind.NUMBER_1)),
        Map.entry("opacity", new StyleMethod("opacity", ArgKind.NUMBER_1)),
        Map.entry("letterSpacing", new StyleMethod("letter-spacing", ArgKind.SIZE_1)),
        Map.entry("boxShadow", new StyleMethod("box-shadow", ArgKind.SHADOW_STR)),
        Map.entry("textAlign", new StyleMethod("text-align", ArgKind.ENUM_1)),
        Map.entry("textDecoration", new StyleMethod("text-decoration", ArgKind.ENUM_1)),
        Map.entry("display", new StyleMethod("display", ArgKind.ENUM_1)),
        Map.entry("flexDirection", new StyleMethod("flex-direction", ArgKind.ENUM_1)),
        Map.entry("alignItems", new StyleMethod("align-items", ArgKind.ENUM_1)),
        Map.entry("justifyContent", new StyleMethod("justify-content", ArgKind.ENUM_1)),
        Map.entry("gap", new StyleMethod("gap", ArgKind.SIZE_1)),
        Map.entry("cursor", new StyleMethod("cursor", ArgKind.ENUM_1)),
        Map.entry("width", new StyleMethod("width", ArgKind.SIZE_1)),
        Map.entry("maxWidth", new StyleMethod("max-width", ArgKind.SIZE_1)),
        Map.entry("height", new StyleMethod("height", ArgKind.SIZE_1)));

    private static final String SHADOW_OK = "[a-zA-Z0-9#., ()%-]{0,120}";

    // ==================== evaluator ====================

    private static final class Eval {
        final String src;
        final List<Knob> knobs = new ArrayList<>();
        int elementCount = 0;

        Eval(String src) { this.src = src; }

        /** Evaluate a call in element-child position: element, text() or style(). */
        Object evalElementArg(SCall c) {
            if (c.name().equals("text")) {
                SStr s = oneString(c, "text(...) takes exactly one string");
                return text(s.val());
            }
            if (c.name().equals("style")) {
                if (!c.args().isEmpty()) throw new DslError("style() takes no arguments — chain methods after it", c.pos());
                jweb.Style<?> st = style();
                for (SCall m : c.chain()) applyStyleMethod(st, m);
                return st;
            }
            if (VOID_ELEMENTS.contains(c.name())) {
                requireNoChain(c);
                if (!c.args().isEmpty()) throw new DslError(c.name() + "() takes no arguments", c.pos());
                return countElement(El.tag(c.name()), c);
            }
            if (ELEMENTS.contains(c.name())) {
                requireNoChain(c);
                List<Object> parts = new ArrayList<>();
                for (Arg a : c.args()) {
                    switch (a) {
                        case SStr s -> { captureTextKnob(c.name(), s); parts.add(text(s.val())); }
                        case SCall sub -> {
                            if (sub.name().equals("text")) {
                                SStr s = oneString(sub, "text(...) takes exactly one string");
                                captureTextKnob(c.name(), s);
                                parts.add(text(s.val()));
                            } else {
                                parts.add(evalElementArg(sub));
                            }
                        }
                        case SNum num -> throw new DslError("a bare number can't be a child — wrap text in text(\"...\")", pos(num));
                        case SIdent id -> throw new DslError(unknownIn(id.name(), "as an element child", ELEMENTS), id.pos());
                    }
                }
                return countElement(El.tag(c.name(), parts.toArray()), c);
            }
            throw new DslError(unknownIn(c.name(), "here", elementCandidates()), c.pos());
        }

        private Element countElement(Element e, SCall c) {
            if (++elementCount > 200) throw new DslError("that's over 200 elements — the preview called HR", c.pos());
            return e;
        }

        private void requireNoChain(SCall c) {
            if (!c.chain().isEmpty()) {
                throw new DslError("methods can only be chained on style(), not on " + c.name() + "()", c.chain().get(0).pos());
            }
        }

        // ---------- style ----------

        private void applyStyleMethod(jweb.Style<?> st, SCall m) {
            StyleMethod sm = STYLE_METHODS.get(m.name());
            if (sm == null) {
                throw new DslError(unknownIn("." + m.name(), "on style()", STYLE_METHODS.keySet()), m.pos());
            }
            String cssText = switch (sm.kind()) {
                case SIZES_1_2 -> {
                    if (m.args().isEmpty() || m.args().size() > 2) {
                        throw new DslError("." + m.name() + "() takes 1 or 2 sizes like rem(1)", m.pos());
                    }
                    StringBuilder sb = new StringBuilder();
                    for (Arg a : m.args()) sb.append(sb.isEmpty() ? "" : " ").append(sizeCss(a, m.name()));
                    yield sb.toString();
                }
                case SIZE_1 -> sizeCss(only(m), m.name());
                case COLOR_1 -> colorCss(only(m), m.name());
                case BORDER_3 -> {
                    if (m.args().size() != 3) {
                        throw new DslError(".border() wants (px(1), solid, hex(\"#e2e8f0\"))", m.pos());
                    }
                    yield sizeCss(m.args().get(0), m.name()) + " "
                        + enumCss(m.args().get(1), Set.of("solid", "dashed", "dotted")) + " "
                        + colorCss(m.args().get(2), m.name());
                }
                case NUMBER_1 -> {
                    Arg a = only(m);
                    if (!(a instanceof SNum num)) throw new DslError("." + m.name() + "() wants a plain number", m.pos());
                    double v = num.val();
                    if (v < 0 || v > 1000) throw new DslError("." + m.name() + "(" + num.raw() + ")? let's stay between 0 and 1000", pos(num));
                    yield num.raw();
                }
                case SHADOW_STR -> {
                    Arg a = only(m);
                    if (!(a instanceof SStr s)) throw new DslError("." + m.name() + "() wants one quoted string", m.pos());
                    if (!s.val().matches(SHADOW_OK)) {
                        throw new DslError("." + m.name() + "() only accepts simple values (letters, digits, # . , ( ) % -)", m.pos());
                    }
                    yield s.val();
                }
                case ENUM_1 -> enumCss(only(m), IDENT_CSS.keySet());
            };
            st.prop(sm.cssProp(), cssText);
        }

        private Arg only(SCall m) {
            if (m.args().size() != 1) throw new DslError("." + m.name() + "() takes exactly one argument", m.pos());
            return m.args().get(0);
        }

        private String enumCss(Arg a, Set<String> allowed) {
            if (a instanceof SIdent id && allowed.contains(id.name()) && IDENT_CSS.containsKey(id.name())) {
                return IDENT_CSS.get(id.name());
            }
            int p = pos(a);
            String got = a instanceof SIdent id ? id.name() : "that";
            throw new DslError(unknownIn(got, "here", allowed), p);
        }

        // ---------- sizes & colors ----------

        private String sizeCss(Arg a, String methodName) {
            if (a instanceof SCall c && UNITS.contains(c.name()) && c.chain().isEmpty()) {
                Arg inner = c.args().size() == 1 ? c.args().get(0) : null;
                if (!(inner instanceof SNum num)) {
                    throw new DslError(c.name() + "() wants one number, e.g. " + c.name() + "(1)", c.pos());
                }
                double v = num.val();
                double max = c.name().equals("px") ? 2000 : 200;
                if (v < 0 || v > max) throw new DslError(c.name() + "(" + num.raw() + ") is outside 0–" + (int) max, pos(num));
                captureNumberKnob(methodName, num, c.name());
                String u = switch (c.name()) {
                    case "percent" -> "%";
                    default -> c.name();
                };
                yieldCheck(num.raw());
                return trimNum(num.raw()) + u;
            }
            if (a instanceof SIdent id && id.name().equals("zero")) return "0";
            throw new DslError(unknownIn(describe(a), "as a size — try rem(1) or px(12)", UNITS), pos(a));
        }

        private static void yieldCheck(String raw) {
            if (raw.chars().filter(ch -> ch == '.').count() > 1) {
                throw new DslError("that number has too many dots: " + raw, 0);
            }
        }

        private static String trimNum(String raw) {
            return raw.endsWith(".0") ? raw.substring(0, raw.length() - 2) : raw;
        }

        private String colorCss(Arg a, String methodName) {
            if (a instanceof SIdent id) {
                String css = IDENT_CSS.get(id.name());
                if (css != null && Set.of("white", "black", "transparent").contains(id.name())) return css;
                throw new DslError(unknownIn(id.name(), "as a color", Set.of("white", "black", "transparent", "hex", "rgb", "rgba")), id.pos());
            }
            if (a instanceof SCall c) {
                switch (c.name()) {
                    case "hex" -> {
                        SStr s = oneString(c, "hex() wants one string like hex(\"#4f46e5\")");
                        if (!s.val().matches("#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})")) {
                            throw new DslError("that's not a hex color: \"" + clip(s.val()) + "\" — try \"#4f46e5\"", s.start());
                        }
                        captureColorKnob(methodName, s);
                        return s.val().toLowerCase();
                    }
                    case "rgb", "rgba" -> {
                        int want = c.name().equals("rgb") ? 3 : 4;
                        if (c.args().size() != want) throw new DslError(c.name() + "() wants " + want + " numbers", c.pos());
                        StringBuilder sb = new StringBuilder(c.name()).append('(');
                        for (int k = 0; k < c.args().size(); k++) {
                            if (!(c.args().get(k) instanceof SNum num)) throw new DslError(c.name() + "() wants plain numbers", c.pos());
                            double max = (k == 3) ? 1 : 255;
                            if (num.val() < 0 || num.val() > max) throw new DslError("channel out of range in " + c.name() + "()", pos(num));
                            sb.append(k > 0 ? ", " : "").append(trimNum(num.raw()));
                        }
                        return sb.append(')').toString();
                    }
                    case "linearGradient" -> {
                        if (c.args().size() < 3) throw new DslError("linearGradient(\"90deg\", color, color, ...) needs an angle and 2+ colors", c.pos());
                        if (!(c.args().get(0) instanceof SStr ang) || !ang.val().matches("-?\\d{1,3}deg")) {
                            throw new DslError("the first gradient argument is an angle string like \"90deg\"", c.pos());
                        }
                        StringBuilder sb = new StringBuilder("linear-gradient(").append(ang.val());
                        for (int k = 1; k < c.args().size(); k++) {
                            sb.append(", ").append(colorCss(c.args().get(k), "gradient"));
                        }
                        return sb.append(')').toString();
                    }
                    default -> throw new DslError(unknownIn(c.name(), "as a color", Set.of("hex", "rgb", "rgba", "linearGradient")), c.pos());
                }
            }
            throw new DslError("expected a color here — hex(\"#4f46e5\"), white, rgba(0,0,0,0.5)...", pos(a));
        }

        // ---------- knob capture ----------

        private void captureTextKnob(String elementName, SStr s) {
            if (knobs.size() >= MAX_KNOBS || s.escaped() || s.val().length() > 60) return;
            knobs.add(new Knob("text", elementName, s.val(), s.start(), s.len(), null));
        }

        private void captureColorKnob(String methodName, SStr s) {
            if (knobs.size() >= MAX_KNOBS || s.escaped() || s.val().length() != 7) return;
            knobs.add(new Knob("color", methodName, s.val().toLowerCase(), s.start(), s.len(), null));
        }

        private void captureNumberKnob(String methodName, SNum num, String unit) {
            if (knobs.size() >= MAX_KNOBS) return;
            if (!Set.of("padding", "margin", "fontSize", "borderRadius", "gap").contains(methodName)) return;
            knobs.add(new Knob("number", methodName, trimNum(num.raw()), num.start(), num.len(), unit));
        }

        // ---------- helpers ----------

        private static SStr oneString(SCall c, String msg) {
            if (c.args().size() == 1 && c.args().get(0) instanceof SStr s) return s;
            throw new DslError(msg, c.pos());
        }

        private static int pos(Arg a) {
            return switch (a) {
                case SCall c -> c.pos();
                case SStr s -> s.start();
                case SNum n -> n.start();
                case SIdent i -> i.pos();
            };
        }

        private static String describe(Arg a) {
            return switch (a) {
                case SCall c -> c.name();
                case SStr s -> "\"" + clip(s.val()) + "\"";
                case SNum n -> n.raw();
                case SIdent i -> i.name();
            };
        }

        private static String clip(String s) {
            return s.length() > 20 ? s.substring(0, 20) + "…" : s;
        }

        private static Set<String> elementCandidates() {
            Set<String> all = new java.util.HashSet<>(ELEMENTS);
            all.addAll(VOID_ELEMENTS);
            all.add("text");
            all.add("style");
            return all;
        }

        private static String unknownIn(String got, String where, Set<String> candidates) {
            String base = "unknown '" + got + "' " + where;
            String best = null;
            int bestD = 3;
            for (String c : candidates) {
                int d = levenshtein(got.replace(".", ""), c);
                if (d < bestD) { bestD = d; best = c; }
            }
            return best != null ? base + " — did you mean " + best + "?" : base;
        }

        private static int levenshtein(String a, String b) {
            int[] prev = new int[b.length() + 1];
            int[] cur = new int[b.length() + 1];
            for (int j = 0; j <= b.length(); j++) prev[j] = j;
            for (int i = 1; i <= a.length(); i++) {
                cur[0] = i;
                for (int j = 1; j <= b.length(); j++) {
                    int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                    cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
                }
                int[] tmp = prev; prev = cur; cur = tmp;
            }
            return prev[b.length()];
        }
    }
}
