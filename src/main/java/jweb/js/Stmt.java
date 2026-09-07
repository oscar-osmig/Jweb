package jweb.js;

/**
 * A single JavaScript statement (an assignment, a {@code return}, a bare
 * expression) as emitted by the JS DSL builders. Was {@code JS.Stmt} before 3.0.
 */
public class Stmt {
    private final String code;
    public Stmt(String code) { this.code = code; }

    /** The generated JavaScript — same accessor as {@link jweb.Val#js()}. */
    public String js() { return code; }

    @Override public String toString() { return code; }
}
