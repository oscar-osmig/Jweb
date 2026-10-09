package jweb;

import com.osmig.Jweb.framework.js.JS;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A handle to one element, for the JavaScript side.
 *
 * <p>Give an element the ref ({@code input(ref(EMAIL))}) and the ref's methods
 * are client-side {@link Action}s on it: {@code onClick(EMAIL.focus())}. The
 * ref also reads as an {@link Id} where a selector or an id attribute is
 * wanted ({@link #id()}).</p>
 *
 * <pre>{@code
 * Ref email = Ref.create();
 * input(ref(email), type("email"))
 * button(onClick(email.focus()), "Go to email")
 * }</pre>
 */
public class Ref {

    private static final AtomicInteger counter = new AtomicInteger(0);

    private final String id;

    protected Ref(String id) {
        this.id = id;
    }

    /** A ref with a generated id ({@code jweb-ref-N}). */
    public static Ref create() {
        return new Ref(nextId("jweb-ref"));
    }

    /** A ref with a generated id under {@code prefix} ({@code prefix-N}). */
    public static Ref create(String prefix) {
        return new Ref(nextId(prefix));
    }

    /** A ref over an existing element id. */
    public static Ref of(String id) {
        return new Ref(id);
    }

    /** A ref over an {@link Id} handle. */
    public static Ref of(Id id) {
        return new Ref(id.name());
    }

    /** The next generated id under {@code prefix}; shared with the legacy alias. */
    protected static String nextId(String prefix) {
        return prefix + "-" + counter.incrementAndGet();
    }

    /** The element id this ref targets. */
    public String id() {
        return id;
    }

    /** The same id as an {@link Id} handle: an element argument and a selector. */
    public Id asId() {
        return Id.of(id);
    }

    /** The JavaScript expression that resolves the element. */
    public String selector() {
        return "document.getElementById('" + id + "')";
    }

    /** The CSS selector for the element: {@code #id}. */
    public String cssSelector() {
        return "#" + id;
    }

    public Action focus() {
        return action(".focus()");
    }

    public Action blur() {
        return action(".blur()");
    }

    public Action scrollIntoView() {
        return action(".scrollIntoView({behavior:'smooth'})");
    }

    public Action scrollIntoView(String behavior, String block) {
        return action(".scrollIntoView({behavior:'" + behavior + "',block:'" + block + "'})");
    }

    public Action click() {
        return action(".click()");
    }

    /** The element's {@code property} as a value: {@code email.get("value")}. */
    public Val get(String property) {
        return JS.expr(selector() + "." + property);
    }

    public Action set(String property, String value) {
        return action("." + property + "='" + escapeJs(value) + "'");
    }

    public Action set(String property, Number value) {
        return action("." + property + "=" + value);
    }

    public Action addClass(String className) {
        return action(".classList.add('" + className + "')");
    }

    public Action addClass(Cls cls) {
        return addClass(cls.name());
    }

    public Action removeClass(String className) {
        return action(".classList.remove('" + className + "')");
    }

    public Action removeClass(Cls cls) {
        return removeClass(cls.name());
    }

    public Action toggleClass(String className) {
        return action(".classList.toggle('" + className + "')");
    }

    public Action toggleClass(Cls cls) {
        return toggleClass(cls.name());
    }

    public Action setStyle(String property, String value) {
        return action(".style." + property + "='" + escapeJs(value) + "'");
    }

    public Action setAttribute(String name, String value) {
        return action(".setAttribute('" + name + "','" + escapeJs(value) + "')");
    }

    public Action removeAttribute(String name) {
        return action(".removeAttribute('" + name + "')");
    }

    private Action action(String tail) {
        String js = selector() + tail;
        return () -> js;
    }

    private static String escapeJs(String value) {
        if (value == null) return "";
        return value
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Ref other && other.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
