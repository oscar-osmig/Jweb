package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Performance API for monitoring and measuring web application performance.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSPerformance.*;
 *
 * // High Resolution Time
 * now()                                  // performance.now()
 * timeOrigin()                           // performance.timeOrigin
 *
 * // User Timing API
 * mark("start")                          // performance.mark('start')
 * measure("load-time", "start", "end")   // performance.measure('load-time', 'start', 'end')
 * clearMarks("start")                    // performance.clearMarks('start')
 * getEntriesByName("start")              // performance.getEntriesByName('start')
 *
 * // Navigation Timing
 * navigationStart()                      // performance.timing.navigationStart
 * loadEventEnd()                         // performance.timing.loadEventEnd
 * domContentLoadedEventEnd()             // performance.timing.domContentLoadedEventEnd
 *
 * // Resource Timing
 * setResourceTimingBufferSize(200)       // performance.setResourceTimingBufferSize(200)
 * clearResourceTimings()                 // performance.clearResourceTimings()
 *
 * // PerformanceObserver
 * performanceObserver()
 *     .observe("measure", "navigation")
 *     .onEntries(callback("list").log(variable("list").dot("getEntries()")))
 *     .build("perfObs");
 * </pre>
 */
public final class JSPerformance {
    private JSPerformance() {}

    // ==================== High Resolution Time ====================

    /**
     * Returns high-resolution timestamp: performance.now()
     * Measures time elapsed since time origin (page load).
     */
    public static Val now() {
        return new Val("performance.now()");
    }

    /**
     * Returns time origin: performance.timeOrigin
     * Timestamp when performance measurement began (page navigation start).
     */
    public static Val timeOrigin() {
        return new Val("performance.timeOrigin");
    }

    // ==================== User Timing API ====================

    /**
     * Creates a performance mark: performance.mark(name)
     * Marks a specific point in time.
     */
    public static Val mark(String name) {
        return new Val("performance.mark('" + JS.esc(name) + "')");
    }

    /**
     * Creates a performance mark with options: performance.mark(name, options)
     * Marks a specific point with custom startTime or detail.
     */
    public static Val mark(String name, Val options) {
        return new Val("performance.mark('" + JS.esc(name) + "'," + options.js() + ")");
    }

    /**
     * Measures time between two marks: performance.measure(name, startMark, endMark)
     * Creates a named measurement between two performance marks.
     */
    public static Val measure(String name, String startMark, String endMark) {
        return new Val("performance.measure('" + JS.esc(name) + "','" + JS.esc(startMark) + "','" + JS.esc(endMark) + "')");
    }

    /**
     * Measures time from navigation start: performance.measure(name)
     * Creates a measurement from navigation start to now.
     */
    public static Val measure(String name) {
        return new Val("performance.measure('" + JS.esc(name) + "')");
    }

    /**
     * Measures time with options: performance.measure(name, options)
     * Provides fine-grained control over measurement timing.
     */
    public static Val measure(String name, Val options) {
        return new Val("performance.measure('" + JS.esc(name) + "'," + options.js() + ")");
    }

    /**
     * Clears specific performance mark: performance.clearMarks(name)
     */
    public static Val clearMarks(String name) {
        return new Val("performance.clearMarks('" + JS.esc(name) + "')");
    }

    /**
     * Clears all performance marks: performance.clearMarks()
     */
    public static Val clearMarks() {
        return new Val("performance.clearMarks()");
    }

    /**
     * Clears specific performance measure: performance.clearMeasures(name)
     */
    public static Val clearMeasures(String name) {
        return new Val("performance.clearMeasures('" + JS.esc(name) + "')");
    }

    /**
     * Clears all performance measures: performance.clearMeasures()
     */
    public static Val clearMeasures() {
        return new Val("performance.clearMeasures()");
    }

    /**
     * Gets performance entries by name: performance.getEntriesByName(name)
     */
    public static Val getEntriesByName(String name) {
        return new Val("performance.getEntriesByName('" + JS.esc(name) + "')");
    }

    /**
     * Gets performance entries by name and type: performance.getEntriesByName(name, type)
     */
    public static Val getEntriesByName(String name, String type) {
        return new Val("performance.getEntriesByName('" + JS.esc(name) + "','" + JS.esc(type) + "')");
    }

    /**
     * Gets performance entries by type: performance.getEntriesByType(type)
     * Types: "mark", "measure", "navigation", "resource", "paint", etc.
     */
    public static Val getEntriesByType(String type) {
        return new Val("performance.getEntriesByType('" + JS.esc(type) + "')");
    }

