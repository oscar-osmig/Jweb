package com.osmig.Jweb.framework.testing;

/**
 * @deprecated Moved to {@link jweb.MockRequest} — same class, shorter import.
 *             {@code MockRequest.get("/")} returns {@code jweb.MockRequest};
 *             only the static factories still resolve through this name.
 */
@Deprecated
public class MockRequest extends jweb.MockRequest {

    private MockRequest() {}
}
