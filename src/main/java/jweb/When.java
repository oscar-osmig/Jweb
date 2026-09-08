package jweb;

import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.List;
import java.util.function.Supplier;

/**
 * The chained either/or form, started by {@code when(condition)}:
 *
 * <pre>{@code
 * when(active)
 *     .then(span(cls("chip chip-on"), name))
 *     .otherwise(a(href("/tag/" + name), cls("chip"), name))
 * }</pre>
 *
 * <p>Use it when a branch is long enough that {@code when(cond, a, b)} stops
 * reading as one expression. It is itself an {@link Element}, so a chain left
 * without {@code otherwise(...)} renders the matched branch — or nothing.</p>
 */
public final class When implements Element {

    private final boolean condition;
    private Element matched;

    /**
     * Internal — start with {@code when(condition)}.
     *
     * @param condition whether the {@code then} branch is taken
     */
    public When(boolean condition) {
        this.condition = condition;
    }

    /**
     * The branch for a true condition — an {@link Element}, a String (text) or
     * {@code null}.
     *
     * @param element what to render when the condition holds
     * @return this chain
     */
    public When then(Object element) {
        if (condition && matched == null) matched = branch(element);
        return this;
    }

    /**
     * The lazy branch for a true condition — built only when it is taken.
     *
     * @param element supplier for the true branch
     * @return this chain
     */
    public When then(Supplier<? extends Element> element) {
        if (condition && matched == null) matched = element.get();
        return this;
    }

    /**
     * The branch for a false condition; ends the chain.
     *
     * @param element what to render when the condition does not hold
     * @return the chosen branch
     */
    public Element otherwise(Object element) {
        return matched != null ? matched : branch(element);
    }

    /**
     * The lazy branch for a false condition; ends the chain.
     *
     * @param element supplier for the false branch
     * @return the chosen branch
     */
    public Element otherwise(Supplier<? extends Element> element) {
        return matched != null ? matched : element.get();
    }

    @Override
    public VNode toVNode() {
        return matched != null ? matched.toVNode() : new VFragment(List.of());
    }

    /**
     * One branch of a {@code when}: an Element, a String (text), or nothing.
     * Shared with the {@code when(cond, a, b)} statics.
     *
     * @param value the branch value
     * @return the branch as an element
     */
    public static Element branch(Object value) {
        return switch (value) {
            case null -> nothing();
            case Element element -> element;
            case String text -> com.osmig.Jweb.framework.elements.TextElement.of(text);
            case VNode node -> () -> node;
            default -> throw new IllegalArgumentException(
                "when(...) branches are Elements, Strings or null — not " + value.getClass().getName());
        };
    }

    /** An element that renders nothing. */
    static Element nothing() {
        return () -> new VFragment(List.of());
    }
}
