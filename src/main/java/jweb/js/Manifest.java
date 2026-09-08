package jweb.js;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A web app manifest, in Java — the JSON a browser reads to decide it may
 * install your site:
 *
 * <pre>{@code
 * import jweb.js.Manifest;
 * import static jweb.js.Pwa.*;
 *
 * Manifest app = manifest("JWeb Demo")
 *     .shortName("JWeb")
 *     .display("standalone")
 *     .startUrl("/")
 *     .themeColor("#4f46e5")
 *     .background("#ffffff")
 *     .icon("/icon-192.png", "192x192")
 *     .icon("/icon-512.png", "512x512");
 * }</pre>
 *
 * <p>{@link Pwa#serve} puts it on a route and {@link Pwa#link} points the page
 * at it. Fields nobody set stay out of the JSON.</p>
 */
public final class Manifest {

    private final Map<String, Object> fields = new LinkedHashMap<>();
    private final List<Map<String, String>> icons = new ArrayList<>();

    /** Internal — start one with {@code Pwa.manifest(name)}. */
    public Manifest(String name) {
        fields.put("name", name);
    }

    /** The short name shown under the home-screen icon. */
    public Manifest shortName(String value) { return put("short_name", value); }

    /** A one-line description. */
    public Manifest description(String value) { return put("description", value); }

    /** {@code fullscreen}, {@code standalone}, {@code minimal-ui} or {@code browser}. */
    public Manifest display(String value) { return put("display", value); }

    /** The URL the app opens at. */
    public Manifest startUrl(String value) { return put("start_url", value); }

    /** The navigation scope the installed app keeps. */
    public Manifest scope(String value) { return put("scope", value); }

    /** The colour of the browser UI around the app. */
    public Manifest themeColor(String value) { return put("theme_color", value); }

    /** The colour painted while the app loads. */
    public Manifest background(String value) { return put("background_color", value); }

    /** {@code any}, {@code portrait} or {@code landscape}. */
    public Manifest orientation(String value) { return put("orientation", value); }

    /** {@code ltr}, {@code rtl} or {@code auto}. */
    public Manifest dir(String value) { return put("dir", value); }

    /** The manifest's language tag. */
    public Manifest lang(String value) { return put("lang", value); }

    /** A PNG icon at the given size ({@code "192x192"}). */
    public Manifest icon(String src, String sizes) {
        return icon(src, sizes, "image/png", null);
    }

    /** An icon with an explicit type and purpose ({@code "maskable"}). */
    public Manifest icon(String src, String sizes, String type, String purpose) {
        Map<String, String> icon = new LinkedHashMap<>();
        icon.put("src", src);
        icon.put("sizes", sizes);
        if (type != null) icon.put("type", type);
        if (purpose != null) icon.put("purpose", purpose);
        icons.add(icon);
        return this;
    }

    private Manifest put(String key, Object value) {
        if (value != null) fields.put(key, value);
        return this;
    }

    /** The manifest as JSON — what {@link Pwa#serve} sends. */
    public String json() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(str(e.getKey())).append(":").append(str(String.valueOf(e.getValue())));
        }
        if (!icons.isEmpty()) {
            if (!first) sb.append(",");
            sb.append("\"icons\":[");
            for (int i = 0; i < icons.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("{");
                boolean f2 = true;
                for (Map.Entry<String, String> e : icons.get(i).entrySet()) {
                    if (!f2) sb.append(",");
                    f2 = false;
                    sb.append(str(e.getKey())).append(":").append(str(e.getValue()));
                }
                sb.append("}");
            }
            sb.append("]");
        }
        return sb.append("}").toString();
    }

    @Override
    public String toString() {
        return json();
    }

    private static String str(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append("\"").toString();
    }
}
