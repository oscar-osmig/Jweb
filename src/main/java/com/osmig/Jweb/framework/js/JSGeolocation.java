package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Geolocation API for location services.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSGeolocation.*;
 *
 * // Get current position
 * getCurrentPosition()
 *     .onSuccess(callback("pos").log(latitude(variable("pos"))))
 *     .onError(callback("err").log(variable("err").dot("message")))
 *     .build()
 *
 * // Watch position changes
 * watchPosition()
 *     .onSuccess(callback("pos").call("updateMap", variable("pos")))
 *     .highAccuracy(true)
 *     .build("watchId")
 *
 * // Stop watching
 * clearWatch(variable("watchId"))
 * </pre>
 */
public final class JSGeolocation {
    private JSGeolocation() {}

    // ==================== Position Requests ====================

    /** Gets current position */
    public static GeolocationBuilder getCurrentPosition() {
        return new GeolocationBuilder(false);
    }

    /** Watches position changes */
    public static GeolocationBuilder watchPosition() {
        return new GeolocationBuilder(true);
    }

    /** Stops watching position */
    public static Val clearWatch(Val watchId) {
        return new Val("navigator.geolocation.clearWatch(" + watchId.js() + ")");
    }

    // ==================== Position Properties ====================

    /** Gets latitude: position.coords.latitude */
    public static Val latitude(Val position) { return new Val(position.js() + ".coords.latitude"); }

    /** Gets longitude: position.coords.longitude */
    public static Val longitude(Val position) { return new Val(position.js() + ".coords.longitude"); }

    /** Gets accuracy in meters: position.coords.accuracy */
    public static Val accuracy(Val position) { return new Val(position.js() + ".coords.accuracy"); }

    /** Gets altitude: position.coords.altitude */
    public static Val altitude(Val position) { return new Val(position.js() + ".coords.altitude"); }

    /** Gets altitude accuracy: position.coords.altitudeAccuracy */
    public static Val altitudeAccuracy(Val position) { return new Val(position.js() + ".coords.altitudeAccuracy"); }

    /** Gets heading in degrees: position.coords.heading */
    public static Val heading(Val position) { return new Val(position.js() + ".coords.heading"); }

    /** Gets speed in m/s: position.coords.speed */
    public static Val speed(Val position) { return new Val(position.js() + ".coords.speed"); }

    /** Gets timestamp: position.timestamp */
    public static Val timestamp(Val position) { return new Val(position.js() + ".timestamp"); }

    /** Gets coords object: position.coords */
    public static Val coords(Val position) { return new Val(position.js() + ".coords"); }

    // ==================== Error Properties ====================

    /** Gets error code: error.code */
    public static Val errorCode(Val error) { return new Val(error.js() + ".code"); }

    /** Gets error message: error.message */
    public static Val errorMessage(Val error) { return new Val(error.js() + ".message"); }

    /** Permission denied error code */
    public static Val PERMISSION_DENIED() { return new Val("1"); }

    /** Position unavailable error code */
    public static Val POSITION_UNAVAILABLE() { return new Val("2"); }

    /** Timeout error code */
    public static Val TIMEOUT() { return new Val("3"); }

    // ==================== Utilities ====================

    /** Checks if geolocation is available */
    public static Val isSupported() {
        return new Val("('geolocation' in navigator)");
    }

    /** Calculates distance between two coordinates (Haversine formula) */
    public static Val distanceBetween(Val lat1, Val lon1, Val lat2, Val lon2) {
        return new Val("(function(){var R=6371e3;var r=Math.PI/180;var a=Math.sin((" + lat2.js() + "-" + lat1.js() + ")*r/2)**2+Math.cos(" + lat1.js() + "*r)*Math.cos(" + lat2.js() + "*r)*Math.sin((" + lon2.js() + "-" + lon1.js() + ")*r/2)**2;return 2*R*Math.atan2(Math.sqrt(a),Math.sqrt(1-a))}())");
    }

    // ==================== Builder ====================

    public static class GeolocationBuilder {
        private final boolean watch;
        private Func successFunc, errorFunc;
        private String successCode, errorCode;
        private Boolean enableHighAccuracy;
        private Integer timeout;
        private Integer maximumAge;

        GeolocationBuilder(boolean watch) { this.watch = watch; }

        public GeolocationBuilder onSuccess(Func callback) { this.successFunc = callback; return this; }
        public GeolocationBuilder onSuccess(String code) { this.successCode = code; return this; }

        public GeolocationBuilder onError(Func callback) { this.errorFunc = callback; return this; }
        public GeolocationBuilder onError(String code) { this.errorCode = code; return this; }

        public GeolocationBuilder highAccuracy(boolean enabled) { this.enableHighAccuracy = enabled; return this; }
        public GeolocationBuilder timeout(int ms) { this.timeout = ms; return this; }
        public GeolocationBuilder maximumAge(int ms) { this.maximumAge = ms; return this; }

        /** Builds and optionally assigns watch ID to variable */
        public Val build(String varName) {
            if (!watch) throw new IllegalStateException("Use build() for getCurrentPosition");
            StringBuilder sb = new StringBuilder("var ").append(varName).append("=");
            sb.append(buildCall());
            return new Val(sb.toString());
        }

        /** Builds the geolocation call */
        public Val build() {
            return new Val(buildCall());
        }

        private String buildCall() {
            StringBuilder sb = new StringBuilder("navigator.geolocation.");
            sb.append(watch ? "watchPosition" : "getCurrentPosition").append("(");

            // Success callback
            if (successFunc != null) sb.append(successFunc.toExpr());
            else if (successCode != null) sb.append("function(p){").append(successCode).append("}");
            else sb.append("function(){}");

            // Error callback
            if (errorFunc != null || errorCode != null) {
                sb.append(",");
                if (errorFunc != null) sb.append(errorFunc.toExpr());
                else sb.append("function(e){").append(errorCode).append("}");
            }

            // Options
            if (enableHighAccuracy != null || timeout != null || maximumAge != null) {
                if (errorFunc == null && errorCode == null) sb.append(",null");
                sb.append(",{");
                boolean first = true;
                if (enableHighAccuracy != null) { sb.append("enableHighAccuracy:").append(enableHighAccuracy); first = false; }
                if (timeout != null) { if (!first) sb.append(","); sb.append("timeout:").append(timeout); first = false; }
                if (maximumAge != null) { if (!first) sb.append(","); sb.append("maximumAge:").append(maximumAge); }
                sb.append("}");
            }

            return sb.append(")").toString();
        }
    }
}
