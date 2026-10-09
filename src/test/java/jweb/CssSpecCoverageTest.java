package jweb;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rule an author relies on: camelCase the CSS name and the setter exists;
 * camelCase the keyword and the constant exists. Nobody should have to reach
 * for {@code prop("white-space", "pre-wrap")} to find out.
 *
 * <p>The two checked-in lists ({@code css-properties.txt},
 * {@code css-keywords.txt}) are the contract. Adding a property to the list
 * without its setter fails this test, which is the point: the list grows with
 * the DSL, never ahead of it.</p>
 */
class CssSpecCoverageTest {

    @Test
    void everyListedPropertyHasATypedSetter() throws IOException {
        Set<String> setters = new HashSet<>();
        for (Method m : Style.class.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers()) && m.getParameterCount() >= 1) {
                setters.add(m.getName());
            }
        }
        List<String> missing = new ArrayList<>();
        for (String property : entries("/jweb/css-properties.txt")) {
            String[] parts = property.split("\\s+");
            String name = parts.length > 1 ? parts[1] : camel(parts[0]);
            if (!setters.contains(name)) missing.add(parts[0] + " -> " + name + "(...)");
        }
        assertTrue(missing.isEmpty(),
            "CSS properties with no typed setter on jweb.Style (add the setter, or drop the "
                + "property from css-properties.txt with a reason):\n  " + String.join("\n  ", missing));
    }

    @Test
    void everyListedKeywordHasAConstant() throws IOException {
        Set<String> constants = new HashSet<>();
        for (Field f : Css.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && CSSValue.class.isAssignableFrom(f.getType())) {
                constants.add(f.getName());
            }
        }
        List<String> missing = new ArrayList<>();
        for (String keyword : entries("/jweb/css-keywords.txt")) {
            String[] parts = keyword.split("\\s+");
            String name = parts.length > 1 ? parts[1] : camel(parts[0]);
            if (!constants.contains(name)) missing.add(parts[0] + " -> " + name);
        }
        assertTrue(missing.isEmpty(),
            "CSS keywords with no constant under jweb.Css (add `public static final CSSValue "
                + "<name> = () -> \"<css>\";` to framework/styles/CSS.java, or drop the keyword "
                + "from css-keywords.txt with a reason):\n  " + String.join("\n  ", missing));
    }

    /** {@code border-top-left-radius} → {@code borderTopLeftRadius}. */
    static String camel(String css) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : css.toCharArray()) {
            if (c == '-') { upper = true; continue; }
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = false;
        }
        return sb.toString();
    }

    private static List<String> entries(String resource) throws IOException {
        try (InputStream in = CssSpecCoverageTest.class.getResourceAsStream(resource)) {
            assertTrue(in != null, "missing test resource " + resource);
            List<String> out = new ArrayList<>();
            for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                out.add(trimmed);
            }
            return out;
        }
    }
}
