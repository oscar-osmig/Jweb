package jweb;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jweb.DocSamplesCompileTest.Report;
import jweb.DocSamplesCompileTest.Sample;

import static jweb.DocSamplesCompileTest.compileAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The same compiler, pointed at the markdown: every <code>```java</code> fenced block in
 * {@code README.md}, {@code JWEB_EXAMPLES.md}, {@code dsl-simplification-3.md}, the guides
 * under {@code readme/}, and the topic files under
 * {@code src/main/java/com/osmig/Jweb/framework/docs/} (they ship inside the jar, so an IDE
 * or an assistant reads them as the API's own documentation).
 *
 * <p>The wrapper, the prelude and the ambient fixture are
 * {@link DocSamplesCompileTest}'s — see that class for what a sample may assume. A block is
 * not compiled when its fence names another language ({@code ```bash}), when the line above
 * it is {@code <!-- nocompile: reason -->}, or when it carries a bare {@code ...} elision.
 */
class ReadmeSamplesCompileTest {

    /** Markdown samples that are deliberately partial. Keep it short. */
    static final Map<String, String> ALLOWED = Map.of();

    @Test
    void everyJavaMarkdownSampleCompiles() throws Exception {
        Path root = Path.of(".");
        List<Sample> samples = new ArrayList<>();
        for (Path f : markdownFiles(root)) samples.addAll(fencedSamplesIn(f));
        Report report = compileAll(samples, "./", ALLOWED);
        System.out.println("[readme samples] " + report.summary());
        if (!report.failures().isEmpty()) fail(report.text());
        // the extractor going quiet would make this test pass by doing nothing
        assertTrue(report.compiled() > 300, "only " + report.compiled() + " samples were compiled — "
            + "the extractor stopped finding them");
    }

    static List<Path> markdownFiles(Path root) throws IOException {
        List<Path> files = new ArrayList<>(List.of(
            root.resolve("README.md"),
            root.resolve("JWEB_EXAMPLES.md"),
            root.resolve("dsl-simplification-3.md")));
        for (Path dir : List.of(root.resolve("readme"),
                                root.resolve("src/main/java/com/osmig/Jweb/framework/docs"))) {
            try (Stream<Path> s = Files.list(dir)) {
                files.addAll(s.filter(p -> p.toString().endsWith(".md")).sorted().toList());
            }
        }
        return files;
    }

    static List<Sample> fencedSamplesIn(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<Sample> out = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).stripTrailing().startsWith("```")) continue;
            String info = lines.get(i).stripTrailing().substring(3).trim().toLowerCase();
            int start = i + 1;
            int end = start;
            while (end < lines.size() && !lines.get(end).stripTrailing().equals("```")) end++;
            if (info.equals("java")) {
                String marker = i > 0 ? lines.get(i - 1).trim() : "";
                String lang = marker.startsWith("<!-- nocompile") ? "nocompile" : "java";
                out.add(new Sample(file, start + 1, lang,
                    String.join("\n", lines.subList(start, Math.min(end, lines.size())))));
            }
            i = end;
        }
        return out;
    }
}
