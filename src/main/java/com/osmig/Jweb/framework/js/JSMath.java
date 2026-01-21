package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;

/**
 * Extended Math operations for JavaScript DSL.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSMath.*;
 *
 * sin(variable("angle"))      // Math.sin(angle)
 * pow(variable("base"), 2)    // Math.pow(base, 2)
 * min(variable("a"), variable("b"))  // Math.min(a, b)
 * </pre>
 */
public final class JSMath {
    private JSMath() {}

    // Trigonometric
    public static Val sin(Val val) { return new Val("Math.sin(" + val.js() + ")"); }
    public static Val cos(Val val) { return new Val("Math.cos(" + val.js() + ")"); }
    public static Val tan(Val val) { return new Val("Math.tan(" + val.js() + ")"); }
    public static Val asin(Val val) { return new Val("Math.asin(" + val.js() + ")"); }
    public static Val acos(Val val) { return new Val("Math.acos(" + val.js() + ")"); }
    public static Val atan(Val val) { return new Val("Math.atan(" + val.js() + ")"); }
    public static Val atan2(Val y, Val x) { return new Val("Math.atan2(" + y.js() + "," + x.js() + ")"); }
    public static Val sinh(Val val) { return new Val("Math.sinh(" + val.js() + ")"); }
    public static Val cosh(Val val) { return new Val("Math.cosh(" + val.js() + ")"); }
    public static Val tanh(Val val) { return new Val("Math.tanh(" + val.js() + ")"); }

    // Exponential and logarithmic
    public static Val exp(Val val) { return new Val("Math.exp(" + val.js() + ")"); }
    public static Val log(Val val) { return new Val("Math.log(" + val.js() + ")"); }
    public static Val log10(Val val) { return new Val("Math.log10(" + val.js() + ")"); }
    public static Val log2(Val val) { return new Val("Math.log2(" + val.js() + ")"); }
    public static Val pow(Val base, Val exp) { return new Val("Math.pow(" + base.js() + "," + exp.js() + ")"); }
    public static Val pow(Val base, int exp) { return new Val("Math.pow(" + base.js() + "," + exp + ")"); }
    public static Val sqrt(Val val) { return new Val("Math.sqrt(" + val.js() + ")"); }
    public static Val cbrt(Val val) { return new Val("Math.cbrt(" + val.js() + ")"); }
    public static Val hypot(Val... vals) {
        StringBuilder sb = new StringBuilder("Math.hypot(");
        for (int i = 0; i < vals.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vals[i].js());
        }
        return new Val(sb.append(")").toString());
    }

    // Min/Max
    public static Val min(Val... vals) {
        StringBuilder sb = new StringBuilder("Math.min(");
        for (int i = 0; i < vals.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vals[i].js());
        }
        return new Val(sb.append(")").toString());
    }
    public static Val max(Val... vals) {
        StringBuilder sb = new StringBuilder("Math.max(");
        for (int i = 0; i < vals.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vals[i].js());
        }
        return new Val(sb.append(")").toString());
    }

    // Sign and truncation
    public static Val sign(Val val) { return new Val("Math.sign(" + val.js() + ")"); }
    public static Val trunc(Val val) { return new Val("Math.trunc(" + val.js() + ")"); }
    public static Val fround(Val val) { return new Val("Math.fround(" + val.js() + ")"); }

    // Clamping (common pattern)
    public static Val clamp(Val val, Val minVal, Val maxVal) {
        return new Val("Math.min(Math.max(" + val.js() + "," + minVal.js() + ")," + maxVal.js() + ")");
    }
    public static Val clamp(Val val, int minVal, int maxVal) {
        return new Val("Math.min(Math.max(" + val.js() + "," + minVal + ")," + maxVal + ")");
    }

    // Constants
    public static Val PI() { return new Val("Math.PI"); }
    public static Val E() { return new Val("Math.E"); }
    public static Val LN2() { return new Val("Math.LN2"); }
    public static Val LN10() { return new Val("Math.LN10"); }
    public static Val SQRT2() { return new Val("Math.SQRT2"); }

    // Random range
    public static Val randomInt(int min, int max) {
        return new Val("Math.floor(Math.random()*" + (max - min + 1) + ")+" + min);
    }
    public static Val randomRange(Val min, Val max) {
        return new Val("Math.random()*(" + max.js() + "-" + min.js() + ")+" + min.js());
    }
}
