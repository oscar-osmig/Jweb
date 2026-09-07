package jweb;

/**
 * JSON Web Tokens — issue, parse and validate:
 *
 * <pre>{@code
 * import jweb.Jwt;
 *
 * Jwt.init(secret);
 * String token = Jwt.token("user-42", Duration.ofHours(8));
 * Jwt.Token parsed = Jwt.parse(token);
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.security.Jwt} — the same
 * statics (and the nested {@code Token} / {@code TokenBuilder}) under the
 * short import.</p>
 */
public class Jwt extends com.osmig.Jweb.framework.security.Jwt {

    protected Jwt() {}
}
