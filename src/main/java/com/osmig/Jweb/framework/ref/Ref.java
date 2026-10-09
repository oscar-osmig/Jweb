package com.osmig.Jweb.framework.ref;

/**
 * A reference to a DOM element for imperative, client-side operations.
 *
 * @deprecated The real class is {@link jweb.Ref}; this name stays so existing
 *             imports compile. Its factories return the {@code jweb.Ref}
 *             type — declare the variable as {@code jweb.Ref}.
 */
@Deprecated
public final class Ref extends jweb.Ref {

    private Ref(String id) {
        super(id);
    }

    public static jweb.Ref create() {
        return jweb.Ref.create();
    }

    public static jweb.Ref create(String prefix) {
        return jweb.Ref.create(prefix);
    }

    public static jweb.Ref of(String id) {
        return jweb.Ref.of(id);
    }
}
