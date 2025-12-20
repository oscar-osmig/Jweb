package com.osmig.Jweb.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized logging utility for JWeb framework.
 *
 * <p>Provides convenient static methods for logging throughout the framework.
 * Uses SLF4J under the hood, which integrates with Spring Boot's Logback.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * Log.info("Server started on port {}", port);
 * Log.debug("Processing request: {}", path);
 * Log.warn("Deprecated API used");
 * Log.error("Failed to process request", exception);
 * </pre>
 *
 * <p>For class-specific loggers:</p>
 * <pre>
 * private static final Logger log = Log.forClass(MyClass.class);
 * log.info("Message");
 * </pre>
 */
public final class Log {

    private static final Logger FRAMEWORK_LOGGER = LoggerFactory.getLogger("JWeb");

    private Log() {
        // Static utility class
    }

    /**
     * Gets a logger for a specific class.
     *
     * @param clazz the class
     * @return the logger
     */
    public static Logger forClass(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Gets a logger with a custom name.
     *
     * @param name the logger name
     * @return the logger
     */
    public static Logger named(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * Gets the framework logger.
     *
     * @return the JWeb framework logger
     */
    public static Logger framework() {
        return FRAMEWORK_LOGGER;
    }

    // ========== Trace Level ==========

    public static void trace(String message) {
        FRAMEWORK_LOGGER.trace(message);
    }

    public static void trace(String format, Object arg) {
        FRAMEWORK_LOGGER.trace(format, arg);
    }

    public static void trace(String format, Object arg1, Object arg2) {
        FRAMEWORK_LOGGER.trace(format, arg1, arg2);
    }

    public static void trace(String format, Object... args) {
        FRAMEWORK_LOGGER.trace(format, args);
    }

    // ========== Debug Level ==========

    public static void debug(String message) {
        FRAMEWORK_LOGGER.debug(message);
    }

    public static void debug(String format, Object arg) {
        FRAMEWORK_LOGGER.debug(format, arg);
    }

    public static void debug(String format, Object arg1, Object arg2) {
        FRAMEWORK_LOGGER.debug(format, arg1, arg2);
    }

    public static void debug(String format, Object... args) {
        FRAMEWORK_LOGGER.debug(format, args);
    }

    // ========== Info Level ==========

    public static void info(String message) {
        FRAMEWORK_LOGGER.info(message);
    }

    public static void info(String format, Object arg) {
        FRAMEWORK_LOGGER.info(format, arg);
    }

    public static void info(String format, Object arg1, Object arg2) {
        FRAMEWORK_LOGGER.info(format, arg1, arg2);
    }

    public static void info(String format, Object... args) {
        FRAMEWORK_LOGGER.info(format, args);
    }

    // ========== Warn Level ==========

    public static void warn(String message) {
        FRAMEWORK_LOGGER.warn(message);
    }

    public static void warn(String format, Object arg) {
        FRAMEWORK_LOGGER.warn(format, arg);
    }

    public static void warn(String format, Object arg1, Object arg2) {
        FRAMEWORK_LOGGER.warn(format, arg1, arg2);
    }

    public static void warn(String format, Object... args) {
        FRAMEWORK_LOGGER.warn(format, args);
    }

    public static void warn(String message, Throwable t) {
        FRAMEWORK_LOGGER.warn(message, t);
    }

    // ========== Error Level ==========

    public static void error(String message) {
        FRAMEWORK_LOGGER.error(message);
    }

    public static void error(String format, Object arg) {
        FRAMEWORK_LOGGER.error(format, arg);
    }

    public static void error(String format, Object arg1, Object arg2) {
        FRAMEWORK_LOGGER.error(format, arg1, arg2);
    }

    public static void error(String format, Object... args) {
        FRAMEWORK_LOGGER.error(format, args);
    }

    public static void error(String message, Throwable t) {
        FRAMEWORK_LOGGER.error(message, t);
    }

    // ========== Level Checks ==========

    public static boolean isTraceEnabled() {
        return FRAMEWORK_LOGGER.isTraceEnabled();
    }

    public static boolean isDebugEnabled() {
        return FRAMEWORK_LOGGER.isDebugEnabled();
    }

    public static boolean isInfoEnabled() {
        return FRAMEWORK_LOGGER.isInfoEnabled();
    }

    public static boolean isWarnEnabled() {
        return FRAMEWORK_LOGGER.isWarnEnabled();
    }

    public static boolean isErrorEnabled() {
        return FRAMEWORK_LOGGER.isErrorEnabled();
    }
}
