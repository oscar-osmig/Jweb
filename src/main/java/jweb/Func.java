package jweb;

import com.osmig.Jweb.framework.js.JS;
import com.osmig.Jweb.framework.js.JS.DoWhileBuilder;
import com.osmig.Jweb.framework.js.JS.ForBuilder;
import com.osmig.Jweb.framework.js.JS.IfBuilder;
import com.osmig.Jweb.framework.js.JS.SwitchBuilder;
import com.osmig.Jweb.framework.js.JS.TryBuilder;
import com.osmig.Jweb.framework.js.JS.WhileBuilder;
import jweb.js.Stmt;

import java.util.ArrayList;
import java.util.List;

import static com.osmig.Jweb.framework.js.JS.toJs;

/**
 * A JavaScript function under construction — {@code func("name", "params"...)}
 * starts one, the fluent statement methods fill its body, and
 * {@link #toDecl()} / {@link #toExpr()} emit it:
 *
 * <pre>{@code
 * import jweb.Func;
 * import static jweb.Js.*;
 *
 * Func check = func("check", "x")
 *     .if_(v("x").gt(10), call("big"))
 *     .else_(call("small"));
 * }</pre>
 *
 * <p>Was {@code JS.Func} before 3.0.</p>
 */
public class Func {
    private final String name;
    private final String[] params;
    private final List<String> body = new ArrayList<>();

    public Func(String name, String... params) {
        this.name = name;
        this.params = params;
    }

    public Func var_(String name, Object value) {
        body.add("var " + name + "=" + toJs(value));
        return this;
    }

    public Func let(String name, Object value) {
        body.add("let " + name + "=" + toJs(value));
        return this;
    }

    public Func set(String name, Object value) {
        body.add(name + "=" + toJs(value));
        return this;
    }

    public Func set(Val target, Object value) {
        body.add(target.js() + "=" + toJs(value));
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
        body.add(JS.call(fn, args).js());
        return this;
    }

    /**
     * Statements, in order — an {@link Action}, {@link Val}, {@link Stmt} or
     * nested {@link Func} each. This is what makes any action a statement
     * inside a callback, and the typed alternative to {@link #unsafeRaw}:
     *
     * <pre>{@code
     * callback("e", "t").does(
     *     preventDefault(),
     *     copyFrom("pre").trigger(v("t")).feedback("Copied!"))
     * }</pre>
     */
    public Func does(Object... statements) {
        for (Object s : statements) {
            String js = JS.toStatement(s);
            if (!js.isEmpty()) body.add(js);
        }
        return this;
    }

    public Func log(Object... args) {
        body.add(JS.call("console.log", args).js());
        return this;
    }

    /**
     * An if statement with its body inline; {@link #elif} and
     * {@link #else_} extend it, and nothing closes it:
     *
     * <pre>
     * .if_(v("x").gt(10), call("big"))
     * .elif(v("x").gt(5), call("mid"))
     * .else_(call("small"))
     * </pre>
     */
    public Func if_(Val condition, Object... thenStmts) {
        StringBuilder sb = new StringBuilder("if(").append(condition.js()).append("){");
        for (Object s : thenStmts) appendStmt(sb, s);
        sb.append("}");
        body.add(sb.toString());
        return this;
    }

    /** An {@code else if} branch on the preceding {@link #if_(Val, Object...)}. */
    public Func elif(Val condition, Object... stmts) {
        return branch("else if(" + condition.js() + ")", stmts);
    }

    /** The {@code else} branch on the preceding {@link #if_(Val, Object...)}. */
    public Func else_(Object... stmts) {
        return branch("else", stmts);
    }

    private Func branch(String head, Object... stmts) {
        if (body.isEmpty() || !body.get(body.size() - 1).startsWith("if(")) {
            throw new IllegalStateException(head + " must directly follow if_(condition, ...) or elif(...)");
        }
        StringBuilder sb = new StringBuilder(body.remove(body.size() - 1)).append(head).append("{");
        for (Object s : stmts) appendStmt(sb, s);
        sb.append("}");
        body.add(sb.toString());
        return this;
    }

