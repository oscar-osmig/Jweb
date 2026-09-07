package jweb;

/**
 * Password hashing and strength checks (BCrypt under the hood):
 *
 * <pre>{@code
 * import jweb.Password;
 *
 * String hash = Password.hash(plain);
 * boolean ok  = Password.verify(plain, hash);
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.security.Password} —
 * the same statics under the short import.</p>
 */
public class Password extends com.osmig.Jweb.framework.security.Password {

    protected Password() {}
}
