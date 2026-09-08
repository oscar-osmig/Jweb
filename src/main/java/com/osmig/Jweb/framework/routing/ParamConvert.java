package com.osmig.Jweb.framework.routing;

import java.util.Arrays;
import java.util.UUID;

/**
 * String → value conversion shared by record binding ({@code app.action})
 * and the {@code @Param}/{@code @Query}/{@code @Header}/{@code @Cookie}
 * REST argument resolvers: the primitives and their boxes, String, UUID,
 * and enums by name (case-insensitive).
 */
public final class ParamConvert {

    private ParamConvert() {}

    /** Whether {@code type} is one this converter understands. */
    public static boolean supports(Class<?> type) {
        return type == String.class || type == CharSequence.class
            || type == int.class || type == Integer.class
            || type == long.class || type == Long.class
            || type == double.class || type == Double.class
            || type == float.class || type == Float.class
            || type == short.class || type == Short.class
            || type == byte.class || type == Byte.class
            || type == boolean.class || type == Boolean.class
            || type == char.class || type == Character.class
            || type == UUID.class || type.isEnum();
    }

    /**
     * Converts {@code raw} to {@code type}.
     *
     * @throws IllegalArgumentException with a reader-facing message on failure
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(String raw, Class<T> type) {
        try {
            if (type == String.class || type == CharSequence.class) return (T) raw;
            if (type == int.class || type == Integer.class) return (T) Integer.valueOf(raw.trim());
            if (type == long.class || type == Long.class) return (T) Long.valueOf(raw.trim());
            if (type == double.class || type == Double.class) return (T) Double.valueOf(raw.trim());
            if (type == float.class || type == Float.class) return (T) Float.valueOf(raw.trim());
            if (type == short.class || type == Short.class) return (T) Short.valueOf(raw.trim());
            if (type == byte.class || type == Byte.class) return (T) Byte.valueOf(raw.trim());
            if (type == boolean.class || type == Boolean.class) return (T) parseBoolean(raw);
            if (type == char.class || type == Character.class) {
                if (raw.length() != 1) throw new IllegalArgumentException("must be a single character");
                return (T) Character.valueOf(raw.charAt(0));
            }
            if (type == UUID.class) return (T) UUID.fromString(raw.trim());
            if (type.isEnum()) return (T) parseEnum(raw, (Class<? extends Enum>) type);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("must be a " + describe(type));
        }
        throw new IllegalArgumentException("has an unsupported type " + type.getSimpleName()
            + " (use String, a number, boolean, UUID, an enum, or Optional/List of one)");
    }

    private static Boolean parseBoolean(String raw) {
        String v = raw.trim().toLowerCase();
        return switch (v) {
            case "true", "1", "yes", "on" -> Boolean.TRUE;
            case "false", "0", "no", "off", "" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException("must be true or false");
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Enum parseEnum(String raw, Class<? extends Enum> type) {
        String wanted = raw.trim();
        for (Enum constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(wanted)) return constant;
        }
        throw new IllegalArgumentException("must be one of " + String.join(", ",
            Arrays.stream(type.getEnumConstants()).map(c -> c.name().toLowerCase()).toList()));
    }

    /** A reader-facing name for the type ("whole number", "number", ...). */
    public static String describe(Class<?> type) {
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class
            || type == short.class || type == Short.class || type == byte.class || type == Byte.class) {
            return "whole number";
        }
        if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
            return "number";
        }
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (type == UUID.class) return "UUID";
        return type.getSimpleName();
    }
}
