package com.osmig.Jweb.framework.security;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Per-request CSP nonce for inline scripts.
 *
 * <p>{@code JWebController} calls {@link #begin()} at the start of each
 * request and {@link #clear()} when it finishes. While a nonce is active,
 * the HTML serializer stamps it onto every {@code <script>} element, and
 * {@code Middlewares.securityHeaders()} emits a
 * {@code Content-Security-Policy} that only allows scripts carrying it —
 * so injected markup can never execute script even if it slips past
 * output escaping.</p>
 *
 * <p>Rendering that happens off the request thread (e.g. Suspense blocks
 * on virtual threads) must propagate the nonce: capture {@link #current()}
 * on the request thread and wrap the async render in
 * {@link #runWith(String, Runnable)} or {@link #set(String)}/{@link #clear()}.</p>
 */
public final class CspNonce {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private CspNonce() {
    }

    /** Generates a fresh 128-bit nonce and binds it to this thread. */
    public static String begin() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        CURRENT.set(nonce);
        return nonce;
    }

    /** The nonce bound to this thread, or null outside a request. */
    public static String current() {
        return CURRENT.get();
    }

    /** Binds an existing nonce to this thread (async render propagation). */
    public static void set(String nonce) {
        if (nonce == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(nonce);
        }
    }

    /** Unbinds the nonce from this thread. */
    public static void clear() {
        CURRENT.remove();
    }

    /** Runs {@code task} with the given nonce bound, restoring the previous binding after. */
    public static void runWith(String nonce, Runnable task) {
        String previous = CURRENT.get();
        set(nonce);
        try {
            task.run();
        } finally {
            set(previous);
        }
    }

    /**
     * A {@code nonce="..."} attribute (with leading space) for the current
     * thread's nonce, or an empty string when none is active. For code that
     * builds script tags as strings.
     */
    public static String attr() {
        String nonce = CURRENT.get();
        return nonce == null ? "" : " nonce=\"" + nonce + "\"";
    }
}
