package com.osmig.Jweb.app.docs;

import java.util.List;
import java.util.Map;

/**
 * The documentation's version registry — newest first. The docs site renders
 * for one selected version (the {@code ?v=} query param, defaulting to
 * latest): the dependency snippet shows it, and sections that arrived in a
 * later release are hidden from the sidebar and answer with a notice.
 *
 * <p>Releasing a version: add it at the head of {@link #ALL}, and if the
 * release introduced a docs section, record it in {@link #INTRODUCED}.</p>
 */
public final class DocVersions {

    /** Every selectable version, newest first. */
    private static final List<String> ALL = List.of("v2.1.0", "v2.0.0");

    /** Sections that did not always exist, mapped to the version that added them. */
    private static final Map<String, String> INTRODUCED = Map.of(
        "three", "v2.1.0"
    );

    private DocVersions() {}

    public static List<String> all() {
        return ALL;
    }

    public static String latest() {
        return ALL.get(0);
    }

    public static boolean isLatest(String version) {
        return latest().equals(version);
    }

    /** Maps null, blank or unknown requests to the latest version. */
    public static String normalize(String requested) {
        return requested != null && ALL.contains(requested) ? requested : latest();
    }

    /** Whether a docs section exists in the given (normalized) version. */
    public static boolean sectionAvailable(String section, String version) {
        String introduced = INTRODUCED.get(section);
        if (introduced == null) return true;
        return ALL.indexOf(version) <= ALL.indexOf(introduced);
    }

    /** The version that introduced a section, or null if it always existed. */
    public static String introducedIn(String section) {
        return INTRODUCED.get(section);
    }

    // ==================== The render context ====================
    // Two tools cover version-dependent docs: whole sections that exist only
    // from some release take the version as a render(...) parameter (see
    // SetupSection), while prose that merely differs INSIDE a shared section
    // branches with DocComponents.since()/before()/sinceText(), which read
    // the version of the render in flight from here. DocContent sets it
    // around every section render; outside one, current() is simply latest.

    private static final ThreadLocal<String> RENDERING = new ThreadLocal<>();

    /** The version the docs render in flight is for; latest outside a render. */
    public static String current() {
        String v = RENDERING.get();
        return v != null ? v : latest();
    }

    /** Marks the version for one render; always pair with {@link #endRender}. */
    static void beginRender(String version) {
        RENDERING.set(normalize(version));
    }

    static void endRender() {
        RENDERING.remove();
    }

    /**
     * Whether {@code version} is {@code floor} or newer. A floor that is not
     * a released version yet is newer than everything — content behind it
     * stays hidden until the release lands in {@link #ALL}.
     */
    public static boolean atLeast(String version, String floor) {
        int f = ALL.indexOf(floor);
        if (f < 0) return false;
        return ALL.indexOf(normalize(version)) <= f;
    }

    /** The docs URL for a section under a version (latest stays canonical, no param). */
    public static String href(String section, String version) {
        String base = "/docs?section=" + section;
        return isLatest(version) ? base : base + "&v=" + version;
    }
}
