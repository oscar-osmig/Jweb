package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Stmt;

/**
 * Modern JavaScript operators: optional chaining, nullish coalescing, logical assignment.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSOperators.*;
 *
 * optionalChain(variable("user"), "address", "city")   // user?.address?.city
 * nullishCoalesce(variable("name"), str("Anonymous"))  // name ?? 'Anonymous'
 * orAssign(variable("config"), obj("debug", false))    // config ||= {debug: false}
 * </pre>
 */
public final class JSOperators {
    private JSOperators() {}

    // ==================== Optional Chaining ====================

    /** Optional property chain: obj?.prop1?.prop2 */
    public static Val optionalChain(Val obj, String... props) {
        StringBuilder sb = new StringBuilder(obj.js());
        for (String prop : props) {
            sb.append("?.").append(prop);
        }
        return new Val(sb.toString());
    }

    /** Optional method call: obj?.method() */
    public static Val optionalCall(Val obj, String method, Object... args) {
        StringBuilder sb = new StringBuilder(obj.js()).append("?.").append(method).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(JS.toJs(args[i]));
        }
        return new Val(sb.append(")").toString());
    }

    /** Optional array access: arr?.[index] */
    public static Val optionalAt(Val arr, int index) {
        return new Val(arr.js() + "?.[" + index + "]");
    }

    /** Optional array access with expression: arr?.[expr] */
    public static Val optionalAt(Val arr, Val index) {
        return new Val(arr.js() + "?.[" + index.js() + "]");
    }

    // ==================== Nullish Coalescing ====================

    /** Nullish coalescing: a ?? b */
    public static Val nullishCoalesce(Val value, Object fallback) {
        return new Val("(" + value.js() + "??(" + JS.toJs(fallback) + "))");
    }

    /** Chained nullish: a ?? b ?? c */
    public static Val nullishChain(Val... values) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append("??");
            sb.append(values[i].js());
        }
        return new Val(sb.append(")").toString());
    }

    // ==================== Logical Assignment ====================

    /** Nullish assignment: x ??= value */
    public static Stmt nullishAssign(Val target, Object value) {
        return new Stmt(target.js() + "??=" + JS.toJs(value));
    }

    /** Or assignment: x ||= value */
    public static Stmt orAssign(Val target, Object value) {
        return new Stmt(target.js() + "||=" + JS.toJs(value));
    }

    /** And assignment: x &&= value */
    public static Stmt andAssign(Val target, Object value) {
        return new Stmt(target.js() + "&&=" + JS.toJs(value));
    }

    // ==================== Destructuring Helpers ====================

    /** Extracts property with default: (obj.prop ?? defaultVal) */
    public static Val extractOr(Val obj, String prop, Object defaultVal) {
        return new Val("(" + obj.js() + "." + prop + "??(" + JS.toJs(defaultVal) + "))");
    }

    /** Safely extracts nested property: obj?.a?.b ?? defaultVal */
    public static Val safeExtract(Val obj, Object defaultVal, String... path) {
        StringBuilder sb = new StringBuilder("(").append(obj.js());
        for (String prop : path) {
            sb.append("?.").append(prop);
        }
        return new Val(sb.append("??(" + JS.toJs(defaultVal) + "))").toString());
    }

    // ==================== Spread Helpers ====================

    /** Spread array: [...arr] */
    public static Val spreadArray(Val arr) {
        return new Val("[..." + arr.js() + "]");
    }

    /** Spread into array: [...arr, ...other] */
    public static Val spreadInto(Val... arrays) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arrays.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("...").append(arrays[i].js());
        }
        return new Val(sb.append("]").toString());
    }

    /** Spread call: fn(...args) */
    public static Val spreadCall(String fn, Val args) {
        return new Val(fn + "(..." + args.js() + ")");
    }

    // ==================== Bitwise Operators ====================

    /** Bitwise AND: a & b */
    public static Val bitwiseAnd(Val a, Val b) {
        return new Val("(" + a.js() + "&" + b.js() + ")");
    }

    /** Bitwise OR: a | b */
    public static Val bitwiseOr(Val a, Val b) {
        return new Val("(" + a.js() + "|" + b.js() + ")");
    }

    /** Bitwise XOR: a ^ b */
    public static Val bitwiseXor(Val a, Val b) {
        return new Val("(" + a.js() + "^" + b.js() + ")");
    }

    /** Bitwise NOT: ~a */
    public static Val bitwiseNot(Val a) {
        return new Val("(~" + a.js() + ")");
    }

    /** Left shift: a << b */
    public static Val leftShift(Val a, Val b) {
        return new Val("(" + a.js() + "<<" + b.js() + ")");
    }

    /** Left shift: a << n */
    public static Val leftShift(Val a, int n) {
        return new Val("(" + a.js() + "<<" + n + ")");
    }

    /** Right shift (sign-propagating): a >> b */
    public static Val rightShift(Val a, Val b) {
        return new Val("(" + a.js() + ">>" + b.js() + ")");
    }

    /** Right shift (sign-propagating): a >> n */
    public static Val rightShift(Val a, int n) {
        return new Val("(" + a.js() + ">>" + n + ")");
    }

    /** Unsigned right shift: a >>> b */
    public static Val unsignedRightShift(Val a, Val b) {
        return new Val("(" + a.js() + ">>>" + b.js() + ")");
    }

    /** Unsigned right shift: a >>> n */
    public static Val unsignedRightShift(Val a, int n) {
        return new Val("(" + a.js() + ">>>" + n + ")");
    }

    // ==================== Bitwise Assignment Operators ====================

    /** Bitwise AND assignment: a &= b */
    public static Stmt bitwiseAndAssign(Val target, Val value) {
        return new Stmt(target.js() + "&=" + value.js());
    }

    /** Bitwise OR assignment: a |= b */
    public static Stmt bitwiseOrAssign(Val target, Val value) {
        return new Stmt(target.js() + "|=" + value.js());
    }

    /** Bitwise XOR assignment: a ^= b */
    public static Stmt bitwiseXorAssign(Val target, Val value) {
        return new Stmt(target.js() + "^=" + value.js());
    }

    /** Left shift assignment: a <<= b */
    public static Stmt leftShiftAssign(Val target, int n) {
        return new Stmt(target.js() + "<<=" + n);
    }

    /** Right shift assignment: a >>= b */
    public static Stmt rightShiftAssign(Val target, int n) {
        return new Stmt(target.js() + ">>=" + n);
    }

    /** Unsigned right shift assignment: a >>>= b */
    public static Stmt unsignedRightShiftAssign(Val target, int n) {
        return new Stmt(target.js() + ">>>=" + n);
    }

    // ==================== Other Operators ====================

    /** Exponentiation: a ** b */
    public static Val pow(Val base, Val exponent) {
        return new Val("(" + base.js() + "**" + exponent.js() + ")");
    }

    /** Exponentiation: a ** n */
    public static Val pow(Val base, int exponent) {
        return new Val("(" + base.js() + "**" + exponent + ")");
    }

    /** Exponentiation assignment: a **= b */
    public static Stmt powAssign(Val target, Val exponent) {
        return new Stmt(target.js() + "**=" + exponent.js());
    }

    /** In operator: prop in obj */
    public static Val in_(String prop, Val obj) {
        return new Val("('" + JS.esc(prop) + "' in " + obj.js() + ")");
    }

    /** In operator: prop in obj (dynamic key) */
    public static Val in_(Val prop, Val obj) {
        return new Val("(" + prop.js() + " in " + obj.js() + ")");
    }

    /** Delete operator: delete obj.prop */
    public static Val delete_(Val obj, String prop) {
        return new Val("delete " + obj.js() + "." + prop);
    }

    /** Delete operator: delete obj[key] */
    public static Val delete_(Val obj, Val key) {
        return new Val("delete " + obj.js() + "[" + key.js() + "]");
    }

    /** Void operator: void expr */
    public static Val void_(Val expr) {
        return new Val("void " + expr.js());
    }

    /** Comma operator: (a, b, c) - evaluates all, returns last */
    public static Val comma(Val... exprs) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < exprs.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(exprs[i].js());
        }
        return new Val(sb.append(")").toString());
    }
}
