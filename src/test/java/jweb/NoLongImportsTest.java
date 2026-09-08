package jweb;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Everything an app author reads — the demo app (real code AND the Java samples
 * inside its text blocks), the docs, and the resources — spells framework types
 * as {@code jweb.*}. A {@code com.osmig.Jweb.framework.} anywhere in those
 * places is a regression: either a type still has no short name, or a sample
 * was written against the long one.
 *
 * <p>Framework-internal tests under {@code src/test/java/com/osmig/**} are out
 * of scope — they deliberately exercise the deprecated aliases.</p>
 */
class NoLongImportsTest {

    private static final String LONG = "com.osmig.Jweb.framework.";

    /**
     * Lines that may legitimately carry the long name. Keyed by path (relative
     * to the project root), each entry is a substring that must appear on the
     * line, with the reason it is allowed.
     */
    private static final Map<String, List<Allowed>> ALLOWED = Map.of(
        "src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports", List.of(
            new Allowed("com.osmig.Jweb.framework.JWebAutoConfiguration",
                "Spring's auto-configuration registry needs the real class name, not an alias")),
        "readme/configuration.md", List.of(
            new Allowed("com.osmig.Jweb.framework.cli.JWebCli",
                "a JVM main-class invocation on a shell command line, not a Java import")),
        "readme/why-jweb.md", List.of(
            new Allowed("com.osmig.Jweb.framework.cli.JWebCli",
                "a JVM main-class invocation on a shell command line, not a Java import")),
        "readme/known-issues.md", List.of(
            new Allowed("`com.osmig.Jweb.framework.*`", "prose naming the legacy namespace in a migration note")),
        "README.md", List.of(
            new Allowed("`com.osmig.Jweb.framework.*`", "prose naming the legacy namespace in a migration note")),
        "JWEB_EXAMPLES.md", List.of(
            new Allowed("`com.osmig.Jweb.framework.*`", "prose naming the legacy namespace in a migration note")),
        "dsl-simplification-3.md", List.of(
            new Allowed("com.osmig.Jweb.framework.", "the migration guide's before→after table lists the long names by definition"))
    );

    record Allowed(String lineContains, String reason) {}

    @Test
    void appDocsAndResourcesUseShortNamesOnly() throws IOException {
        Path root = Path.of("").toAbsolutePath();
        List<Path> files = new ArrayList<>();
        collect(root.resolve("src/main/java/com/osmig/Jweb/app"), "glob:**/*.java", files);
        collect(root.resolve("src/main/resources"), "glob:**/*", files);
        collect(root.resolve("readme"), "glob:**/*.md", files);
        for (String top : List.of("README.md", "dsl-simplification.md", "dsl-simplification-3.md", "JWEB_EXAMPLES.md")) {
            Path p = root.resolve(top);
            if (Files.isRegularFile(p)) files.add(p);
        }
        assertTrue(files.size() > 50, "expected to scan the app, docs and resources; found " + files.size());

        List<String> violations = new ArrayList<>();
        for (Path file : files) {
            String rel = root.relativize(file).toString().replace('\\', '/');
            List<Allowed> allowed = ALLOWED.getOrDefault(rel, List.of());
            List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.contains(LONG)) continue;
                if (allowed.stream().anyMatch(a -> line.contains(a.lineContains()))) continue;
                violations.add(rel + ":" + (i + 1) + "  " + line.strip());
            }
        }
        assertTrue(violations.isEmpty(),
            "long framework names in author-facing places (use the jweb.* spelling):\n  "
                + String.join("\n  ", violations));
    }

    private static void collect(Path dir, String glob, List<Path> into) throws IOException {
        if (!Files.isDirectory(dir)) return;
        PathMatcher matcher = dir.getFileSystem().getPathMatcher(glob);
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.filter(Files::isRegularFile).filter(matcher::matches).forEach(into::add);
        }
    }
}
