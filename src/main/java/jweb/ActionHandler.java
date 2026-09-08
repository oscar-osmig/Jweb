package jweb;

/**
 * The handler of a typed route: the request plus the record its parameters
 * were bound to (see {@code app.action(path, Pref.class, (req, pref) -> ...)}).
 *
 * @param <T> the record type the parameters bind to
 */
@FunctionalInterface
public interface ActionHandler<T> {
    Object handle(Request request, T params);
}
