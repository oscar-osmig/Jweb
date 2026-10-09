package com.osmig.Jweb.app.api;

import jweb.MockRequest;
import jweb.Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The review API's credentials: the admin email and token as headers, checked
 * the way the login form checks them — constant time, whitespace and case
 * tolerated, failures counted per IP, and closed when nothing is configured.
 */
class AdminApiTest {

    private static AdminApi configured() {
        AdminApi api = new AdminApi(new MessageStore());
        api.configure("admin@example.com", "s3cret-token");
        return api;
    }

    private static Request withHeaders(String email, String token) {
        MockRequest req = MockRequest.get("/api/v1/admin/snippets");
        if (email != null) req.header(AdminApi.EMAIL_HEADER, email);
        if (token != null) req.header(AdminApi.TOKEN_HEADER, token);
        return req.build();
    }

    @Test
    void theAdminHeadersAuthorize() {
        assertTrue(configured().authorize(withHeaders("admin@example.com", "s3cret-token")));
        assertTrue(configured().authorize(withHeaders(" Admin@Example.com ", " s3cret-token\n")),
            "copy-paste whitespace and email case are tolerated");
    }

    @Test
    void anythingElseDoesNot() {
        AdminApi api = configured();
        assertFalse(api.authorize(withHeaders(null, null)), "no headers, no session");
        assertFalse(api.authorize(withHeaders("admin@example.com", "wrong")));
        assertFalse(api.authorize(withHeaders("someone@example.com", "s3cret-token")));
        assertFalse(api.authorize(withHeaders(null, "s3cret-token")), "the email is required too");
        assertFalse(api.authorize(withHeaders("admin@example.com", null)));
    }

    @Test
    void unconfiguredCredentialsFailClosed() {
        AdminApi api = new AdminApi(new MessageStore());
        assertFalse(api.isConfigured());
        assertFalse(api.authorize(withHeaders("admin@example.com", "")));
        assertFalse(api.authorize(withHeaders("", "")));
    }

    @Test
    void repeatedFailuresLockTheAddressOut() {
        AdminApi api = configured();
        for (int i = 0; i < 5; i++) {
            assertFalse(api.authorize(withHeaders("admin@example.com", "guess-" + i)));
        }
        assertFalse(api.authorize(withHeaders("admin@example.com", "s3cret-token")),
            "the right token no longer helps from a locked-out address");
    }
}
