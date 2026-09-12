package com.osmig.Jweb.framework.server;

/**
 * The pre-3.0 spelling of the Set-Cookie builder.
 *
 * @deprecated Replaced by {@link jweb.http.Cookie} — {@code Cookie.of(...)}
 *             (inherited here) hands out that type, so declare
 *             {@code jweb.http.Cookie}. It lives in {@code jweb.http} rather
 *             than {@code jweb} so it never collides with the {@code @Cookie}
 *             parameter annotation in {@code jweb.api}.
 */
@Deprecated
public class Cookie extends jweb.http.Cookie {

    protected Cookie(String name, String value) {
        super(name, value);
    }
}
