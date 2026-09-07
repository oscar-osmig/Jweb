package jweb;

/**
 * Server-side HTTP client with a fluent builder:
 *
 * <pre>{@code
 * import jweb.Fetch;
 * import jweb.FetchResult;
 *
 * FetchResult res = Fetch.get("https://api.example.com/users")
 *     .header("Authorization", "Bearer " + token)
 *     .execute();
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.http.Fetch} — the same
 * statics under the short import; {@code execute()} returns {@link FetchResult}.</p>
 */
public class Fetch extends com.osmig.Jweb.framework.http.Fetch {

    protected Fetch(String method, String url) {
        super(method, url);
    }
}
