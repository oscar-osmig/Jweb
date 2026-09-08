package jweb;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * The visitor's session — typed, null-safe access to the servlet session
 * behind a request, with one-shot flash messages.
 *
 * <pre>{@code
 * // A per-visitor object, created on first use (no-arg constructor)
 * Visit visit = Session.of(Visit.class, req);
 * visit.entered = true;
 *
 * // Keyed values
 * Session session = Session.of(req);
 * session.put("theme", "dark");
 * String theme = session.get("theme", String.class);      // null when absent
 * Optional<Cart> cart = session.find(Cart.class);
 *
 * // Flash: written now, read once on the next page
 * session.flash("notice", "Saved!");
 * String notice = session.flash("notice");                // then it is gone
 *
 * // Inside a page, without a Request in hand
 * public Element render() {
 *     Visit visit = session().of(Visit.class);
 *     ...
 * }
 * }</pre>
 *
 * <h2>Lifetime</h2>
 * <p>A session is the servlet container's: it is created on the first write
 * (reads never create one), identified by the {@code JSESSIONID} cookie,
 * expires after the configured idle timeout ({@code server.servlet.session.timeout},
 * 30 minutes by default), and ends on {@link #end()} — which {@code Auth.logout}
 * also calls. Values live in the server's memory: keep them small and
 * serializable, and never store secrets a page should not see. Typed values
 * are keyed by their class name, so a class that changes shape across a
 * deploy simply starts fresh.</p>
 */
public final class Session {

    private static final String TYPED_PREFIX = "jweb.session.";
    private static final String FLASH_PREFIX = "jweb.flash.";

    private final Request request;

    private Session(Request request) {
        this.request = request;
    }

    // ==================== Obtaining one ====================

    /** The session behind a request (created lazily, on the first write). */
    public static Session of(Request request) {
        if (request == null) throw new IllegalArgumentException("request is null");
        return new Session(request);
    }

    /**
     * The per-visitor value of {@code type}, created through its no-arg
     * constructor and stored on first use.
     */
    public static <T> T of(Class<T> type, Request request) {
        return of(request).of(type);
    }

    /** {@link #of(Class, Request)} with an explicit factory for the first use. */
    public static <T> T of(Class<T> type, Request request, Supplier<? extends T> factory) {
        return of(request).of(type, factory);
    }

    /**
     * The session of the request in flight — for code that renders inside a
     * page route or router handler but has no {@code Request} parameter.
     *
     * @throws IllegalStateException outside a dispatch
     */
    public static Session current() {
        return of(com.osmig.Jweb.framework.server.CurrentRequest.require("Session.current()"));
    }

    // ==================== Typed values ====================

    /** The value of {@code type} in this session, created on first use. */
    public <T> T of(Class<T> type) {
        return of(type, () -> instantiate(type));
    }

    /** The value of {@code type} in this session, created by {@code factory} on first use. */
    public <T> T of(Class<T> type, Supplier<? extends T> factory) {
        T existing = get(type);
        if (existing != null) return existing;
        T created = factory.get();
        if (created == null) throw new IllegalStateException("Session factory for " + type.getName() + " returned null");
        write(TYPED_PREFIX + type.getName(), created);
        return created;
    }

    /** The value of {@code type}, or null when absent (or stored as another type). */
    public <T> T get(Class<T> type) {
        return get(TYPED_PREFIX + type.getName(), type);
    }

    /** The value of {@code type}, if present. */
    public <T> Optional<T> find(Class<T> type) {
        return Optional.ofNullable(get(type));
    }

    /** Stores {@code value} under its class. */
    public Session put(Object value) {
        if (value == null) throw new IllegalArgumentException("value is null — use remove(Class)");
        write(TYPED_PREFIX + value.getClass().getName(), value);
        return this;
    }

    /** Removes the value stored under {@code type}. */
    public Session remove(Class<?> type) {
        return remove(TYPED_PREFIX + type.getName());
    }

    /** Whether a value of {@code type} is stored. */
    public boolean has(Class<?> type) {
        return get(type) != null;
    }

    // ==================== Keyed values ====================

    /** The value under {@code key} as {@code type}, or null when absent or of another type. */
    public <T> T get(String key, Class<T> type) {
        Object value = read(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    /** The value under {@code key}, or null. */
    public Object get(String key) {
        return read(key);
    }

    /** The value under {@code key} as {@code type}, or {@code fallback} when absent. */
    public <T> T get(String key, Class<T> type, T fallback) {
        T value = get(key, type);
        return value != null ? value : fallback;
    }

    /** Stores {@code value} under {@code key}; null removes it. */
    public Session put(String key, Object value) {
        if (value == null) return remove(key);
        write(key, value);
        return this;
    }

    /** Removes {@code key}. */
    public Session remove(String key) {
        HttpSession session = request.session(false);
        if (session != null) session.removeAttribute(key);
        return this;
    }

    /** Whether {@code key} is present. */
    public boolean has(String key) {
        return read(key) != null;
    }

    // ==================== Flash ====================

    /** Stores a message that the next {@link #flash(String)} read removes. */
    public Session flash(String key, Object value) {
        return put(FLASH_PREFIX + key, value);
    }

    /**
     * Reads and clears the flash message under {@code key} — null when there
     * is none. Typed by inference: {@code String notice = session.flash("notice")}.
     */
    @SuppressWarnings("unchecked")
    public <T> T flash(String key) {
        Object value = read(FLASH_PREFIX + key);
        if (value != null) remove(FLASH_PREFIX + key);
        return (T) value;
    }

    /** Whether a flash message waits under {@code key} (does not consume it). */
    public boolean hasFlash(String key) {
        return read(FLASH_PREFIX + key) != null;
    }

    // ==================== Lifetime ====================

    /** Whether the visitor has a session at all (nothing was ever written). */
    public boolean exists() {
        return request.session(false) != null;
    }

    /** The container's session id, or null when there is no session yet. */
    public String id() {
        HttpSession session = request.session(false);
        return session == null ? null : session.getId();
    }

    /** Ends the session: every value is forgotten and the cookie invalidated. */
    public void end() {
        HttpSession session = request.session(false);
        if (session != null) session.invalidate();
    }

    /** The request this session belongs to. */
    public Request request() {
        return request;
    }

    /** The servlet session, created if absent — the escape hatch. */
    public HttpSession raw() {
        return request.session(true);
    }

    // ==================== Internals ====================

    private Object read(String key) {
        HttpSession session = request.session(false);
        if (session == null) return null;
        try {
            return session.getAttribute(key);
        } catch (IllegalStateException invalidated) {
            return null;
        }
    }

    private void write(String key, Object value) {
        request.session(true).setAttribute(key, value);
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type.getName()
                + " has no no-arg constructor — pass a factory: Session.of(" + type.getSimpleName()
                + ".class, req, () -> new " + type.getSimpleName() + "(...))", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create " + type.getName(), e);
        }
    }
}
