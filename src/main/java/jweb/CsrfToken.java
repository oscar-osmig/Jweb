package jweb;

/**
 * A CSRF token, as returned by {@code Csrf.getOrCreateToken(request)}.
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.security.CsrfToken} —
 * the framework always hands out this subtype, so either name works in
 * declarations.</p>
 */
@SuppressWarnings("deprecation")
public class CsrfToken extends com.osmig.Jweb.framework.security.CsrfToken {

    /** Internal — obtain tokens via {@code Csrf.getOrCreateToken(...)}. */
    public CsrfToken(String value, long expiresAt) {
        super(value, expiresAt);
    }
}
