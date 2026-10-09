package jweb;

import jweb.css.Selector;

/**
 * An element id as one handle for all three languages: an element argument
 * ({@code textarea(EDITOR)}), a selector ({@code rule(EDITOR.focus(), ...)})
 * and a JavaScript target ({@code byId(EDITOR)}, {@code query(EDITOR)}).
 *
 * <pre>{@code
 * static final Id EDITOR = id("editor");
 *
 * textarea(EDITOR, ...)                                   // id="editor"
 * stylesheet().rule(EDITOR.focus(), style().outline(none)) // #editor:focus{...}
 * byId(EDITOR).dot("value")                                // document.getElementById('editor')
 * }</pre>
 *
 * <p>An {@code Id} is an immutable {@link Selector}, so {@code EDITOR.focus()}
 * leaves {@code EDITOR} untouched.</p>
 */
public final class Id extends Selector {

    private final String name;

    private Id(String name) {
        super("#" + name);
        this.name = name;
    }

    /** The handle for the element id {@code name}. */
    public static Id of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("an id must not be blank");
        }
        return new Id(name.strip());
    }

    /** The id attribute's value. */
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Id other && other.name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
