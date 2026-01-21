package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * JSON operations: stringify, parse with options.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSJson.*;
 *
 * stringify(variable("data"))              // JSON.stringify(data)
 * stringify(variable("data"), 2)           // JSON.stringify(data, null, 2)
 * parse(variable("jsonStr"))               // JSON.parse(jsonStr)
 * safeParse(variable("str"), obj())        // try/catch with fallback
 * </pre>
 */
public final class JSJson {
    private JSJson() {}

    // ==================== Stringify ====================

    /** JSON.stringify(value) */
    public static Val stringify(Val value) {
        return new Val("JSON.stringify(" + value.js() + ")");
    }

    /** JSON.stringify(value, null, indent) - with indentation */
    public static Val stringify(Val value, int indent) {
        return new Val("JSON.stringify(" + value.js() + ",null," + indent + ")");
    }

    /** JSON.stringify(value, replacer) - with replacer function */
    public static Val stringify(Val value, Func replacer) {
        return new Val("JSON.stringify(" + value.js() + "," + replacer.toExpr() + ")");
    }

    /** JSON.stringify(value, replacer, indent) */
    public static Val stringify(Val value, Func replacer, int indent) {
        return new Val("JSON.stringify(" + value.js() + "," + replacer.toExpr() + "," + indent + ")");
    }

    /** JSON.stringify with array of keys to include */
    public static Val stringifyKeys(Val value, String... keys) {
        StringBuilder sb = new StringBuilder("JSON.stringify(").append(value.js()).append(",[");
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("'").append(JS.esc(keys[i])).append("'");
        }
        return new Val(sb.append("])").toString());
    }

    // ==================== Parse ====================

    /** JSON.parse(text) */
    public static Val parse(Val text) {
        return new Val("JSON.parse(" + text.js() + ")");
    }

    /** JSON.parse(text, reviver) - with reviver function */
    public static Val parse(Val text, Func reviver) {
        return new Val("JSON.parse(" + text.js() + "," + reviver.toExpr() + ")");
    }

    /** Safe parse with fallback on error */
    public static Val safeParse(Val text, Object fallback) {
        return new Val("(function(){try{return JSON.parse(" + text.js() +
            ")}catch(e){return " + JS.toJs(fallback) + "}}())");
    }

    /** Safe parse with error callback */
    public static Val safeParse(Val text, Object fallback, Func onError) {
        return new Val("(function(){try{return JSON.parse(" + text.js() +
            ")}catch(e){(" + onError.toExpr() + ")(e);return " + JS.toJs(fallback) + "}}())");
    }

    // ==================== Helpers ====================

    /** Deep clone via JSON: JSON.parse(JSON.stringify(obj)) */
    public static Val deepClone(Val obj) {
        return new Val("JSON.parse(JSON.stringify(" + obj.js() + "))");
    }

    /** Check if string is valid JSON */
    public static Val isValidJson(Val text) {
        return new Val("(function(){try{JSON.parse(" + text.js() + ");return true}catch(e){return false}}())");
    }

    /** Pretty print object */
    public static Val prettyPrint(Val obj) {
        return stringify(obj, 2);
    }

    // ==================== Common Revivers ====================

    /** Reviver that converts date strings to Date objects */
    public static Func dateReviver() {
        return JS.callback("k", "v")
            .unsafeRaw("if(typeof v==='string'&&/^\\d{4}-\\d{2}-\\d{2}T/.test(v))return new Date(v)")
            .ret(JS.variable("v"));
    }
}
