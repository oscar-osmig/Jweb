package jweb;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * "Every CSS property takes a String as well as a typed value" (3.0 rule 3),
 * checked by reflection over {@link Style} so a new property cannot ship
 * half a pair: a setter that takes one {@code CSSValue} (or {@code CSSValue...})
 * needs a same-named setter that takes one {@code String}, and a setter that
 * takes one {@code String} needs a same-named overload whose parameters are
 * all typed — a {@code CSSValue}, a number, a varargs of either. Deprecated
 * setters are exempt (they are on their way out, not in), and the names in
 * {@link #STRING_ONLY} are exempt from the second check because their only
 * sensible value is free text.
 */
class StylePropertyPairsTest {

    /** Properties whose value is a name, a list of names, or a feature string — nothing typed to pair with. */
    private static final Set<String> STRING_ONLY = Set.of(
        "containerName", "counterIncrement", "counterReset", "counterSet",
        "font", "fontFeatureSettings", "fontVariationSettings", "hyphenateCharacter",
        "quotes", "scrollTimelineName", "viewTimelineName", "timelineScope",
        "viewTransitionName", "viewTransitionClass", "textEmphasisPosition",
        "prop");   // prop("name: value") is the escape hatch, not a property

    @Test
    void everyTypedPropertyAlsoTakesAString() {
        Set<String> missing = new TreeSet<>();
        for (Method m : setters()) {
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 1 && isCssValueOrVarargs(p[0]) && !hasOverload(m.getName(), String.class)) {
                missing.add(m.getName());
            }
        }
        assertTrue(missing.isEmpty(),
            "Style setters with a CSSValue form and no String twin (add name(String)):\n  " + missing);
    }

    @Test
    void everyStringPropertyHasATypedForm() {
        Set<String> missing = new TreeSet<>();
        for (Method m : setters()) {
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 1 && p[0] == String.class
                    && !STRING_ONLY.contains(m.getName())
                    && setters().stream().noneMatch(o -> o.getName().equals(m.getName()) && allTyped(o))) {
                missing.add(m.getName());
            }
        }
        assertTrue(missing.isEmpty(),
            "Style setters with only a String form (add a CSSValue/number overload, or list it in STRING_ONLY with the reason):\n  " + missing);
    }

    @Test
    void theAllowListNamesRealStringOnlySetters() {
        Set<String> stale = new TreeSet<>();
        for (String name : STRING_ONLY) {
            boolean stringForm = hasOverload(name, String.class);
            boolean typedForm = setters().stream().anyMatch(o -> o.getName().equals(name) && allTyped(o));
            if (!stringForm || typedForm) stale.add(name);
        }
        assertTrue(stale.isEmpty(), "STRING_ONLY entries that are not String-only setters any more: " + stale);
    }

    // ==================== helpers ====================

    private static List<Method> SETTERS;

    /** Public, non-static, non-deprecated methods of Style that return the builder ({@code T}). */
    private static List<Method> setters() {
        if (SETTERS == null) {
            List<Method> out = new ArrayList<>();
            for (Method m : Style.class.getDeclaredMethods()) {
                if (!Modifier.isPublic(m.getModifiers()) || Modifier.isStatic(m.getModifiers())) continue;
                if (m.isSynthetic() || m.isBridge() || m.isAnnotationPresent(Deprecated.class)) continue;
                if (!(m.getGenericReturnType() instanceof TypeVariable)) continue;
                out.add(m);
            }
            SETTERS = out;
        }
        return SETTERS;
    }

    private static boolean hasOverload(String name, Class<?> single) {
        return setters().stream().anyMatch(o -> o.getName().equals(name)
            && o.getParameterCount() == 1 && o.getParameterTypes()[0] == single);
    }

    /**
     * A {@code CSSValue} parameter — but not a nested {@code Style}: a Style
     * implements CSSValue too, and the methods that take one ({@code hover},
     * {@code dark}, {@code apply}, {@code startingStyle}…) are variants, not
     * property setters.
     */
    private static boolean isCssValueOrVarargs(Class<?> type) {
        Class<?> t = type.isArray() ? type.getComponentType() : type;
        return CSSValue.class.isAssignableFrom(t) && !Style.class.isAssignableFrom(t);
    }

    private static boolean isTyped(Class<?> type) {
        Class<?> t = type.isArray() ? type.getComponentType() : type;
        return t.isPrimitive() || Number.class.isAssignableFrom(t) || isCssValueOrVarargs(t);
    }

    private static boolean allTyped(Method m) {
        return m.getParameterCount() > 0 && Arrays.stream(m.getParameterTypes()).allMatch(StylePropertyPairsTest::isTyped);
    }
}