    /**
     * Gets all performance entries: performance.getEntries()
     */
    public static Val getEntries() {
        return new Val("performance.getEntries()");
    }

    // ==================== Navigation Timing (Legacy) ====================

    /**
     * Gets performance.timing object (deprecated but widely used)
     */
    public static Val timing() {
        return new Val("performance.timing");
    }

    /**
     * Navigation start timestamp: performance.timing.navigationStart
     */
    public static Val navigationStart() {
        return new Val("performance.timing.navigationStart");
    }

    /**
     * DOM loading timestamp: performance.timing.domLoading
     */
    public static Val domLoading() {
        return new Val("performance.timing.domLoading");
    }

    /**
     * DOM interactive timestamp: performance.timing.domInteractive
     */
    public static Val domInteractive() {
        return new Val("performance.timing.domInteractive");
    }

    /**
     * DOM content loaded event start: performance.timing.domContentLoadedEventStart
     */
    public static Val domContentLoadedEventStart() {
        return new Val("performance.timing.domContentLoadedEventStart");
    }

    /**
     * DOM content loaded event end: performance.timing.domContentLoadedEventEnd
     */
    public static Val domContentLoadedEventEnd() {
        return new Val("performance.timing.domContentLoadedEventEnd");
    }

    /**
     * DOM complete timestamp: performance.timing.domComplete
     */
    public static Val domComplete() {
        return new Val("performance.timing.domComplete");
    }

    /**
     * Load event start: performance.timing.loadEventStart
     */
    public static Val loadEventStart() {
        return new Val("performance.timing.loadEventStart");
    }

    /**
     * Load event end: performance.timing.loadEventEnd
     */
    public static Val loadEventEnd() {
        return new Val("performance.timing.loadEventEnd");
    }

    /**
     * Unload event start: performance.timing.unloadEventStart
     */
    public static Val unloadEventStart() {
        return new Val("performance.timing.unloadEventStart");
    }

    /**
     * Unload event end: performance.timing.unloadEventEnd
     */
    public static Val unloadEventEnd() {
        return new Val("performance.timing.unloadEventEnd");
    }

    /**
     * Redirect start: performance.timing.redirectStart
     */
    public static Val redirectStart() {
        return new Val("performance.timing.redirectStart");
    }

    /**
     * Redirect end: performance.timing.redirectEnd
     */
    public static Val redirectEnd() {
        return new Val("performance.timing.redirectEnd");
    }

    /**
     * Fetch start: performance.timing.fetchStart
     */
    public static Val fetchStart() {
        return new Val("performance.timing.fetchStart");
    }

    /**
     * Domain lookup start: performance.timing.domainLookupStart
     */
    public static Val domainLookupStart() {
        return new Val("performance.timing.domainLookupStart");
    }

    /**
     * Domain lookup end: performance.timing.domainLookupEnd
     */
    public static Val domainLookupEnd() {
        return new Val("performance.timing.domainLookupEnd");
    }

    /**
     * Connect start: performance.timing.connectStart
     */
    public static Val connectStart() {
        return new Val("performance.timing.connectStart");
    }

    /**
     * Connect end: performance.timing.connectEnd
     */
    public static Val connectEnd() {
        return new Val("performance.timing.connectEnd");
    }

    /**
     * Secure connection start: performance.timing.secureConnectionStart
     */
    public static Val secureConnectionStart() {
        return new Val("performance.timing.secureConnectionStart");
    }

    /**
     * Request start: performance.timing.requestStart
     */
    public static Val requestStart() {
        return new Val("performance.timing.requestStart");
    }

    /**
     * Response start: performance.timing.responseStart
     */
    public static Val responseStart() {
        return new Val("performance.timing.responseStart");
    }

    /**
     * Response end: performance.timing.responseEnd
     */
    public static Val responseEnd() {
        return new Val("performance.timing.responseEnd");
    }

    // ==================== Navigation Timing Level 2 ====================

    /**
     * Gets navigation entry: performance.getEntriesByType('navigation')[0]
     * Modern replacement for performance.timing.
     */
    public static Val navigationEntry() {
        return new Val("performance.getEntriesByType('navigation')[0]");
    }

    /**
     * Navigation type: performance.navigation.type
     * 0: navigate, 1: reload, 2: back_forward, 255: reserved
     */
    public static Val navigationType() {
        return new Val("performance.navigation.type");
    }

    /**
     * Redirect count: performance.navigation.redirectCount
     */
    public static Val redirectCount() {
        return new Val("performance.navigation.redirectCount");
    }

    // ==================== Resource Timing ====================

