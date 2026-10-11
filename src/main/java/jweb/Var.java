package jweb;

import jweb.js.Stmt;

import static com.osmig.Jweb.framework.js.JS.toJs;

/**
 * A JavaScript local, declared once as a handle and used by name nowhere.
 *
 * <p>Like {@link Cls} and {@link Id} for the other two languages: the name is
 * typed in one place, and every reference, declaration and assignment goes
 * through the handle, so a misspelling is a compile error and a rename is one
 * edit. A {@code Var} is a {@link Val}, so everything an expression can do —
 * {@code .plus(1)}, {@code .length()}, {@code .eq("x")}, {@code .trim()} — it
 * can do.</p>
 *
 * <pre>
 * static final Var Q = local("q"), HTML = local("html");
 *
 * callback()
 *     .let(Q, byId(INPUT).value().trim())            // let q = ...
 *     .if_(Q.length().eq(0), results.setHtml(""))     // q.length === 0
 *     .else_(fetch(str(url).plus(Q)).text()
 *         .then(callback(HTML).does(results.setHtml(HTML))))   // (html) => ...
 * </pre>
 *
 * <p>{@code Js.local(name)} is the factory. The name is emitted verbatim, so it
 * must be a valid JavaScript identifier.</p>
 */
public final class Var extends Val {

    private final String name;

    private Var(String name) {
        super(name);
        this.name = name;
    }

    /** The handle for a local named {@code name}; {@code Js.local(name)} is the usual spelling. */
    public static Var of(String name) {
        if (name == null || !name.matches("[A-Za-z_$][A-Za-z0-9_$]*")) {
            throw new IllegalArgumentException("not a JavaScript identifier: " + name);
        }
        return new Var(name);
    }

    /** The identifier as it appears in the script. */
    public String name() {
        return name;
    }

    /** {@code let name = value;} */
    public Stmt let(Object value) {
        return new Stmt("let " + name + "=" + toJs(value));
    }

    /** {@code const name = value;} ({@code const} is a Java keyword, hence the underscore). */
    public Stmt const_(Object value) {
        return new Stmt("const " + name + "=" + toJs(value));
    }

    /** {@code name = value;} — the same as {@link #assign(Object)}, read as a verb. */
    public Stmt set(Object value) {
        return assign(value);
    }

    /** {@code name++;} */
    public Stmt increment() {
        return new Stmt(name + "++");
    }

    /** {@code name--;} */
    public Stmt decrement() {
        return new Stmt(name + "--");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Var other && other.name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
