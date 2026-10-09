package jweb;

import jweb.css.Selector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class name as one handle for all three languages.
 *
 * <p>Declared once, it is an element argument, a selector and a JavaScript
 * target, so the name is typed once and a rename is a refactor:</p>
 *
 * <pre>{@code
 * static final Cls CARD = cls("card"), COPY = cls("copy");
 *
 * article(CARD, button(COPY, "Copy"))                      // class="card" / class="copy"
 * stylesheet().rule(CARD, style().padding(rem(1)))         // .card{padding:1rem}
 *             .rule(COPY.hover(), style().color(PRIMARY))  // .copy:hover{...}
 *             .rule(CARD.descendant(COPY), ...)            // .card .copy{...}
 * delegate(CARD, "click", COPY)                            // the JS DSL takes it too
 * }</pre>
 *
 * <p>A {@code Cls} is a {@link Selector}, so every combinator and pseudo-class
 * is available on it; selectors are immutable, so {@code CARD.hover()} leaves
 * {@code CARD} itself untouched. Several words in one handle
 * ({@code cls("btn primary")}) set both classes on the element and select
 * elements carrying both ({@code .btn.primary}).</p>
 *
 * <p>{@link #scoped()} mints a name from the declaring class
 * ({@code snippetcard-1}) for a component that does not care what its class is
 * called; declare it as a {@code static final} field so the name is stable.</p>
 */
public final class Cls extends Selector {

    private static final Map<String, AtomicInteger> SCOPED = new ConcurrentHashMap<>();

    private final String name;

    private Cls(String name) {
        super(compound(name));
        this.name = name;
    }

    /** The handle for {@code name} — one class, or several separated by spaces. */
    public static Cls of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("a class name must not be blank");
        }
        return new Cls(name.strip());
    }

    /**
     * A class scoped to the calling class: {@code snippetcard-1},
     * {@code snippetcard-2}, ... in declaration order. Meant for a
     * {@code static final} field; a handle minted inside {@code render()} would
     * get a new number on every render.
     */
    public static Cls scoped() {
        Class<?> owner = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames
                .map(StackWalker.StackFrame::getDeclaringClass)
                .filter(c -> !c.getName().startsWith("jweb.")
                    && !c.getName().startsWith("com.osmig.Jweb.framework."))
                .findFirst()
                .orElse(Cls.class));
        String base = owner.getSimpleName().toLowerCase();
        if (base.isEmpty()) base = "anon";
        int n = SCOPED.computeIfAbsent(owner.getName(), k -> new AtomicInteger()).incrementAndGet();
        return new Cls(base + "-" + n);
    }

    /** The class attribute's value: the name as declared. */
    public String name() {
        return name;
    }

    private static String compound(String name) {
        StringBuilder sb = new StringBuilder();
        for (String word : name.strip().split("\\s+")) {
            if (!word.isEmpty()) sb.append('.').append(word);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cls other && other.name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