    /** Start an if/elif/else chain: if_(condition).then(...).elif(cond2).then(...).else_(...).end() */
    public IfBuilder if_(Val condition) {
        return new IfBuilder(this, condition);
    }

    /** {@code return} — matches the {@code if_}/{@code while_} keyword convention. */
    public Func return_() {
        body.add("return");
        return this;
    }

    /** {@code return <value>} */
    public Func return_(Object value) {
        body.add("return " + toJs(value));
        return this;
    }

    /** @deprecated Use {@link #return_()}. */
    @Deprecated
    public Func ret() {
        return return_();
    }

    /** @deprecated Use {@link #return_(Object)}. */
    @Deprecated
    public Func ret(Object value) {
        return return_(value);
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
        return new ForBuilder(this, "const " + varName + " of " + iterable.js());
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
        return new ForBuilder(this, "const " + varName + " in " + object.js());
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

    // ==================== Loops (one-call form) ====================
    // Same loops as above, with the body passed inline — no matching
    // end-call to remember, and the nesting reads like the JS it emits.

    /**
     * {@code for (let i = 0; i < n; i++) { ... }} in one call.
     *
     * <pre>
     * func("render")
     *     .for_("i", 0, variable("items").length(),
     *         call("draw", variable("items").at(variable("i"))))
     * </pre>
     */
    public Func for_(String varName, int start, Val endCondition, Object... body) {
        return block("for(let " + varName + "=" + start + ";" + varName + "<"
                + endCondition.js() + ";" + varName + "++)", body);
    }

    /** {@code for (const x of iterable) { ... }} in one call. */
    public Func forOf(String varName, Val iterable, Object... body) {
        return block("for(const " + varName + " of " + iterable.js() + ")", body);
    }

    /** {@code for (const k in object) { ... }} in one call. */
    public Func forIn(String varName, Val object, Object... body) {
        return block("for(const " + varName + " in " + object.js() + ")", body);
    }

    /** {@code while (cond) { ... }} in one call. */
    public Func while_(Val condition, Object... body) {
        return block("while(" + condition.js() + ")", body);
    }

    /** {@code do { ... } while (cond)} in one call. */
    public Func doWhile(Val condition, Object... body) {
        StringBuilder sb = new StringBuilder("do{");
        for (Object s : body) appendStmt(sb, s);
        sb.append("}while(").append(condition.js()).append(")");
        this.body.add(sb.toString());
        return this;
    }

    /** Emits {@code <header>{ <body> }} and appends it to this function. */
    private Func block(String header, Object[] stmts) {
        StringBuilder sb = new StringBuilder(header).append("{");
        for (Object s : stmts) appendStmt(sb, s);
        sb.append("}");
        body.add(sb.toString());
        return this;
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
     *         .case_("add").then(call("add"), "break")
     *         .case_("remove").then(call("remove"), "break")
     *         .default_().then(call("noop"))
     *     .endSwitch()
     * </pre>
     *
     * @param value the value to switch on
     * @return a SwitchBuilder to continue building the switch
     */
    public SwitchBuilder switch_(Val value) {
        return new SwitchBuilder(this, value);
    }

    /** Renders this function as an expression: {@code function(a){...}} */
    public String toExpr() {
        StringBuilder sb = new StringBuilder("function(");
        sb.append(String.join(",", params)).append("){");
        for (String s : body) {
            sb.append(s);
            if (!s.endsWith("}") && !s.endsWith(";")) sb.append(";");
        }
        return sb.append("}").toString();
    }

    /** Renders this function as a declaration: {@code function name(a){...}} */
    public String toDecl() {
        StringBuilder sb = new StringBuilder("function ");
        if (name != null) sb.append(name);
        sb.append("(").append(String.join(",", params)).append("){");
        for (String s : body) {
            sb.append(s);
            if (!s.endsWith("}") && !s.endsWith(";")) sb.append(";");
        }
        return sb.append("}").toString();
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
