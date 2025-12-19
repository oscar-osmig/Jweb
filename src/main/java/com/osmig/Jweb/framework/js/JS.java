package com.osmig.Jweb.framework.js;

import java.util.ArrayList;
import java.util.List;

/**
 * Clean, fluent JavaScript DSL.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * Func formatTime = func("formatTime", "seconds")
 *     .var_("hrs", floor(variable("seconds").div(3600)))
 *     .ret(variable("hrs").padStart(2, "0"));
 *
 * Func startTimer = func("startTimer")
 *     .if_(variable("running"), ret())
 *     .set("running", true)
 *     .set("interval", setInterval(callback().inc("count").call("update"), 1000));
 *
 * String js = script()
 *     .var_("count", 0)
 *     .var_("running", false)
 *     .add(formatTime)
 *     .add(startTimer)
 *     .build();
 * </pre>
 */
public final class JS {

    private JS() {}

    // ==================== Entry Points ====================

    public static Script script() {
        return new Script();
    }

    public static Func func(String name, String... params) {
        return new Func(name, params);
    }

    public static Func callback(String... params) {
        return new Func(null, params);
    }

    // ==================== Values ====================

    /** Variable reference: variable("count") -> count */
    public static Val variable(String name) {
        return new Val(name);
    }

    /** String literal: str("hello") -> 'hello' */
    public static Val str(String value) {
        return new Val("'" + esc(value) + "'");
    }

    /** null */
    public static Val null_() {
        return new Val("null");
    }

    /** this */
    public static Val this_() {
        return new Val("this");
    }

