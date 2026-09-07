package com.osmig.Jweb.framework.testing;

import java.util.Map;

/**
 * @deprecated Moved to {@link jweb.TestClient} — same class, shorter import.
 *             {@code TestClient.localhost(port)} returns {@code jweb.TestClient};
 *             only the static factories still resolve through this name.
 */
@Deprecated
public class TestClient extends jweb.TestClient {

    protected TestClient(String baseUrl, Map<String, String> headers) {
        super(baseUrl, headers);
    }
}