    /**
     * Gets all resource entries: performance.getEntriesByType('resource')
     */
    public static Val resourceEntries() {
        return new Val("performance.getEntriesByType('resource')");
    }

    /**
     * Sets resource timing buffer size: performance.setResourceTimingBufferSize(size)
     */
    public static Val setResourceTimingBufferSize(int size) {
        return new Val("performance.setResourceTimingBufferSize(" + size + ")");
    }

    /**
     * Clears resource timing buffer: performance.clearResourceTimings()
     */
    public static Val clearResourceTimings() {
        return new Val("performance.clearResourceTimings()");
    }

    /**
     * Adds listener for buffer full event: performance.onresourcetimingbufferfull
     */
    public static Val onResourceTimingBufferFull(Func handler) {
        return new Val("performance.onresourcetimingbufferfull=" + handler.toExpr());
    }

    // ==================== Paint Timing ====================

    /**
     * Gets paint entries: performance.getEntriesByType('paint')
     * Includes 'first-paint' and 'first-contentful-paint'.
     */
    public static Val paintEntries() {
        return new Val("performance.getEntriesByType('paint')");
    }

    /**
     * Gets first paint entry: performance.getEntriesByName('first-paint')[0]
     */
    public static Val firstPaint() {
        return new Val("performance.getEntriesByName('first-paint')[0]");
    }

    /**
     * Gets first contentful paint entry: performance.getEntriesByName('first-contentful-paint')[0]
     */
    public static Val firstContentfulPaint() {
        return new Val("performance.getEntriesByName('first-contentful-paint')[0]");
    }

    // ==================== Long Tasks API ====================

    /**
     * Gets long task entries: performance.getEntriesByType('longtask')
     * Requires PerformanceObserver for real-time monitoring.
     */
    public static Val longTaskEntries() {
        return new Val("performance.getEntriesByType('longtask')");
    }

    // ==================== Element Timing ====================

    /**
     * Gets element timing entries: performance.getEntriesByType('element')
     * Requires elements with elementtiming attribute.
     */
    public static Val elementEntries() {
        return new Val("performance.getEntriesByType('element')");
    }

    // ==================== Largest Contentful Paint ====================

    /**
     * Gets largest contentful paint entries: performance.getEntriesByType('largest-contentful-paint')
     * Requires PerformanceObserver for real-time monitoring.
     */
    public static Val largestContentfulPaintEntries() {
        return new Val("performance.getEntriesByType('largest-contentful-paint')");
    }

    // ==================== Memory (Chrome Only) ====================

    /**
     * Gets memory info: performance.memory
     * Chrome-only, returns usedJSHeapSize, totalJSHeapSize, jsHeapSizeLimit.
     */
    public static Val memory() {
        return new Val("performance.memory");
    }

    /**
     * Used JS heap size: performance.memory.usedJSHeapSize
     */
    public static Val usedJSHeapSize() {
        return new Val("performance.memory.usedJSHeapSize");
    }

    /**
     * Total JS heap size: performance.memory.totalJSHeapSize
     */
    public static Val totalJSHeapSize() {
        return new Val("performance.memory.totalJSHeapSize");
    }

    /**
     * JS heap size limit: performance.memory.jsHeapSizeLimit
     */
    public static Val jsHeapSizeLimit() {
        return new Val("performance.memory.jsHeapSizeLimit");
    }

    // ==================== PerformanceObserver ====================

    /**
     * Creates a PerformanceObserver builder.
     * Observes performance entries as they are recorded.
     */
    public static PerformanceObserverBuilder performanceObserver() {
        return new PerformanceObserverBuilder();
    }

    /**
     * Checks if entry type is supported: PerformanceObserver.supportedEntryTypes
     */
    public static Val supportedEntryTypes() {
        return new Val("PerformanceObserver.supportedEntryTypes");
    }

    /**
     * Builder for PerformanceObserver API.
     */
    public static class PerformanceObserverBuilder {
        private Func callback;
        private String callbackCode;
        private String[] entryTypes;
        private String type;
        private boolean buffered;

        /**
         * Sets the callback function to handle performance entries.
         * Callback receives (list, observer) where list has getEntries() method.
         */
        public PerformanceObserverBuilder onEntries(Func callback) {
            this.callback = callback;
            this.callbackCode = null;
            return this;
        }

        /**
         * Sets the callback using raw JavaScript code.
         */
        public PerformanceObserverBuilder onEntries(String code) {
            this.callbackCode = code;
            this.callback = null;
            return this;
        }

        /**
         * Observes specific entry types (for backward compatibility).
         * Types: "mark", "measure", "navigation", "resource", "paint", "longtask", "element", "largest-contentful-paint"
         */
        public PerformanceObserverBuilder observe(String... types) {
            this.entryTypes = types;
            return this;
        }

