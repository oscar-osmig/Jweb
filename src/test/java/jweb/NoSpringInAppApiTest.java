package jweb;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The demo app's API layer and its routes are written against {@code jweb}
 * only: no {@code org.springframework} and no {@code jakarta.servlet}
 * import in {@code app/api/**} or {@code app/Routes.java}. Components,
 * config values, bodies, params, uploads and statuses all have a
 * {@code jweb} spelling — a Spring import there means one is missing.
 */
class NoSpringInAppApiTest {

    @Test
    void appApiAndRoutesImportNoSpring() throws IOException {
        Path root = Path.of("").toAbsolutePath();
        List<Path> files = new ArrayList<>();
        Path api = root.resolve("src/main/java/com/osmig/Jweb/app/api");
        try (Stream<Path> walk = Files.walk(api)) {
            walk.filter(p -> p.toString().endsWith(".java")).forEach(files::add);
        }
        files.add(root.resolve("src/main/java/com/osmig/Jweb/app/Routes.java"));
        assertTrue(files.size() >= 5, "expected the api package and Routes.java; found " + files.size());

        List<String> violations = new ArrayList<>();
        for (Path file : files) {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).strip();
                if (!line.startsWith("import ")) continue;
                if (line.contains("org.springframework") || line.contains("jakarta.servlet")) {
                    violations.add(root.relativize(file) + ":" + (i + 1) + "  " + line);
                }
            }
        }
        assertTrue(violations.isEmpty(),
            "Spring leaks into author code (use the jweb.* spelling):\n  " + String.join("\n  ", violations));
    }
}
