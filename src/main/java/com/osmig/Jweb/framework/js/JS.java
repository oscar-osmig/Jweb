package com.osmig.Jweb.framework.js;

import jweb.Action;
import jweb.Func;
import jweb.Val;
import jweb.js.Stmt;

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
 *     .var_("hrs", floor(v("seconds").div(3600)))
 *     .return_(v("hrs").padStart(2, "0"));
 *
 * Func startTimer = func("startTimer")
 *     .if_(v("running"), return_())
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
 *
 * @deprecated Replaced by {@code jweb.Js} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class JS extends Events {

    protected JS() {}

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

    /** Variable reference, short form: {@code v("count")} -> {@code count} */
    public static Val v(String name) {
        return new Val(name);
    }

    /**
     * Raw expression: expr("myVar.foo") -> myVar.foo
     * Use when you need to reference a JS expression not covered by the DSL.
     */
    public static Val expr(String rawExpr) {
        return new Val(rawExpr);
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

    // ==================== Object Literals ====================

    /**
     * Creates a JavaScript object literal.
     *
     * <p>Example:</p>
     * <pre>
     * obj("name", "John", "age", 30)
     * // Output: {name:'John',age:30}
     *
     * obj("user", obj("id", 1, "name", "Alice"))
     * // Output: {user:{id:1,name:'Alice'}}
     * </pre>
     *
     * @param pairs alternating key-value pairs (key1, val1, key2, val2, ...)
     * @return a Val representing the object literal
     */
    public static Val obj(Object... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("obj() requires an even number of arguments (key-value pairs)");
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < pairs.length; i += 2) {
            if (i > 0) sb.append(",");
            sb.append(pairs[i]).append(":").append(toJs(pairs[i + 1]));
        }
        return new Val(sb.append("}").toString());
    }

    /**
     * Creates a JavaScript object using an ObjectBuilder.
     *
     * <p>Example:</p>
     * <pre>
     * object()
     *     .prop("name", "John")
     *     .prop("age", 30)
     *     .prop("active", true)
     *     .build()
     * </pre>
     *
     * @return a new ObjectBuilder
     */
    public static ObjectBuilder object() {
        return new ObjectBuilder();
    }

    /**
     * Builder for complex JavaScript objects.
     */
    public static class ObjectBuilder {
        private final List<String> props = new ArrayList<>();

        /** Adds a property with a static value. */
        public ObjectBuilder prop(String key, Object value) {
            props.add(key + ":" + toJs(value));
            return this;
        }

        /** Adds a property with a computed key. */
        public ObjectBuilder computedProp(Val keyExpr, Object value) {
            props.add("[" + keyExpr.js() + "]:" + toJs(value));
            return this;
        }

        /** Adds a method to the object. */
        public ObjectBuilder method(String name, Func fn) {
            props.add(name + ":" + fn.toExpr());
            return this;
        }

        /** Adds a shorthand property (variable name becomes both key and value). */
        public ObjectBuilder shorthand(String varName) {
            props.add(varName);
            return this;
        }

        /** Spreads another object's properties. */
        public ObjectBuilder spread(Val obj) {
            props.add("..." + obj.js());
            return this;
        }

        /** Builds the object literal. */
        public Val build() {
            return new Val("{" + String.join(",", props) + "}");
        }
    }

    // ==================== DOM ====================

    /** document.getElementById('id') */
    public static El byId(String id) {
        return new El("document.getElementById('" + esc(id) + "')");
    }

    /** document.getElementById('id') — prefer {@link #byId(String)} (matches the platform name). */
    public static El getElem(String id) {
        return byId(id);
    }

    /** Shorthand for getElem - simpler to write: $("myId") instead of getElem("myId") */
    public static El $(String id) {
        return getElem(id);
    }

    /** document.querySelector('selector') */
    public static El query(String selector) {
        return new El("document.querySelector('" + esc(selector) + "')");
    }

    /** document.querySelectorAll('selector') */
    public static Val queryAll(String selector) {
        return new Val("document.querySelectorAll('" + esc(selector) + "')");
    }

    // ==================== Viewport & Media Queries ====================

    /** {@code window.matchMedia(query)} — a live MediaQueryList for the given query. */
    public static Val matchMedia(String query) {
        return new Val("window.matchMedia('" + esc(query) + "')");
    }

    /**
     * {@code window.matchMedia('(prefers-reduced-motion: reduce)').matches} —
     * true when the user has asked the OS to minimize animation.
     */
    public static Val reducedMotion() {
        return new Val("window.matchMedia('(prefers-reduced-motion: reduce)').matches");
    }

    /** {@code window.innerWidth} — the viewport width in CSS pixels. */
    public static Val viewportWidth() {
        return new Val("window.innerWidth");
    }

    /** {@code window.innerHeight} — the viewport height in CSS pixels. */
    public static Val viewportHeight() {
        return new Val("window.innerHeight");
    }

    // ==================== Math ====================

    public static Val floor(Val val) { return new Val("Math.floor(" + val.js() + ")"); }
    public static Val ceil(Val val) { return new Val("Math.ceil(" + val.js() + ")"); }
    public static Val round(Val val) { return new Val("Math.round(" + val.js() + ")"); }
    public static Val abs(Val val) { return new Val("Math.abs(" + val.js() + ")"); }
    public static Val random() { return new Val("Math.random()"); }

    // ==================== Number Parsing & Checking ====================

    /** Parses integer: parseInt(value) */
    public static Val parseInt(Val value) { return new Val("parseInt(" + value.js() + ")"); }

    /** Parses integer with radix: parseInt(value, radix) */
    public static Val parseInt(Val value, int radix) { return new Val("parseInt(" + value.js() + "," + radix + ")"); }

    /** Parses float: parseFloat(value) */
    public static Val parseFloat(Val value) { return new Val("parseFloat(" + value.js() + ")"); }

    /** Checks if NaN: isNaN(value) */
    public static Val isNaN(Val value) { return new Val("isNaN(" + value.js() + ")"); }

    /** Checks if finite: isFinite(value) */
    public static Val isFinite(Val value) { return new Val("isFinite(" + value.js() + ")"); }

    /** Number.isNaN - strict NaN check */
    public static Val numberIsNaN(Val value) { return new Val("Number.isNaN(" + value.js() + ")"); }

    /** Number.isFinite - strict finite check */
    public static Val numberIsFinite(Val value) { return new Val("Number.isFinite(" + value.js() + ")"); }

    /** Number.isInteger - checks if integer */
    public static Val numberIsInteger(Val value) { return new Val("Number.isInteger(" + value.js() + ")"); }

    /** Number.isSafeInteger - checks if safe integer */
    public static Val numberIsSafeInteger(Val value) { return new Val("Number.isSafeInteger(" + value.js() + ")"); }

    // ==================== Object Static Methods ====================

    /** Object.assign: Object.assign(target, ...sources) */
    public static Val objectAssign(Val target, Val... sources) {
        StringBuilder sb = new StringBuilder("Object.assign(" + target.js());
        for (Val source : sources) sb.append(",").append(source.js());
        return new Val(sb.append(")").toString());
    }

    /** Object.freeze: Object.freeze(obj) */
    public static Val objectFreeze(Val obj) { return new Val("Object.freeze(" + obj.js() + ")"); }

    /** Object.seal: Object.seal(obj) */
    public static Val objectSeal(Val obj) { return new Val("Object.seal(" + obj.js() + ")"); }

    /** Object.is: Object.is(val1, val2) */
    public static Val objectIs(Val val1, Val val2) { return new Val("Object.is(" + val1.js() + "," + val2.js() + ")"); }

    /** Object.create: Object.create(proto) */
    public static Val objectCreate(Val proto) { return new Val("Object.create(" + proto.js() + ")"); }

    /** Object.getOwnPropertyNames: Object.getOwnPropertyNames(obj) */
    public static Val objectGetOwnPropertyNames(Val obj) { return new Val("Object.getOwnPropertyNames(" + obj.js() + ")"); }

    /** Object.getPrototypeOf: Object.getPrototypeOf(obj) */
    public static Val objectGetPrototypeOf(Val obj) { return new Val("Object.getPrototypeOf(" + obj.js() + ")"); }

    /** Object.setPrototypeOf: Object.setPrototypeOf(obj, proto) */
    public static Val objectSetPrototypeOf(Val obj, Val proto) { return new Val("Object.setPrototypeOf(" + obj.js() + "," + proto.js() + ")"); }

    /** Object.isFrozen: Object.isFrozen(obj) */
    public static Val objectIsFrozen(Val obj) { return new Val("Object.isFrozen(" + obj.js() + ")"); }

    /** Object.isSealed: Object.isSealed(obj) */
    public static Val objectIsSealed(Val obj) { return new Val("Object.isSealed(" + obj.js() + ")"); }

    /** Object.fromEntries: Object.fromEntries(entries) */
    public static Val objectFromEntries(Val entries) { return new Val("Object.fromEntries(" + entries.js() + ")"); }

    // ==================== Array Static Methods ====================

    /** Array.from: Array.from(arrayLike) */
    public static Val arrayFrom(Val arrayLike) { return new Val("Array.from(" + arrayLike.js() + ")"); }

    /** Array.from with mapper: Array.from(arrayLike, mapFn) */
    public static Val arrayFrom(Val arrayLike, Func mapper) { return new Val("Array.from(" + arrayLike.js() + "," + mapper.toExpr() + ")"); }

    /** Array.isArray: Array.isArray(value) */
    public static Val arrayIsArray(Val value) { return new Val("Array.isArray(" + value.js() + ")"); }

    /** Array.of: Array.of(...items) */
    public static Val arrayOf(Object... items) {
        StringBuilder sb = new StringBuilder("Array.of(");
        for (int i = 0; i < items.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(toJs(items[i]));
        }
        return new Val(sb.append(")").toString());
    }

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
        return new Val("clearInterval(" + id.js() + ")");
    }

    public static Val clearTimeout(Val id) {
        return new Val("clearTimeout(" + id.js() + ")");
    }

    // ==================== Statements ====================

    /** {@code return} — matches the {@code if_}/{@code while_} keyword convention. */
    public static Stmt return_() {
        return new Stmt("return");
    }

    /** {@code return <value>} */
    public static Stmt return_(Object value) {
        return new Stmt("return " + toJs(value));
    }

    /** @deprecated Use {@link #return_()} — every Java-keyword name in this DSL ends with {@code _}. */
    @Deprecated
    public static Stmt ret() {
        return new Stmt("return");
    }

    /** @deprecated Use {@link #return_(Object)} — every Java-keyword name in this DSL ends with {@code _}. */
    @Deprecated
    public static Stmt ret(Object value) {
        return new Stmt("return " + toJs(value));
    }

    // ==================== Utilities ====================

    public static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
    }

    /**
     * Renders one statement for a builder body — the single place that knows
     * how each DSL value becomes a statement. A {@link Stmt}, {@link Action},
     * {@link Val} or String is emitted as-is and terminated; a {@link Func}
     * becomes a function declaration. This is what {@code does(...)} takes,
     * on every builder that has it.
     *
     * @param o the statement value
     * @return the JavaScript, ending in {@code ;}
     */
    public static String toStatement(Object o) {
        if (o == null) return "";
        String js = o instanceof Stmt st ? st.js()
            : o instanceof Action a ? a.build()
            : o instanceof Val v ? v.js()
            : o instanceof Func f ? f.toDecl()
            : String.valueOf(o);
        if (js.isEmpty()) return "";
        // Always terminate: an expression ending in "}" ("x=y||{}") would
        // otherwise merge with the next statement. A ";" after a block is a
        // harmless empty statement.
        return js.endsWith(";") ? js : js + ";";
    }

    public static String toJs(Object o) {
        if (o == null) return "null";
        if (o instanceof Val val) return val.js();
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

        public Script let(String name, Object value) {
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

        public Script add(Async.AsyncFunc fn) {
            parts.add(fn.toDecl());
            return this;
        }

        /**
         * Statements, in order — an {@link Action}, {@link Val}, {@link Stmt}
         * or {@link Func} each. The typed alternative to {@code unsafeRaw}.
         */
        public Script does(Object... statements) {
            for (Object s : statements) parts.add(toStatement(s));
            return this;
        }

        /**
         * Adds raw JavaScript code.
         * @deprecated Use {@link #unsafeRaw(String)} to make the escape explicit
         */
        @Deprecated
        public Script raw(String js) {
            parts.add(js);
            return this;
        }

        /**
         * Adds raw JavaScript code (unsafe - no validation).
         * Use this when the DSL doesn't support a specific construct.
         *
         * @param js the raw JavaScript code
         * @return this script for chaining
         */
        public Script unsafeRaw(String js) {
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


    // ==================== If/Elif/Else Builder ====================

    /**
     * Builder for if/elif/else chains.
     *
     * <p>Usage:</p>
     * <pre>
     * func("example")
     *     .if_(condition1).then(stmt1, stmt2)
     *     .elif(condition2).then(stmt3)
     *     .elif(condition3).then(stmt4)
     *     .else_(stmt5, stmt6)
     *     .end()
     * </pre>
     */
    public static class IfBuilder {
        private final Func parent;
        private final StringBuilder sb = new StringBuilder();
        private boolean needsThen = true;

        public IfBuilder(Func parent, Val condition) {
            this.parent = parent;
            sb.append("if(").append(condition.js()).append(")");
        }

        /** Statements to execute if condition is true */
        public IfBuilder then(Object... stmts) {
            if (!needsThen) throw new IllegalStateException("then() already called");
            sb.append("{");
            for (Object s : stmts) appendStmt(sb, s);
            sb.append("}");
            needsThen = false;
            return this;
        }

        /** Add an else-if branch */
        public IfBuilder elif(Val condition) {
            if (needsThen) throw new IllegalStateException("then() must be called before elif()");
            sb.append("else if(").append(condition.js()).append(")");
            needsThen = true;
            return this;
        }

        /** Add an else branch and finish the chain */
        public Func else_(Object... stmts) {
            if (needsThen) throw new IllegalStateException("then() must be called before else_()");
            sb.append("else{");
            for (Object s : stmts) appendStmt(sb, s);
            sb.append("}");
            parent.raw(sb.toString());
            return parent;
        }

        /** Finish the chain without an else branch */
        public Func end() {
            if (needsThen) throw new IllegalStateException("then() must be called before end()");
            parent.raw(sb.toString());
            return parent;
        }

        private void appendStmt(StringBuilder sb, Object s) {
            if (s instanceof Stmt st) sb.append(st.js()).append(";");
            else if (s instanceof Action a) sb.append(a.build()).append(";");
            else if (s instanceof Val val) sb.append(val.js()).append(";");
            else if (s instanceof String str) {
                sb.append(str);
                if (!str.endsWith(";") && !str.endsWith("}")) sb.append(";");
            }
        }
    }

    // ==================== For Loop Builder ====================

    /**
     * Builder for for loops.
     */
    public static class ForBuilder {
        private final Func parent;
        private final String header;
        private final List<String> bodyStmts = new ArrayList<>();

        // Standard for loop: for(let i=start; i<end; i++)
        public ForBuilder(Func parent, String varName, int start, Val endCondition) {
            this.parent = parent;
            this.header = "for(let " + varName + "=" + start + ";" + varName + "<" + endCondition.js() + ";" + varName + "++)";
        }

        // Custom for loop: for(init; condition; update)
        public ForBuilder(Func parent, String init, Val condition, String update) {
            this.parent = parent;
            this.header = "for(" + init + ";" + condition.js() + ";" + update + ")";
        }

        // For...of / For...in loop: for(const x of/in y)
        public ForBuilder(Func parent, String iteratorClause) {
            this.parent = parent;
            this.header = "for(" + iteratorClause + ")";
        }

        /** Adds statements to the loop body. */
        public ForBuilder body(Object... stmts) {
            for (Object s : stmts) {
                if (s instanceof Stmt st) bodyStmts.add(st.js());
                else if (s instanceof Action a) bodyStmts.add(a.build());
                else if (s instanceof Val val) bodyStmts.add(val.js());
                else if (s instanceof String str) bodyStmts.add(str);
            }
            return this;
        }

        /** Ends the for loop and returns to the parent function. */
        public Func endFor() {
            StringBuilder sb = new StringBuilder(header).append("{");
            for (String stmt : bodyStmts) {
                sb.append(stmt);
                if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
            }
            sb.append("}");
            parent.raw(sb.toString());
            return parent;
        }
    }

    // ==================== While Loop Builder ====================

    /**
     * Builder for while loops.
     */
    public static class WhileBuilder {
        private final Func parent;
        private final Val condition;
        private final List<String> bodyStmts = new ArrayList<>();

        public WhileBuilder(Func parent, Val condition) {
            this.parent = parent;
            this.condition = condition;
        }

        /** Adds statements to the loop body. */
        public WhileBuilder body(Object... stmts) {
            for (Object s : stmts) {
                if (s instanceof Stmt st) bodyStmts.add(st.js());
                else if (s instanceof Action a) bodyStmts.add(a.build());
                else if (s instanceof Val val) bodyStmts.add(val.js());
                else if (s instanceof String str) bodyStmts.add(str);
            }
            return this;
        }

        /** Ends the while loop and returns to the parent function. */
        public Func endWhile() {
            StringBuilder sb = new StringBuilder("while(").append(condition.js()).append("){");
            for (String stmt : bodyStmts) {
                sb.append(stmt);
                if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
            }
            sb.append("}");
            parent.raw(sb.toString());
            return parent;
        }
    }

    // ==================== Do-While Loop Builder ====================

    /**
     * Builder for do...while loops.
     */
    public static class DoWhileBuilder {
        private final Func parent;
        private final List<String> bodyStmts = new ArrayList<>();

        public DoWhileBuilder(Func parent) {
            this.parent = parent;
        }

        /** Adds statements to the loop body. */
        public DoWhileBuilder body(Object... stmts) {
            for (Object s : stmts) {
                if (s instanceof Stmt st) bodyStmts.add(st.js());
                else if (s instanceof Action a) bodyStmts.add(a.build());
                else if (s instanceof Val val) bodyStmts.add(val.js());
                else if (s instanceof String str) bodyStmts.add(str);
            }
            return this;
        }

        /** Ends the do...while loop with the given condition. */
        public Func while_(Val condition) {
            StringBuilder sb = new StringBuilder("do{");
            for (String stmt : bodyStmts) {
                sb.append(stmt);
                if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
            }
            sb.append("}while(").append(condition.js()).append(")");
            parent.raw(sb.toString());
            return parent;
        }
    }

    // ==================== Try/Catch Builder ====================

    /**
     * Builder for try/catch/finally blocks.
     */
    public static class TryBuilder {
        private final Func parent;
        private final List<String> tryStmts = new ArrayList<>();
        private String catchVar;
        private final List<String> catchStmts = new ArrayList<>();
        private final List<String> finallyStmts = new ArrayList<>();

        public TryBuilder(Func parent) {
            this.parent = parent;
        }

        /** Adds statements to the try block. */
        public TryBuilder body(Object... stmts) {
            for (Object s : stmts) {
                if (s instanceof Stmt st) tryStmts.add(st.js());
                else if (s instanceof Action a) tryStmts.add(a.build());
                else if (s instanceof Val val) tryStmts.add(val.js());
                else if (s instanceof String str) tryStmts.add(str);
            }
            return this;
        }

        /** Starts the catch block. */
        public CatchBuilder catch_(String varName) {
            this.catchVar = varName;
            return new CatchBuilder(this);
        }

        /**
         * Catch block with its body inline — closes the try/catch, so there is
         * no {@code endTry()} to remember.
         *
         * <pre>
         * func("save")
         *     .try_().body(call("risky"))
         *     .catch_("e", call("console.error", variable("e")))
         * </pre>
         */
        public Func catch_(String varName, Object... body) {
            this.catchVar = varName;
            for (Object s : body) {
                if (s instanceof Stmt st) catchStmts.add(st.js());
                else if (s instanceof Action a) catchStmts.add(a.build());
                else if (s instanceof Val val) catchStmts.add(val.js());
                else if (s instanceof String str) catchStmts.add(str);
            }
            return endTry();
        }

        /** Ends the try/catch block. */
        public Func endTry() {
            StringBuilder sb = new StringBuilder("try{");
            for (String stmt : tryStmts) {
                sb.append(stmt);
                if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
            }
            sb.append("}");

            if (catchVar != null) {
                sb.append("catch(").append(catchVar).append("){");
                for (String stmt : catchStmts) {
                    sb.append(stmt);
                    if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
                }
                sb.append("}");
            }

            if (!finallyStmts.isEmpty()) {
                sb.append("finally{");
                for (String stmt : finallyStmts) {
                    sb.append(stmt);
                    if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
                }
                sb.append("}");
            }

            parent.raw(sb.toString());
            return parent;
        }

        /** Builder for catch block content. */
        public class CatchBuilder {
            private final TryBuilder parent;

            CatchBuilder(TryBuilder parent) {
                this.parent = parent;
            }

            /** Adds statements to the catch block. */
            public CatchBuilder body(Object... stmts) {
                for (Object s : stmts) {
                    if (s instanceof Stmt st) parent.catchStmts.add(st.js());
                    else if (s instanceof Action a) parent.catchStmts.add(a.build());
                    else if (s instanceof Val val) parent.catchStmts.add(val.js());
                    else if (s instanceof String str) parent.catchStmts.add(str);
                }
                return this;
            }

            /** Adds a finally block. */
            public FinallyBuilder finally_() {
                return new FinallyBuilder(parent);
            }

            /** Ends the try/catch block. */
            public Func endTry() {
                return parent.endTry();
            }
        }

        /** Builder for finally block content. */
        public class FinallyBuilder {
            private final TryBuilder parent;

            FinallyBuilder(TryBuilder parent) {
                this.parent = parent;
            }

            /** Adds statements to the finally block. */
            public FinallyBuilder body(Object... stmts) {
                for (Object s : stmts) {
                    if (s instanceof Stmt st) parent.finallyStmts.add(st.js());
                    else if (s instanceof Action a) parent.finallyStmts.add(a.build());
                    else if (s instanceof Val val) parent.finallyStmts.add(val.js());
                    else if (s instanceof String str) parent.finallyStmts.add(str);
                }
                return this;
            }

            /** Ends the try/catch/finally block. */
            public Func endTry() {
                return parent.endTry();
            }
        }
    }

    // ==================== Switch Builder ====================

    /**
     * Builder for switch statements.
     */
    public static class SwitchBuilder {
        private final Func parent;
        private final Val value;
        private final List<String> cases = new ArrayList<>();

        public SwitchBuilder(Func parent, Val value) {
            this.parent = parent;
            this.value = value;
        }

        /** Adds a case clause. */
        public CaseBuilder case_(Object caseValue) {
            return new CaseBuilder(this, toJs(caseValue));
        }

        /**
         * Case clause with its statements inline. JavaScript cases fall through,
         * so pass {@code "break"} as the last statement when you want a break.
         *
         * <pre>
         * .switch_(variable("action"))
         *     .case_("add", call("add"), "break")
         *     .default_(call("noop"))
         * </pre>
         */
        public SwitchBuilder case_(Object caseValue, Object... stmts) {
            return new CaseBuilder(this, toJs(caseValue)).then(stmts);
        }

        /** Adds the default clause. */
        public CaseBuilder default_() {
            return new CaseBuilder(this, null);
        }

        /** Default clause with its statements inline — closes the switch. */
        public Func default_(Object... stmts) {
            new CaseBuilder(this, null).then(stmts);
            return endSwitch();
        }

        /** Ends the switch statement. */
        public Func endSwitch() {
            StringBuilder sb = new StringBuilder("switch(").append(value.js()).append("){");
            for (String c : cases) {
                sb.append(c);
            }
            sb.append("}");
            parent.raw(sb.toString());
            return parent;
        }

        /** Builder for case clause content. */
        public class CaseBuilder {
            private final SwitchBuilder parent;
            private final String caseValue; // null for default

            CaseBuilder(SwitchBuilder parent, String caseValue) {
                this.parent = parent;
                this.caseValue = caseValue;
            }

            /** Adds statements to this case. */
            public SwitchBuilder then(Object... stmts) {
                StringBuilder sb = new StringBuilder();
                if (caseValue != null) {
                    sb.append("case ").append(caseValue).append(":");
                } else {
                    sb.append("default:");
                }
                for (Object s : stmts) {
                    if (s instanceof Stmt st) sb.append(st.js()).append(";");
                    else if (s instanceof Action a) sb.append(a.build()).append(";");
                    else if (s instanceof Val val) sb.append(val.js()).append(";");
                    else if (s instanceof String str) {
                        sb.append(str);
                        if (!str.endsWith(";") && !str.endsWith("}")) sb.append(";");
                    }
                }
                parent.cases.add(sb.toString());
                return parent;
            }
        }
    }


    // ==================== Element ====================

    /**
     * Represents a DOM element with type-safe manipulation methods.
     *
     * <p>Example:</p>
     * <pre>
     * getElem("myDiv")
     *     .addClass("active")
     *     .removeClass("hidden")
     *     .setAttribute("data-id", "123")
     *     .setStyle("color", "red")
     * </pre>
     */
    public static class El extends Val {
        El(String code) { super(code); }

        // ==================== Content ====================

        /** Gets textContent: elem.textContent */
        public Val text() { return new Val(code + ".textContent"); }

        /** Sets textContent: elem.textContent = value */
        public El setText(String text) {
            return new El(code + ".textContent='" + esc(text) + "'");
        }

        /** Sets textContent from expression: elem.textContent = expr */
        public El setText(Val expr) {
            return new El(code + ".textContent=" + expr.js());
        }

        /** Gets value (for inputs): elem.value */
        public Val value() { return new Val(code + ".value"); }

        /** Sets value: elem.value = value */
        public El setValue(String value) {
            return new El(code + ".value='" + esc(value) + "'");
        }

        /** Sets value from expression: elem.value = expr */
        public El setValue(Val expr) {
            return new El(code + ".value=" + expr.js());
        }

        /** Gets innerHTML: elem.innerHTML */
        public Val html() { return new Val(code + ".innerHTML"); }

        /** Sets innerHTML: elem.innerHTML = html */
        public El setHtml(String html) {
            return new El(code + ".innerHTML='" + esc(html) + "'");
        }

        /** Sets innerHTML from expression: elem.innerHTML = expr */
        public El setHtml(Val expr) {
            return new El(code + ".innerHTML=" + expr.js());
        }

        // ==================== CSS Classes ====================

        /** Adds a CSS class: elem.classList.add('className') */
        public El addClass(String className) {
            return new El(code + ".classList.add('" + esc(className) + "')");
        }

        /** Removes a CSS class: elem.classList.remove('className') */
        public El removeClass(String className) {
            return new El(code + ".classList.remove('" + esc(className) + "')");
        }

        /** Toggles a CSS class: elem.classList.toggle('className') */
        public El toggleClass(String className) {
            return new El(code + ".classList.toggle('" + esc(className) + "')");
        }

        /** Toggles a CSS class based on condition: elem.classList.toggle('className', force) */
        public El toggleClass(String className, Val force) {
            return new El(code + ".classList.toggle('" + esc(className) + "'," + force.js() + ")");
        }

        /** Checks if element has class: elem.classList.contains('className') */
        public Val hasClass(String className) {
            return new Val(code + ".classList.contains('" + esc(className) + "')");
        }

        /** Replaces one class with another: elem.classList.replace('old', 'new') */
        public El replaceClass(String oldClass, String newClass) {
            return new El(code + ".classList.replace('" + esc(oldClass) + "','" + esc(newClass) + "')");
        }

        // ==================== Attributes ====================

        /** Gets an attribute: elem.getAttribute('name') */
        public Val getAttribute(String name) {
            return new Val(code + ".getAttribute('" + esc(name) + "')");
        }

        /** Sets an attribute: elem.setAttribute('name', 'value') */
        public El setAttribute(String name, String value) {
            return new El(code + ".setAttribute('" + esc(name) + "','" + esc(value) + "')");
        }

        /** Sets an attribute from expression: elem.setAttribute('name', expr) */
        public El setAttribute(String name, Val value) {
            return new El(code + ".setAttribute('" + esc(name) + "'," + value.js() + ")");
        }

        /** Removes an attribute: elem.removeAttribute('name') */
        public El removeAttribute(String name) {
            return new El(code + ".removeAttribute('" + esc(name) + "')");
        }

        /** Checks if element has attribute: elem.hasAttribute('name') */
        public Val hasAttribute(String name) {
            return new Val(code + ".hasAttribute('" + esc(name) + "')");
        }

        // ==================== Data Attributes ====================

        /** Gets a data attribute: elem.dataset.name */
        public Val getData(String name) {
            return new Val(code + ".dataset." + name);
        }

        /** Sets a data attribute: elem.dataset.name = value */
        public El setData(String name, String value) {
            return new El(code + ".dataset." + name + "='" + esc(value) + "'");
        }

        /** Sets a data attribute from expression: elem.dataset.name = expr */
        public El setData(String name, Val value) {
            return new El(code + ".dataset." + name + "=" + value.js());
        }

        // ==================== Inline Styles ====================

        /** Gets a style property: elem.style.property */
        public Val getStyle(String property) {
            return new Val(code + ".style." + property);
        }

        /** Sets a style property: elem.style.property = value */
        public El setStyle(String property, String value) {
            return new El(code + ".style." + property + "='" + esc(value) + "'");
        }

        /** Sets a style property from expression: elem.style.property = expr */
        public El setStyle(String property, Val value) {
            return new El(code + ".style." + property + "=" + value.js());
        }

        /** Gets computed style: getComputedStyle(elem).property */
        public Val getComputedStyle(String property) {
            return new Val("getComputedStyle(" + code + ")." + property);
        }

        // ==================== Visibility ====================

        /** Hides element: elem.style.display = 'none' */
        public El hide() {
            return new El(code + ".style.display='none'");
        }

        /** Shows element: elem.style.display = '' */
        public El show() {
            return new El(code + ".style.display=''");
        }

        /** Shows element with specific display: elem.style.display = display */
        public El show(String display) {
            return new El(code + ".style.display='" + esc(display) + "'");
        }

        /** Toggles visibility based on condition */
        public El visible(Val condition) {
            return new El(code + ".style.display=" + condition.js() + "?'':'none'");
        }

        // ==================== DOM Manipulation ====================

        /** Appends a child element: elem.appendChild(child) */
        public El appendChild(Val child) {
            return new El(code + ".appendChild(" + child.js() + ")");
        }

        /** Removes a child element: elem.removeChild(child) */
        public El removeChild(Val child) {
            return new El(code + ".removeChild(" + child.js() + ")");
        }

        /** Removes this element from DOM: elem.remove() */
        public El remove() {
            return new El(code + ".remove()");
        }

        /** Inserts before another element: elem.insertBefore(newNode, refNode) */
        public El insertBefore(Val newNode, Val refNode) {
            return new El(code + ".insertBefore(" + newNode.js() + "," + refNode.js() + ")");
        }

        /** Replaces a child element: elem.replaceChild(newChild, oldChild) */
        public El replaceChild(Val newChild, Val oldChild) {
            return new El(code + ".replaceChild(" + newChild.js() + "," + oldChild.js() + ")");
        }

        /** Clones element: elem.cloneNode(deep) */
        public Val cloneNode(boolean deep) {
            return new Val(code + ".cloneNode(" + deep + ")");
        }

        /** Gets parent element: elem.parentElement */
        public El parent() {
            return new El(code + ".parentElement");
        }

        /** Gets children: elem.children */
        public Val children() {
            return new Val(code + ".children");
        }

        /** Gets first child element: elem.firstElementChild */
        public El firstChild() {
            return new El(code + ".firstElementChild");
        }

        /** Gets last child element: elem.lastElementChild */
        public El lastChild() {
            return new El(code + ".lastElementChild");
        }

        /** Gets next sibling element: elem.nextElementSibling */
        public El nextSibling() {
            return new El(code + ".nextElementSibling");
        }

        /** Gets previous sibling element: elem.previousElementSibling */
        public El prevSibling() {
            return new El(code + ".previousElementSibling");
        }

        /** Finds descendant by selector: elem.querySelector(selector) */
        public El querySelector(String selector) {
            return new El(code + ".querySelector('" + esc(selector) + "')");
        }

        /** Finds all descendants by selector: elem.querySelectorAll(selector) */
        public Val querySelectorAll(String selector) {
            return new Val(code + ".querySelectorAll('" + esc(selector) + "')");
        }

        /** Finds closest ancestor matching selector: elem.closest(selector) */
        public El closest(String selector) {
            return new El(code + ".closest('" + esc(selector) + "')");
        }

        /** Checks if element matches selector: elem.matches(selector) */
        public Val matches(String selector) {
            return new Val(code + ".matches('" + esc(selector) + "')");
        }

        // ==================== Focus ====================

        /** Focuses element: elem.focus() */
        public El focus() {
            return new El(code + ".focus()");
        }

        /** Blurs element: elem.blur() */
        public El blur() {
            return new El(code + ".blur()");
        }

        // ==================== Scrolling ====================

        /** Scrolls element into view: elem.scrollIntoView() */
        public El scrollIntoView() {
            return new El(code + ".scrollIntoView()");
        }

        /** Scrolls element into view with options: elem.scrollIntoView({behavior: 'smooth'}) */
        public El scrollIntoView(String behavior) {
            return new El(code + ".scrollIntoView({behavior:'" + esc(behavior) + "'})");
        }

        // ==================== Events ====================

        /**
         * Adds an event listener whose handler is an expression — what
         * {@code debounce(ms, ...)} and {@code throttle(ms, ...)} return:
         *
         * <pre>
         * byId("search").addEventListener("input", debounce(300, callback("e").call("runSearch")))
         * </pre>
         */
        public El addEventListener(String type, Val handler) {
            return new El(code + ".addEventListener('" + esc(type) + "'," + handler.js() + ")");
        }

        /** Adds event listener: elem.addEventListener(type, handler) */
        public El addEventListener(String type, Func handler) {
            return new El(code + ".addEventListener('" + esc(type) + "'," + handler.toExpr() + ")");
        }

        /** Removes event listener: elem.removeEventListener(type, handler) */
        public El removeEventListener(String type, Val handler) {
            return new El(code + ".removeEventListener('" + esc(type) + "'," + handler.js() + ")");
        }

        /** Dispatches a custom event: elem.dispatchEvent(new Event(type)) */
        public El dispatchEvent(String type) {
            return new El(code + ".dispatchEvent(new Event('" + esc(type) + "'))");
        }

        /** Clicks element: elem.click() */
        public El click() {
            return new El(code + ".click()");
        }

        // ==================== Properties ====================

        /** Gets id: elem.id */
        public Val id() {
            return new Val(code + ".id");
        }

        /** Gets tagName: elem.tagName */
        public Val tagName() {
            return new Val(code + ".tagName");
        }

        /** Gets className: elem.className */
        public Val className() {
            return new Val(code + ".className");
        }

        /** Gets offsetWidth: elem.offsetWidth */
        public Val offsetWidth() {
            return new Val(code + ".offsetWidth");
        }

        /** Gets offsetHeight: elem.offsetHeight */
        public Val offsetHeight() {
            return new Val(code + ".offsetHeight");
        }

        /** Gets clientWidth: elem.clientWidth */
        public Val clientWidth() {
            return new Val(code + ".clientWidth");
        }

        /** Gets clientHeight: elem.clientHeight */
        public Val clientHeight() {
            return new Val(code + ".clientHeight");
        }

        /** Gets scrollTop: elem.scrollTop */
        public Val scrollTop() {
            return new Val(code + ".scrollTop");
        }

        /** Gets scrollLeft: elem.scrollLeft */
        public Val scrollLeft() {
            return new Val(code + ".scrollLeft");
        }

        /** Gets bounding client rect: elem.getBoundingClientRect() */
        public Val getBoundingClientRect() {
            return new Val(code + ".getBoundingClientRect()");
        }
    }

}
