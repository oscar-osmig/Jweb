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
            props.add("[" + keyExpr.code + "]:" + toJs(value));
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
            props.add("..." + obj.code);
            return this;
        }

        /** Builds the object literal. */
        public Val build() {
            return new Val("{" + String.join(",", props) + "}");
        }
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

        public Script add(Async.AsyncFunc fn) {
            parts.add(fn.toDecl());
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

        /**
         * Adds raw JavaScript code.
         * @deprecated Use {@link #unsafeRaw(String)} to make the escape explicit
         */
        @Deprecated
        public Func raw(String js) {
            body.add(js);
            return this;
        }

        /**
         * Adds raw JavaScript code (unsafe - no validation).
         * Use this when the DSL doesn't support a specific construct.
         *
         * @param js the raw JavaScript code
         * @return this function for chaining
         */
        public Func unsafeRaw(String js) {
            body.add(js);
            return this;
        }

        // ==================== Loops ====================

        /**
         * Creates a for loop.
         *
         * <p>Example:</p>
         * <pre>
         * func("example")
         *     .for_("i", 0, variable("items").dot("length"))
         *         .body(
         *             call("console.log", variable("items").at(variable("i")))
         *         )
         *     .endFor()
         * </pre>
         *
         * @param varName the loop variable name
         * @param start the starting value
         * @param endCondition the end condition (loop while varName < endCondition)
         * @return a ForBuilder to continue building the loop
         */
        public ForBuilder for_(String varName, int start, Val endCondition) {
            return new ForBuilder(this, varName, start, endCondition);
        }

        /**
         * Creates a for loop with custom condition and increment.
         *
         * <p>Example:</p>
         * <pre>
         * func("example")
         *     .forLoop("let i=0", variable("i").lt(10), "i++")
         *         .body(call("process", variable("i")))
         *     .endFor()
         * </pre>
         *
         * @param init the initialization statement
         * @param condition the loop condition
         * @param update the update statement
         * @return a ForBuilder to continue building the loop
         */
        public ForBuilder forLoop(String init, Val condition, String update) {
            return new ForBuilder(this, init, condition, update);
        }

        /**
         * Creates a for...of loop (iterates over iterable values).
         *
         * <p>Example:</p>
         * <pre>
         * func("example")
         *     .forOf("item", variable("items"))
         *         .body(call("console.log", variable("item")))
         *     .endFor()
         * </pre>
         *
         * @param varName the variable name for each item
         * @param iterable the iterable to loop over
         * @return a ForBuilder to continue building the loop
         */
        public ForBuilder forOf(String varName, Val iterable) {
            return new ForBuilder(this, "const " + varName + " of " + iterable.code);
        }

        /**
         * Creates a for...in loop (iterates over object keys).
         *
         * <p>Example:</p>
         * <pre>
         * func("example")
         *     .forIn("key", variable("obj"))
         *         .body(call("console.log", variable("key")))
         *     .endFor()
         * </pre>
         *
         * @param varName the variable name for each key
         * @param object the object to iterate over
         * @return a ForBuilder to continue building the loop
         */
        public ForBuilder forIn(String varName, Val object) {
            return new ForBuilder(this, "const " + varName + " in " + object.code);
        }

        /**
         * Creates a while loop.
         *
         * <p>Example:</p>
         * <pre>
         * func("example")
         *     .while_(variable("count").lt(10))
         *         .body(
         *             "count++"
         *         )
         *     .endWhile()
         * </pre>
         *
         * @param condition the loop condition
         * @return a WhileBuilder to continue building the loop
         */
        public WhileBuilder while_(Val condition) {
            return new WhileBuilder(this, condition);
        }

        /**
         * Creates a do...while loop.
         *
         * <p>Example:</p>
         * <pre>
         * func("example")
         *     .doWhile()
         *         .body("count++")
         *     .while_(variable("count").lt(10))
         * </pre>
         *
         * @return a DoWhileBuilder to continue building the loop
         */
        public DoWhileBuilder doWhile() {
            return new DoWhileBuilder(this);
        }

        // ==================== Try/Catch ====================

        /**
         * Creates a try/catch block.
         *
         * <p>Example:</p>
         * <pre>
         * func("example")
         *     .try_()
         *         .body(call("riskyOperation"))
         *     .catch_("e")
         *         .body(call("console.error", variable("e")))
         *     .endTry()
         * </pre>
         *
         * @return a TryBuilder to continue building the try/catch
         */
        public TryBuilder try_() {
            return new TryBuilder(this);
        }

        // ==================== Switch ====================

        /**
         * Creates a switch statement.
         *
         * <p>Example:</p>
         * <pre>
         * func("example")
         *     .switch_(variable("action"))
         *         .case_("add").then_(call("add"), "break")
         *         .case_("remove").then_(call("remove"), "break")
         *         .default_().then_(call("noop"))
         *     .endSwitch()
         * </pre>
         *
         * @param value the value to switch on
         * @return a SwitchBuilder to continue building the switch
         */
        public SwitchBuilder switch_(Val value) {
            return new SwitchBuilder(this, value);
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

    // ==================== For Loop Builder ====================

    /**
     * Builder for for loops.
     */
    public static class ForBuilder {
        private final Func parent;
        private final String header;
        private final List<String> bodyStmts = new ArrayList<>();

        // Standard for loop: for(let i=start; i<end; i++)
        ForBuilder(Func parent, String varName, int start, Val endCondition) {
            this.parent = parent;
            this.header = "for(let " + varName + "=" + start + ";" + varName + "<" + endCondition.code + ";" + varName + "++)";
        }

        // Custom for loop: for(init; condition; update)
        ForBuilder(Func parent, String init, Val condition, String update) {
            this.parent = parent;
            this.header = "for(" + init + ";" + condition.code + ";" + update + ")";
        }

        // For...of / For...in loop: for(const x of/in y)
        ForBuilder(Func parent, String iteratorClause) {
            this.parent = parent;
            this.header = "for(" + iteratorClause + ")";
        }

        /** Adds statements to the loop body. */
        public ForBuilder body(Object... stmts) {
            for (Object s : stmts) {
                if (s instanceof Stmt st) bodyStmts.add(st.code);
                else if (s instanceof Val val) bodyStmts.add(val.code);
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
            parent.body.add(sb.toString());
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

        WhileBuilder(Func parent, Val condition) {
            this.parent = parent;
            this.condition = condition;
        }

        /** Adds statements to the loop body. */
        public WhileBuilder body(Object... stmts) {
            for (Object s : stmts) {
                if (s instanceof Stmt st) bodyStmts.add(st.code);
                else if (s instanceof Val val) bodyStmts.add(val.code);
                else if (s instanceof String str) bodyStmts.add(str);
            }
            return this;
        }

        /** Ends the while loop and returns to the parent function. */
        public Func endWhile() {
            StringBuilder sb = new StringBuilder("while(").append(condition.code).append("){");
            for (String stmt : bodyStmts) {
                sb.append(stmt);
                if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
            }
            sb.append("}");
            parent.body.add(sb.toString());
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

        DoWhileBuilder(Func parent) {
            this.parent = parent;
        }

        /** Adds statements to the loop body. */
        public DoWhileBuilder body(Object... stmts) {
            for (Object s : stmts) {
                if (s instanceof Stmt st) bodyStmts.add(st.code);
                else if (s instanceof Val val) bodyStmts.add(val.code);
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
            sb.append("}while(").append(condition.code).append(")");
            parent.body.add(sb.toString());
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

        TryBuilder(Func parent) {
            this.parent = parent;
        }

        /** Adds statements to the try block. */
        public TryBuilder body(Object... stmts) {
            for (Object s : stmts) {
                if (s instanceof Stmt st) tryStmts.add(st.code);
                else if (s instanceof Val val) tryStmts.add(val.code);
                else if (s instanceof String str) tryStmts.add(str);
            }
            return this;
        }

        /** Starts the catch block. */
        public CatchBuilder catch_(String varName) {
            this.catchVar = varName;
            return new CatchBuilder(this);
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

            parent.body.add(sb.toString());
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
                    if (s instanceof Stmt st) parent.catchStmts.add(st.code);
                    else if (s instanceof Val val) parent.catchStmts.add(val.code);
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
                    if (s instanceof Stmt st) parent.finallyStmts.add(st.code);
                    else if (s instanceof Val val) parent.finallyStmts.add(val.code);
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

        SwitchBuilder(Func parent, Val value) {
            this.parent = parent;
            this.value = value;
        }

        /** Adds a case clause. */
        public CaseBuilder case_(Object caseValue) {
            return new CaseBuilder(this, toJs(caseValue));
        }

        /** Adds the default clause. */
        public CaseBuilder default_() {
            return new CaseBuilder(this, null);
        }

        /** Ends the switch statement. */
        public Func endSwitch() {
            StringBuilder sb = new StringBuilder("switch(").append(value.code).append("){");
            for (String c : cases) {
                sb.append(c);
            }
            sb.append("}");
            parent.body.add(sb.toString());
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
            public SwitchBuilder then_(Object... stmts) {
                StringBuilder sb = new StringBuilder();
                if (caseValue != null) {
                    sb.append("case ").append(caseValue).append(":");
                } else {
                    sb.append("default:");
                }
                for (Object s : stmts) {
                    if (s instanceof Stmt st) sb.append(st.code).append(";");
                    else if (s instanceof Val val) sb.append(val.code).append(";");
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

        // ==================== String Methods ====================

        public Val padStart(int len, String pad) {
            return new Val("String(" + code + ").padStart(" + len + ",'" + esc(pad) + "')");
        }
        public Val padEnd(int len, String pad) {
            return new Val("String(" + code + ").padEnd(" + len + ",'" + esc(pad) + "')");
        }
        public Val length() { return new Val(code + ".length"); }
        public Val trim() { return new Val(code + ".trim()"); }
        public Val toLowerCase() { return new Val(code + ".toLowerCase()"); }
        public Val toUpperCase() { return new Val(code + ".toUpperCase()"); }

        /** Gets substring: str.substring(start, end) */
        public Val substring(int start, int end) {
            return new Val(code + ".substring(" + start + "," + end + ")");
        }

        /** Gets substring from start: str.substring(start) */
        public Val substring(int start) {
            return new Val(code + ".substring(" + start + ")");
        }

        /** Gets character at index: str.charAt(index) */
        public Val charAt(int index) {
            return new Val(code + ".charAt(" + index + ")");
        }

        /** Finds index of substring: str.indexOf(search) */
        public Val indexOf(String search) {
            return new Val(code + ".indexOf('" + esc(search) + "')");
        }

        /** Finds last index of substring: str.lastIndexOf(search) */
        public Val lastIndexOf(String search) {
            return new Val(code + ".lastIndexOf('" + esc(search) + "')");
        }

        /** Splits string: str.split(separator) */
        public Val split(String separator) {
            return new Val(code + ".split('" + esc(separator) + "')");
        }

        /** Replaces first occurrence: str.replace(search, replacement) */
        public Val replace(String search, String replacement) {
            return new Val(code + ".replace('" + esc(search) + "','" + esc(replacement) + "')");
        }

        /** Replaces all occurrences: str.replaceAll(search, replacement) */
        public Val replaceAll(String search, String replacement) {
            return new Val(code + ".replaceAll('" + esc(search) + "','" + esc(replacement) + "')");
        }

        /** Checks if string starts with prefix: str.startsWith(prefix) */
        public Val startsWith(String prefix) {
            return new Val(code + ".startsWith('" + esc(prefix) + "')");
        }

        /** Checks if string ends with suffix: str.endsWith(suffix) */
        public Val endsWith(String suffix) {
            return new Val(code + ".endsWith('" + esc(suffix) + "')");
        }

        /** Checks if string includes substring: str.includes(search) */
        public Val includes(String search) {
            return new Val(code + ".includes('" + esc(search) + "')");
        }

        // ==================== Array Methods ====================

        /**
         * Filters array elements: arr.filter(callback)
         *
         * <p>Example:</p>
         * <pre>
         * variable("items").filter(callback("x").ret(variable("x").gt(5)))
         * // Output: items.filter(function(x){return (x>5);})
         * </pre>
         */
        public Val filter(Func predicate) {
            return new Val(code + ".filter(" + predicate.toExpr() + ")");
        }

        /**
         * Maps array elements: arr.map(callback)
         *
         * <p>Example:</p>
         * <pre>
         * variable("items").map(callback("x").ret(variable("x").times(2)))
         * // Output: items.map(function(x){return (x*2);})
         * </pre>
         */
        public Val map(Func transformer) {
            return new Val(code + ".map(" + transformer.toExpr() + ")");
        }

        /**
         * Iterates over array: arr.forEach(callback)
         *
         * <p>Example:</p>
         * <pre>
         * variable("items").forEach(callback("item").call("process", variable("item")))
         * </pre>
         */
        public Val forEach(Func action) {
            return new Val(code + ".forEach(" + action.toExpr() + ")");
        }

        /**
         * Finds first matching element: arr.find(callback)
         */
        public Val find(Func predicate) {
            return new Val(code + ".find(" + predicate.toExpr() + ")");
        }

        /**
         * Finds index of first matching element: arr.findIndex(callback)
         */
        public Val findIndex(Func predicate) {
            return new Val(code + ".findIndex(" + predicate.toExpr() + ")");
        }

        /**
         * Checks if any element matches: arr.some(callback)
         */
        public Val some(Func predicate) {
            return new Val(code + ".some(" + predicate.toExpr() + ")");
        }

        /**
         * Checks if all elements match: arr.every(callback)
         */
        public Val every(Func predicate) {
            return new Val(code + ".every(" + predicate.toExpr() + ")");
        }

        /**
         * Reduces array to single value: arr.reduce(callback, initialValue)
         */
        public Val reduce(Func reducer, Object initialValue) {
            return new Val(code + ".reduce(" + reducer.toExpr() + "," + toJs(initialValue) + ")");
        }

        /**
         * Reduces array to single value: arr.reduce(callback)
         */
        public Val reduce(Func reducer) {
            return new Val(code + ".reduce(" + reducer.toExpr() + ")");
        }

        /**
         * Gets slice of array: arr.slice(start, end)
         */
        public Val slice(int start, int end) {
            return new Val(code + ".slice(" + start + "," + end + ")");
        }

        /**
         * Gets slice of array from start: arr.slice(start)
         */
        public Val slice(int start) {
            return new Val(code + ".slice(" + start + ")");
        }

        /**
         * Concatenates arrays: arr.concat(other)
         */
        public Val concat(Val other) {
            return new Val(code + ".concat(" + other.code + ")");
        }

        /**
         * Joins array to string: arr.join(separator)
         */
        public Val join(String separator) {
            return new Val(code + ".join('" + esc(separator) + "')");
        }

        /**
         * Reverses array: arr.reverse()
         */
        public Val reverse() {
            return new Val(code + ".reverse()");
        }

        /**
         * Sorts array: arr.sort()
         */
        public Val sort() {
            return new Val(code + ".sort()");
        }

        /**
         * Sorts array with comparator: arr.sort(comparator)
         */
        public Val sort(Func comparator) {
            return new Val(code + ".sort(" + comparator.toExpr() + ")");
        }

        /**
         * Checks if array includes value: arr.includes(value)
         */
        public Val includes(Val value) {
            return new Val(code + ".includes(" + value.code + ")");
        }

        /**
         * Gets first element: arr[0]
         */
        public Val first() {
            return new Val(code + "[0]");
        }

        /**
         * Gets last element: arr[arr.length-1]
         */
        public Val last() {
            return new Val(code + "[" + code + ".length-1]");
        }

        /**
         * Pushes element to array: arr.push(value)
         */
        public Val push(Object value) {
            return new Val(code + ".push(" + toJs(value) + ")");
        }

        /**
         * Pops element from array: arr.pop()
         */
        public Val pop() {
            return new Val(code + ".pop()");
        }

        /**
         * Shifts element from array: arr.shift()
         */
        public Val shift() {
            return new Val(code + ".shift()");
        }

        /**
         * Unshifts element to array: arr.unshift(value)
         */
        public Val unshift(Object value) {
            return new Val(code + ".unshift(" + toJs(value) + ")");
        }

        // ==================== Object Methods ====================

        /**
         * Gets object keys: Object.keys(obj)
         */
        public Val keys() {
            return new Val("Object.keys(" + code + ")");
        }

        /**
         * Gets object values: Object.values(obj)
         */
        public Val values() {
            return new Val("Object.values(" + code + ")");
        }

        /**
         * Gets object entries: Object.entries(obj)
         */
        public Val entries() {
            return new Val("Object.entries(" + code + ")");
        }

        /**
         * Checks if object has own property: obj.hasOwnProperty(key)
         */
        public Val hasOwnProperty(String key) {
            return new Val(code + ".hasOwnProperty('" + esc(key) + "')");
        }

        // ==================== Type Checking ====================

        /**
         * Gets typeof: typeof value
         */
        public Val typeof() {
            return new Val("typeof " + code);
        }

        /**
         * Checks instanceof: value instanceof Type
         */
        public Val instanceof_(String type) {
            return new Val("(" + code + " instanceof " + type + ")");
        }

        // ==================== Assignment ====================

        /** Assignment statement: variable("x").assign(5) -> x=5 */
        public Stmt assign(Object value) { return new Stmt(code + "=" + toJs(value)); }

        /** Compound assignment: variable("x").addAssign(5) -> x+=5 */
        public Stmt addAssign(Object value) { return new Stmt(code + "+=" + toJs(value)); }

        /** Compound assignment: variable("x").subAssign(5) -> x-=5 */
        public Stmt subAssign(Object value) { return new Stmt(code + "-=" + toJs(value)); }

        /** Compound assignment: variable("x").mulAssign(5) -> x*=5 */
        public Stmt mulAssign(Object value) { return new Stmt(code + "*=" + toJs(value)); }

        /** Compound assignment: variable("x").divAssign(5) -> x/=5 */
        public Stmt divAssign(Object value) { return new Stmt(code + "/=" + toJs(value)); }

        @Override
        public String toString() { return code; }
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
            return new El(code + ".textContent=" + expr.code);
        }

        /** Gets value (for inputs): elem.value */
        public Val value() { return new Val(code + ".value"); }

        /** Sets value: elem.value = value */
        public El setValue(String value) {
            return new El(code + ".value='" + esc(value) + "'");
        }

        /** Sets value from expression: elem.value = expr */
        public El setValue(Val expr) {
            return new El(code + ".value=" + expr.code);
        }

        /** Gets innerHTML: elem.innerHTML */
        public Val html() { return new Val(code + ".innerHTML"); }

        /** Sets innerHTML: elem.innerHTML = html */
        public El setHtml(String html) {
            return new El(code + ".innerHTML='" + esc(html) + "'");
        }

        /** Sets innerHTML from expression: elem.innerHTML = expr */
        public El setHtml(Val expr) {
            return new El(code + ".innerHTML=" + expr.code);
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
            return new El(code + ".classList.toggle('" + esc(className) + "'," + force.code + ")");
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
            return new El(code + ".setAttribute('" + esc(name) + "'," + value.code + ")");
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
            return new El(code + ".dataset." + name + "=" + value.code);
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
            return new El(code + ".style." + property + "=" + value.code);
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
            return new El(code + ".style.display=" + condition.code + "?'':'none'");
        }

        // ==================== DOM Manipulation ====================

        /** Appends a child element: elem.appendChild(child) */
        public El appendChild(Val child) {
            return new El(code + ".appendChild(" + child.code + ")");
        }

        /** Removes a child element: elem.removeChild(child) */
        public El removeChild(Val child) {
            return new El(code + ".removeChild(" + child.code + ")");
        }

        /** Removes this element from DOM: elem.remove() */
        public El remove() {
            return new El(code + ".remove()");
        }

        /** Inserts before another element: elem.insertBefore(newNode, refNode) */
        public El insertBefore(Val newNode, Val refNode) {
            return new El(code + ".insertBefore(" + newNode.code + "," + refNode.code + ")");
        }

        /** Replaces a child element: elem.replaceChild(newChild, oldChild) */
        public El replaceChild(Val newChild, Val oldChild) {
            return new El(code + ".replaceChild(" + newChild.code + "," + oldChild.code + ")");
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

        /** Adds event listener: elem.addEventListener(type, handler) */
        public El addEventListener(String type, Func handler) {
            return new El(code + ".addEventListener('" + esc(type) + "'," + handler.toExpr() + ")");
        }

        /** Removes event listener: elem.removeEventListener(type, handler) */
        public El removeEventListener(String type, Val handler) {
            return new El(code + ".removeEventListener('" + esc(type) + "'," + handler.code + ")");
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

    // ==================== Statement ====================

    public static class Stmt {
        final String code;
        Stmt(String code) { this.code = code; }
    }
}