    /** Array: array(1, 2, 3) -> [1,2,3] */
    public static Val array(Object... items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(toJs(items[i]));
        }
        return new Val(sb.append("]").toString());
    }

    // ==================== DOM ====================

    /** document.getElementById('id') */
    public static El getElem(String id) {
        return new El("document.getElementById('" + esc(id) + "')");
    }

    /** document.querySelector('selector') */
    public static El query(String selector) {
        return new El("document.querySelector('" + esc(selector) + "')");
    }

    // ==================== Math ====================

    public static Val floor(Val val) { return new Val("Math.floor(" + val.code + ")"); }
    public static Val ceil(Val val) { return new Val("Math.ceil(" + val.code + ")"); }
    public static Val round(Val val) { return new Val("Math.round(" + val.code + ")"); }
    public static Val abs(Val val) { return new Val("Math.abs(" + val.code + ")"); }
    public static Val random() { return new Val("Math.random()"); }

    // ==================== Calls ====================

    public static Val call(String fn, Object... args) {
        StringBuilder sb = new StringBuilder(fn).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(toJs(args[i]));
        }
        return new Val(sb.append(")").toString());
    }

    public static Val setInterval(Func fn, int ms) {
        return new Val("setInterval(" + fn.toExpr() + "," + ms + ")");
    }

    public static Val setTimeout(Func fn, int ms) {
        return new Val("setTimeout(" + fn.toExpr() + "," + ms + ")");
    }

    public static Val clearInterval(Val id) {
        return new Val("clearInterval(" + id.code + ")");
    }

    public static Val clearTimeout(Val id) {
        return new Val("clearTimeout(" + id.code + ")");
    }

    // ==================== Statements ====================

    public static Stmt ret() {
        return new Stmt("return");
    }

    public static Stmt ret(Object value) {
        return new Stmt("return " + toJs(value));
    }

    // ==================== Utilities ====================

    static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
    }

    static String toJs(Object o) {
        if (o == null) return "null";
        if (o instanceof Val val) return val.code;
        if (o instanceof Func f) return f.toExpr();
        if (o instanceof Integer i) return i.toString();
        if (o instanceof Double d) return d.toString();
        if (o instanceof Boolean b) return b.toString();
        if (o instanceof String s) return "'" + esc(s) + "'";
        return o.toString();
    }

    // ==================== Script ====================

    public static class Script {
        private final List<String> parts = new ArrayList<>();

        public Script var_(String name, Object value) {
            parts.add("var " + name + "=" + toJs(value));
            return this;
        }

        public Script let_(String name, Object value) {
            parts.add("let " + name + "=" + toJs(value));
            return this;
        }

        public Script const_(String name, Object value) {
            parts.add("const " + name + "=" + toJs(value));
            return this;
        }

        public Script add(Func fn) {
            parts.add(fn.toDecl());
            return this;
        }

        public Script raw(String js) {
            parts.add(js);
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                sb.append(part);
                if (!part.endsWith("}") && !part.endsWith(";")) {
                    sb.append(";");
                }
            }
            return sb.toString();
        }
    }

    // ==================== Function ====================

    public static class Func {
        private final String name;
        private final String[] params;
        private final List<String> body = new ArrayList<>();

        Func(String name, String... params) {
            this.name = name;
            this.params = params;
        }

        public Func var_(String name, Object value) {
            body.add("var " + name + "=" + toJs(value));
            return this;
        }

        public Func let_(String name, Object value) {
            body.add("let " + name + "=" + toJs(value));
            return this;
        }

        public Func set(String name, Object value) {
            body.add(name + "=" + toJs(value));
            return this;
        }

        public Func set(Val target, Object value) {
            body.add(target.code + "=" + toJs(value));
            return this;
        }

        public Func inc(String name) {
            body.add(name + "++");
            return this;
        }

        public Func dec(String name) {
            body.add(name + "--");
            return this;
        }

        public Func call(String fn, Object... args) {
            body.add(JS.call(fn, args).code);
            return this;
        }

        public Func log(Object... args) {
            body.add(JS.call("console.log", args).code);
            return this;
        }

        /** Simple if statement: if_(condition, stmt1, stmt2, ...) */
        public Func if_(Val condition, Object... thenStmts) {
            StringBuilder sb = new StringBuilder("if(").append(condition.code).append("){");
            for (Object s : thenStmts) appendStmt(sb, s);
            sb.append("}");
            body.add(sb.toString());
            return this;
        }

        /** Start an if/elif/else chain: if_(condition).then_(...).elif_(cond2).then_(...).else_(...).end() */
        public IfBuilder if_(Val condition) {
            return new IfBuilder(this, condition);
        }

        public Func ret() {
            body.add("return");
            return this;
        }

        public Func ret(Object value) {
            body.add("return " + toJs(value));
            return this;
        }

        public Func raw(String js) {
            body.add(js);
            return this;
        }

        String toExpr() {
            StringBuilder sb = new StringBuilder("function(");
            sb.append(String.join(",", params)).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith("}")) sb.append(";");
            }
            return sb.append("}").toString();
        }

        String toDecl() {
            StringBuilder sb = new StringBuilder("function ");
            if (name != null) sb.append(name);
            sb.append("(").append(String.join(",", params)).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith("}")) sb.append(";");
            }
            return sb.append("}").toString();
        }

        private void appendStmt(StringBuilder sb, Object s) {
            if (s instanceof Stmt st) sb.append(st.code).append(";");
            else if (s instanceof Val val) sb.append(val.code).append(";");
            else if (s instanceof String str) {
                sb.append(str);
                if (!str.endsWith(";") && !str.endsWith("}")) sb.append(";");
            }
        }
    }

    // ==================== If/Elif/Else Builder ====================

    /**
     * Builder for if/elif/else chains.
     *
     * <p>Usage:</p>
     * <pre>
     * func("example")
     *     .if_(condition1).then_(stmt1, stmt2)
     *     .elif_(condition2).then_(stmt3)
     *     .elif_(condition3).then_(stmt4)
     *     .else_(stmt5, stmt6)
     *     .end()
     * </pre>
     */
    public static class IfBuilder {
        private final Func parent;
        private final StringBuilder sb = new StringBuilder();
        private boolean needsThen = true;

        IfBuilder(Func parent, Val condition) {
            this.parent = parent;
            sb.append("if(").append(condition.code).append(")");
        }

        /** Statements to execute if condition is true */
        public IfBuilder then_(Object... stmts) {
            if (!needsThen) throw new IllegalStateException("then_() already called");
            sb.append("{");
            for (Object s : stmts) appendStmt(sb, s);
            sb.append("}");
            needsThen = false;
            return this;
        }

        /** Add an else-if branch */
        public IfBuilder elif_(Val condition) {
            if (needsThen) throw new IllegalStateException("then_() must be called before elif_()");
            sb.append("else if(").append(condition.code).append(")");
            needsThen = true;
            return this;
        }

        /** Add an else branch and finish the chain */
        public Func else_(Object... stmts) {
            if (needsThen) throw new IllegalStateException("then_() must be called before else_()");
            sb.append("else{");
            for (Object s : stmts) appendStmt(sb, s);
            sb.append("}");
            parent.body.add(sb.toString());
            return parent;
        }

        /** Finish the chain without an else branch */
        public Func end() {
            if (needsThen) throw new IllegalStateException("then_() must be called before end()");
            parent.body.add(sb.toString());
            return parent;
        }

        private void appendStmt(StringBuilder sb, Object s) {
            if (s instanceof Stmt st) sb.append(st.code).append(";");
            else if (s instanceof Val val) sb.append(val.code).append(";");
            else if (s instanceof String str) {
                sb.append(str);
                if (!str.endsWith(";") && !str.endsWith("}")) sb.append(";");
            }
        }
    }

    // ==================== Value ====================

    public static class Val {
        final String code;

        Val(String code) { this.code = code; }

        public String js() { return code; }

        public Val dot(String prop) { return new Val(code + "." + prop); }
        public Val at(int index) { return new Val(code + "[" + index + "]"); }
        public Val at(Val key) { return new Val(code + "[" + key.code + "]"); }

        public Val invoke(Object... args) {
            StringBuilder sb = new StringBuilder(code).append("(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJs(args[i]));
            }
            return new Val(sb.append(")").toString());
        }

        public Val plus(Object o) { return new Val("(" + code + "+" + toJs(o) + ")"); }
        public Val minus(Object o) { return new Val("(" + code + "-" + toJs(o) + ")"); }
        public Val times(Object o) { return new Val("(" + code + "*" + toJs(o) + ")"); }
        public Val div(Object o) { return new Val("(" + code + "/" + toJs(o) + ")"); }
        public Val mod(Object o) { return new Val("(" + code + "%" + toJs(o) + ")"); }

        public Val eq(Object o) { return new Val("(" + code + "===" + toJs(o) + ")"); }
        public Val neq(Object o) { return new Val("(" + code + "!==" + toJs(o) + ")"); }
        public Val gt(Object o) { return new Val("(" + code + ">" + toJs(o) + ")"); }
        public Val gte(Object o) { return new Val("(" + code + ">=" + toJs(o) + ")"); }
        public Val lt(Object o) { return new Val("(" + code + "<" + toJs(o) + ")"); }
        public Val lte(Object o) { return new Val("(" + code + "<=" + toJs(o) + ")"); }

        public Val and(Val o) { return new Val("(" + code + "&&" + o.code + ")"); }
        public Val or(Val o) { return new Val("(" + code + "||" + o.code + ")"); }
        public Val not() { return new Val("(!" + code + ")"); }
        public Val ternary(Object ifTrue, Object ifFalse) {
            return new Val("(" + code + "?" + toJs(ifTrue) + ":" + toJs(ifFalse) + ")");
        }

        public Val padStart(int len, String pad) {
            return new Val("String(" + code + ").padStart(" + len + ",'" + pad + "')");
        }
        public Val padEnd(int len, String pad) {
            return new Val("String(" + code + ").padEnd(" + len + ",'" + pad + "')");
        }
        public Val length() { return new Val(code + ".length"); }
        public Val trim() { return new Val(code + ".trim()"); }
        public Val toLowerCase() { return new Val(code + ".toLowerCase()"); }
        public Val toUpperCase() { return new Val(code + ".toUpperCase()"); }

        /** Assignment statement: variable("x").assign(5) -> x=5 */
        public Stmt assign(Object value) { return new Stmt(code + "=" + toJs(value)); }

        @Override
        public String toString() { return code; }
    }

    // ==================== Element ====================

    public static class El extends Val {
        El(String code) { super(code); }

        public Val text() { return new Val(code + ".textContent"); }
        public Val value() { return new Val(code + ".value"); }
        public Val html() { return new Val(code + ".innerHTML"); }
    }

    // ==================== Statement ====================

    public static class Stmt {
        final String code;
        Stmt(String code) { this.code = code; }
    }
}
