package com.osmig.Jweb.framework.three;

import java.io.IOException;
import java.io.InputStream;

/**
 * The vendored three.js bundle (core + OrbitControls) that ships inside the
 * jar and is served immutably at {@code /jweb/three-bundle.js}.
 *
 * <p>The bundle is built by {@code tools/three-bundle/build.sh} and committed
 * at {@code src/main/resources/jweb/three-bundle.min.js}. To upgrade three.js,
 * bump the version there and {@link #THREE_VERSION} here — it is the bundle's
 * cache-busting {@code ?v=} value, so the two must move together.</p>
 */
public final class ThreeAssets {

    /** The pinned three.js version inside the bundle; checked against the bundle's banner. */
    public static final String THREE_VERSION = "0.185.1";

    private static final String BUNDLE_RESOURCE = "/jweb/three-bundle.min.js";

    private static volatile byte[] bundle;
    private static volatile String version;

    private ThreeAssets() {}

    /**
     * The bundle's cache-busting {@code ?v=} value: the three.js version plus
     * a content hash, so a rebuilt bundle (new addon, patched build) busts
     * immutable caches even when the three.js version itself is unchanged.
     */
    public static String bundleVersion() {
        String v = version;
        if (v == null) {
            v = THREE_VERSION + "-" + Integer.toHexString(java.util.Arrays.hashCode(bundleBytes()));
            version = v;
        }
        return v;
    }

    /** The bundle's bytes, loaded from the classpath once and cached. */
    public static byte[] bundleBytes() {
        byte[] bytes = bundle;
        if (bytes == null) {
            try (InputStream in = ThreeAssets.class.getResourceAsStream(BUNDLE_RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException(
                        "three.js bundle missing from classpath: " + BUNDLE_RESOURCE
                        + " — run tools/three-bundle/build.sh and rebuild");
                }
                bytes = in.readAllBytes();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read three.js bundle", e);
            }
            bundle = bytes;
        }
        return bytes;
    }
}
