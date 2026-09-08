package jweb;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The framework's own app is the proof. Two 3.0 spellings are gone from it —
 * and from the Java samples inside its documentation text blocks, which is
 * where most readers meet the DSL:
 *
 * <ul>
 *   <li>{@code text("…")} — a bare String child already is escaped text.</li>
 *   <li>{@code .done()} / {@code attrs().style()} — a style is its own element
 *       argument, or a {@code style(s -> …)} lambda inside an {@code attrs()}
 *       chain.</li>
 * </ul>
 *
 * <p>Both still compile (the {@code text} wrapper is deprecated, not deleted;
 * {@code transition()…done()} is a different builder that stays), so only a
 * scan can keep them out.</p>
 */
class AppDslHygieneTest {

    /** {@code text("x")} as a call — not {@code todo.text()}, not {@code .getText(}. */
    private static final Pattern TEXT_CALL = Pattern.compile("(?<![.\\w])text\\s*\\(\\s*[\"'+]");

    /**
     * The deleted {@code attrs().style()} starter, and a {@code .done()} that
     * ends a chain — never a record accessor like {@code todo.done()}, which is
     * preceded by an identifier rather than by {@code )} or a line break.
     */
    private static final Pattern STYLE_DONE =
        Pattern.compile("\\.style\\s*\\(\\s*\\)|(?<=\\))\\.done\\(\\)|^\\s*\\.done\\(\\)");

    @Test
    void theAppAndItsSamplesUseBareStringsInsteadOfText() throws IOException {
        List<String> offenders = scan(TEXT_CALL);
        assertTrue(offenders.isEmpty(),
            "a bare String child is escaped text — drop text(...):\n  " + String.join("\n  ", offenders));
    }

    @Test
    void theAppAndItsSamplesHaveNoInlineStyleDone() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (String hit : scan(STYLE_DONE)) {
            // .transition()...done() is the transition builder, which survives 3.0
            if (hit.contains("transition(")) continue;
            offenders.add(hit);
        }
        assertTrue(offenders.isEmpty(),
            "attrs().style() and .done() are gone — pass style() as an argument, or use "
                + "attrs().style(s -> ...):\n  " + String.join("\n  ", offenders));
    }

    private static List<String> scan(Pattern pattern) throws IOException {
        Path app = Path.of("").toAbsolutePath().resolve("src/main/java/com/osmig/Jweb/app");
        assertTrue(Files.isDirectory(app), "expected the demo app at " + app);

        List<String> hits = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(app)) {
            for (Path file : walk.filter(Files::isRegularFile)
                                 .filter(f -> f.toString().endsWith(".java"))
                                 .toList()) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (pattern.matcher(lines.get(i)).find()) {
                        hits.add(app.relativize(file) + ":" + (i + 1) + "  " + lines.get(i).strip());
                    }
                }
            }
        }
        return hits;
    }
}
