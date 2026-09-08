package com.osmig.Jweb.framework.js;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ten most-used browser-API modules are reachable from
 * {@code import static jweb.Js.*}. Every public static they declare is
 * forwarded, except the names {@link Modules#MODULE_ONLY} lists — so a module
 * growing a verb without the facade following is a failure here, not a
 * surprise for whoever tries the shorter import.
 */
class JsModuleFacadeTest {

    @Test
    void everyModuleStaticIsReachableFromJs() {
        List<String> missing = new ArrayList<>();
        for (Class<?> module : Modules.FORWARDED) {
            for (Method m : module.getDeclaredMethods()) {
                if (!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers())) continue;
                if (m.isSynthetic() || m.isAnnotationPresent(Deprecated.class)) continue;
                if (Modules.MODULE_ONLY.contains(m.getName())) continue;
                try {
                    jweb.Js.class.getMethod(m.getName(), m.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    missing.add(module.getSimpleName() + "." + m.getName() + "("
                        + Arrays.stream(m.getParameterTypes()).map(Class::getSimpleName)
                            .collect(Collectors.joining(", ")) + ")");
                }
            }
        }
        assertTrue(missing.isEmpty(),
            "not forwarded by jweb.Js (add to Modules, or to Modules.MODULE_ONLY with a reason):\n  "
                + String.join("\n  ", missing));
    }

    @Test
    void theExcludedNamesAreExcludedForARealReason() {
        // Each one either already means something in the Actions layer, is
        // declared by two of the ten modules, or would capture a call meant for
        // the HTML/CSS DSL. The guard is that they resolve to *something*
        // elsewhere: a name excluded for no reason is a name that should be
        // forwarded.
        List<String> stillReachable = new ArrayList<>();
        for (String name : Modules.MODULE_ONLY) {
            boolean declaredTwice = Modules.FORWARDED.stream()
                .filter(c -> Arrays.stream(c.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals(name)
                        && Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
                .count() > 1;
            boolean inChain = Arrays.stream(jweb.Js.class.getMethods())
                .anyMatch(m -> m.getName().equals(name) && Modifier.isStatic(m.getModifiers()));
            boolean inHtmlOrCss = hasStatic(jweb.El.class, name) || hasStatic(jweb.Css.class, name);
            if (!declaredTwice && !inChain && !inHtmlOrCss) stillReachable.add(name);
        }
        assertTrue(stillReachable.isEmpty(),
            "excluded without a collision to justify it: " + stillReachable);
    }

    private static boolean hasStatic(Class<?> type, String name) {
        return Arrays.stream(type.getMethods())
            .anyMatch(m -> m.getName().equals(name) && Modifier.isStatic(m.getModifiers()));
    }
}