        /**
         * Observes a single entry type (modern approach).
         * Allows buffered option to get historical entries.
         */
        public PerformanceObserverBuilder type(String type) {
            this.type = type;
            return this;
        }

        /**
         * Enables buffered mode to include historical entries.
         * Only works with type() method, not observe().
         */
        public PerformanceObserverBuilder buffered() {
            this.buffered = true;
            return this;
        }

        /**
         * Enables or disables buffered mode.
         */
        public PerformanceObserverBuilder buffered(boolean buffered) {
            this.buffered = buffered;
            return this;
        }

        private String buildCallback() {
            if (callback != null) return callback.toExpr();
            return "function(list,observer){" + (callbackCode != null ? callbackCode : "") + "}";
        }

        private String buildObserveOptions() {
            if (type != null) {
                // Modern approach: {type: 'X', buffered: true}
                String opts = "{type:'" + JS.esc(type) + "'";
                if (buffered) opts += ",buffered:true";
                return opts + "}";
            } else if (entryTypes != null && entryTypes.length > 0) {
                // Legacy approach: {entryTypes: ['X', 'Y']}
                StringBuilder sb = new StringBuilder("{entryTypes:[");
                for (int i = 0; i < entryTypes.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append("'").append(JS.esc(entryTypes[i])).append("'");
                }
                return sb.append("]}").toString();
            }
            return "{entryTypes:[]}";
        }

        /**
         * Builds and returns the PerformanceObserver as a Val.
         * Stores observer in variable and begins observing.
         */
        public Val build(String varName) {
            return new Val("var " + varName + "=new PerformanceObserver(" + buildCallback() + ");" +
                varName + ".observe(" + buildObserveOptions() + ")");
        }

        /**
         * Builds the observer code as a string.
         */
        public String buildCode(String varName) {
            return build(varName).js();
        }

        /**
         * Creates observer without starting observation.
         * Use this if you want to manually call observe() later.
         */
        public Val create(String varName) {
            return new Val("var " + varName + "=new PerformanceObserver(" + buildCallback() + ")");
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Disconnects a PerformanceObserver: observer.disconnect()
     */
    public static Val disconnect(String observerVar) {
        return new Val(observerVar + ".disconnect()");
    }

    /**
     * Takes pending records: observer.takeRecords()
     */
    public static Val takeRecords(String observerVar) {
        return new Val(observerVar + ".takeRecords()");
    }

    /**
     * Manually observe with observer: observer.observe(options)
     */
    public static Val observeWith(String observerVar, Val options) {
        return new Val(observerVar + ".observe(" + options.js() + ")");
    }

    // ==================== Utility Calculations ====================

    /**
     * Calculates page load time: loadEventEnd - navigationStart
     * Returns duration in milliseconds.
     */
    public static Val calculatePageLoadTime() {
        return new Val("(performance.timing.loadEventEnd-performance.timing.navigationStart)");
    }

    /**
     * Calculates DOM ready time: domContentLoadedEventEnd - navigationStart
     * Returns duration in milliseconds.
     */
    public static Val calculateDOMReadyTime() {
        return new Val("(performance.timing.domContentLoadedEventEnd-performance.timing.navigationStart)");
    }

    /**
     * Calculates DNS lookup time: domainLookupEnd - domainLookupStart
     */
    public static Val calculateDNSTime() {
        return new Val("(performance.timing.domainLookupEnd-performance.timing.domainLookupStart)");
    }

    /**
     * Calculates TCP connection time: connectEnd - connectStart
     */
    public static Val calculateTCPTime() {
        return new Val("(performance.timing.connectEnd-performance.timing.connectStart)");
    }

    /**
     * Calculates server response time: responseEnd - requestStart
     */
    public static Val calculateServerResponseTime() {
        return new Val("(performance.timing.responseEnd-performance.timing.requestStart)");
    }

    /**
     * Calculates time to first byte: responseStart - navigationStart
     */
    public static Val calculateTimeToFirstByte() {
        return new Val("(performance.timing.responseStart-performance.timing.navigationStart)");
    }

    /**
     * Calculates DOM processing time: domComplete - domLoading
     */
    public static Val calculateDOMProcessingTime() {
        return new Val("(performance.timing.domComplete-performance.timing.domLoading)");
    }

    /**
     * Calculates redirect time: redirectEnd - redirectStart
     */
    public static Val calculateRedirectTime() {
        return new Val("(performance.timing.redirectEnd-performance.timing.redirectStart)");
    }
}
