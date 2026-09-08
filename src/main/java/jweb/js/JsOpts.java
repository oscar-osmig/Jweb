package jweb.js;

import jweb.Val;

/**
 * Builds the small options object every behavior passes to its client-runtime
 * helper. Package-private: the shape is an implementation detail of the pair
 * (the Java builder, the {@code JWeb.*} function it calls).
 */
final class JsOpts {

    private final StringBuilder sb = new StringBuilder();

    static JsOpts opts() { return new JsOpts(); }

    /** Escapes a Java String for a single-quoted JS literal. */
    static String esc(String s) {
        return com.osmig.Jweb.framework.js.JS.esc(s);
    }

    /** A single-quoted JS string literal. */
    static String quote(String s) {
        return "'" + esc(s) + "'";
    }

    JsOpts raw(String key, String jsValue) {
        if (jsValue != null) {
            if (sb.length() > 0) sb.append(",");
            sb.append(key).append(":").append(jsValue);
        }
        return this;
    }

    JsOpts str(String key, String value) {
        return value == null ? this : raw(key, quote(value));
    }

    JsOpts num(String key, Integer value) {
        return value == null ? this : raw(key, value.toString());
    }

    JsOpts num(String key, Double value) {
        return value == null ? this : raw(key, jsNumber(value));
    }

    JsOpts flag(String key, boolean value) {
        return value ? raw(key, "true") : this;
    }

    JsOpts val(String key, Val value) {
        return value == null ? this : raw(key, value.js());
    }

    /** A one-argument function literal, for options the runtime calls back into. */
    JsOpts fn(String key, String param, Val body) {
        return body == null ? this : raw(key, "function(" + param + "){return " + body.js() + "}");
    }

    String js() {
        return "{" + sb + "}";
    }

    /** Whole doubles serialize without the {@code .0} tail. */
    static String jsNumber(double d) {
        return d == Math.rint(d) && !Double.isInfinite(d)
            ? String.valueOf((long) d)
            : String.valueOf(d);
    }
}
