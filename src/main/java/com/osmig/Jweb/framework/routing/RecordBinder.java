package com.osmig.Jweb.framework.routing;

import jweb.BindException;
import jweb.Request;
import jweb.api.Length;
import jweb.api.Pattern;
import jweb.api.Range;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Binds a request's query/form parameters to a record, one component per
 * parameter of the same name — what {@code app.action(path, Pref.class, ...)}
 * and {@code req.bind(Pref.class)} run:
 *
 * <pre>{@code
 * enum Theme { LIGHT, DARK }
 * record Pref(Theme theme, @Range(min = 1, max = 5) int level, Optional<String> note) {}
 * }</pre>
 *
 * <ul>
 *   <li>String, the primitives and their boxes, UUID and enums (by name,
 *       case-insensitive) bind from the single value; {@code List<T>} of
 *       those from every value of that name.</li>
 *   <li>{@code Optional<T>} is optional; everything else is required — a
 *       missing or blank value fails, except a primitive {@code boolean},
 *       which reads as {@code false} (an unchecked checkbox sends nothing).</li>
 *   <li>{@link Range}, {@link Length} and {@link Pattern} on a component
 *       validate the bound value.</li>
 * </ul>
 *
 * <p>The first failure throws {@link BindException} with a message naming
 * the component; the framework turns it into a 400.</p>
 */
public final class RecordBinder {

    private static final Map<Class<?>, Constructor<?>> CTORS = new ConcurrentHashMap<>();

    private RecordBinder() {}

    /** Whether {@code type} can be bound — a record whose components are all supported. */
    public static void check(Class<?> type) {
        if (!type.isRecord()) {
            throw new IllegalArgumentException(type.getName() + " is not a record — action parameters bind to records");
        }
        for (RecordComponent component : type.getRecordComponents()) {
            Class<?> raw = rawType(component);
            if (!ParamConvert.supports(raw)) {
                throw new IllegalArgumentException(type.getSimpleName() + "." + component.getName()
                    + " has unsupported type " + component.getGenericType().getTypeName()
                    + " — use String, a number, boolean, UUID, an enum, or Optional/List of one");
            }
        }
    }

    /**
     * Binds the request's parameters to a new {@code type}.
     *
     * @throws BindException when a component cannot be bound
     */
    public static <T> T bind(Class<T> type, Request request) {
        check(type);
        RecordComponent[] components = type.getRecordComponents();
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            args[i] = bindComponent(components[i], request);
        }
        try {
            @SuppressWarnings("unchecked")
            Constructor<T> ctor = (Constructor<T>) CTORS.computeIfAbsent(type, RecordBinder::canonicalConstructor);
            return ctor.newInstance(args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // A compact constructor's own validation: surface its message as a 400
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException iae) {
                throw new BindException(null, iae.getMessage() != null ? iae.getMessage() : "invalid parameters");
            }
            throw new IllegalStateException("Could not construct " + type.getName(), cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not construct " + type.getName(), e);
        }
    }

    private static Constructor<?> canonicalConstructor(Class<?> type) {
        Class<?>[] types = java.util.Arrays.stream(type.getRecordComponents())
            .map(RecordComponent::getType).toArray(Class<?>[]::new);
        try {
            Constructor<?> ctor = type.getDeclaredConstructor(types);
            ctor.setAccessible(true);
            return ctor;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Record without canonical constructor: " + type.getName(), e);
        }
    }

    private static Object bindComponent(RecordComponent component, Request request) {
        String name = component.getName();
        Class<?> declared = component.getType();
        Type generic = component.getGenericType();

        if (declared == Optional.class) {
            Class<?> inner = typeArgument(generic);
            String raw = request.query(name);
            if (raw == null || raw.isBlank()) return Optional.empty();
            return Optional.of(convertAndValidate(component, inner, raw));
        }
        if (declared == List.class) {
            Class<?> inner = typeArgument(generic);
            String[] values = request.queryParams().get(name);
            List<Object> out = new ArrayList<>();
            if (values != null) {
                for (String raw : values) {
                    if (raw == null || raw.isBlank()) continue;
                    out.add(convertAndValidate(component, inner, raw));
                }
            }
            return List.copyOf(out);
        }

        String raw = request.query(name);
        boolean missing = raw == null || (raw.isBlank() && declared != String.class);
        if (missing) {
            if (declared == boolean.class) return Boolean.FALSE;
            throw new BindException(name, name + " is required");
        }
        return convertAndValidate(component, declared, raw);
    }

    private static Object convertAndValidate(RecordComponent component, Class<?> type, String raw) {
        String name = component.getName();
        Object value;
        try {
            value = ParamConvert.convert(raw, type);
        } catch (IllegalArgumentException e) {
            throw new BindException(name, name + " " + e.getMessage());
        }
        validate(component, value);
        return value;
    }

    private static void validate(RecordComponent component, Object value) {
        String name = component.getName();
        Range range = component.getAnnotation(Range.class);
        if (range != null) {
            if (!(value instanceof Number n)) {
                throw new BindException(name, name + " must be a number");
            }
            double d = n.doubleValue();
            if (d < range.min() || d > range.max()) {
                throw new BindException(name, name + " must be between " + fmt(range.min()) + " and " + fmt(range.max()));
            }
        }
        Length length = component.getAnnotation(Length.class);
        if (length != null) {
            int len = String.valueOf(value).length();
            if (len < length.min() || len > length.max()) {
                throw new BindException(name, length.max() == Integer.MAX_VALUE
                    ? name + " must be at least " + length.min() + " characters"
                    : name + " must be between " + length.min() + " and " + length.max() + " characters");
            }
        }
        Pattern pattern = component.getAnnotation(Pattern.class);
        if (pattern != null) {
            if (!java.util.regex.Pattern.matches(pattern.value(), String.valueOf(value))) {
                throw new BindException(name, pattern.message().isEmpty()
                    ? name + " is not in the expected format" : pattern.message());
            }
        }
    }

    private static String fmt(double d) {
        return d == Math.rint(d) && !Double.isInfinite(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    private static Class<?> rawType(RecordComponent component) {
        Class<?> declared = component.getType();
        if (declared == Optional.class || declared == List.class) {
            return typeArgument(component.getGenericType());
        }
        return declared;
    }

    private static Class<?> typeArgument(Type generic) {
        if (generic instanceof ParameterizedType p && p.getActualTypeArguments().length == 1
                && p.getActualTypeArguments()[0] instanceof Class<?> c) {
            return c;
        }
        throw new IllegalArgumentException("Cannot bind " + generic.getTypeName()
            + " — declare the element type, e.g. Optional<String> or List<Integer>");
    }
}
