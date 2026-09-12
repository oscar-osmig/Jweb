package jweb;

import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Every Java sample in the in-app docs is compiled.
 *
 * <p>The docs hold their samples as opaque text — {@code codeBlock("""…""")} in
 * {@code app/docs/**} and {@code String} constants in {@code DocExamples} — so nothing
 * stopped a DSL change from leaving them wrong. This test reads those sources as text
 * (never as classes: the docs only exist under {@code -Pdemo}), lifts every sample out,
 * and hands it to {@code javax.tools.JavaCompiler} inside a synthetic wrapper.
 *
 * <p><b>What a sample gets.</b> Its own {@code import} lines are hoisted to the top,
 * under the standard prelude (the four DSL wildcards, which
 * {@code com.osmig.Jweb.framework.dsl.DslPass3Test} proves coexist). The body is then
 * split into the largest self-standing units and each is placed where it belongs: a type
 * or method declaration in the class body, a statement in a method body, and anything
 * else — the bare {@code div(…)} an element sample is — as an array element, which
 * type-checks an expression without demanding a statement. Several wrappings are tried;
 * the first that compiles wins.
 *
 * <p><b>What a sample may assume.</b> Two names, declared in {@link #FIXTURE}: the
 * {@code app} a routing snippet configures and the {@code req} a handler serves. Every
 * other name a sample uses, it declares.
 *
 * <p><b>What is not compiled.</b> A block tagged with a language
 * ({@code codeBlock("bash", …)}, or {@code // lang: bash} above a constant) is that
 * language, not Java. A block gated behind {@code before("v3.0.1", …)} is the old syntax
 * on purpose. A block with a bare {@code ...} or {@code …} elision cannot be Java by
 * construction. {@code codeBlock("style", …)} is the one middle case: a chain of
 * {@link jweb.Style} methods whose receiver the sample elides, compiled with
 * {@code style()} in front, so every property name in those catalogues is still checked.
 */
class DocSamplesCompileTest {

    // ==================== what the docs may assume ====================

    /** The two things a docs snippet is written "inside"; anything else it declares. */
    static final String FIXTURE = "JWeb app; Request req;\n";

    static final String PRELUDE = """
        import jweb.*;
        import jweb.css.*;
        import jweb.three.*;
        import jweb.js.*;
        // jweb.State is the hooks facade, so the State<T> type useState() returns needs its
        // own single-type import to win the simple name back from `import jweb.*`.
        import jweb.state.State;
        import jweb.api.*;
        import static jweb.El.*;
        import static jweb.Css.*;
        import static jweb.Js.*;
        import static jweb.Three.*;
        import static jweb.State.*;
        import java.util.*;
        import java.util.function.*;
        import java.util.stream.*;
        import java.time.*;
        // Types a sample cannot name without a com.osmig import, because they have no
        // jweb.* spelling yet — the 2026-09-07 rule ("every type an author can name has a
        // jweb.* spelling that IS the type") has not reached them. Delete a line the day
        // its type moves; the samples need no change.
        import com.osmig.Jweb.framework.util.Json;
        """;

    /**
     * Samples that cannot compile for a reason outside the sample, by {@code file:line}.
     * Keep it short, and keep every entry a live to-do: the fix for a broken sample is the
     * sample, never an entry here. Every entry below goes away with a framework change —
     * when one lands, delete the entry and let this test prove the samples are right.
     */
    static final String JPA_NOT_ON_THE_CLASSPATH =
        "names jakarta.persistence and Spring Data JPA; spring-boot-starter-data-jpa is "
        + "commented out in pom.xml, so the annotations do not exist to compile against";

    static final Map<String, String> ALLOWED = Map.ofEntries(
        Map.entry("DocExamples.java:1445", JPA_NOT_ON_THE_CLASSPATH),
        Map.entry("DocExamples.java:1462", JPA_NOT_ON_THE_CLASSPATH));

    // ==================== the test ====================

    @Test
    void everyJavaDocSampleCompiles() throws Exception {
        Path docs = Path.of("src/main/java/com/osmig/Jweb/app/docs");
        List<Sample> samples = new ArrayList<>();
        try (Stream<Path> files = Files.walk(docs)) {
            for (Path f : files.filter(p -> p.toString().endsWith(".java")).sorted().toList()) {
                samples.addAll(samplesIn(f));
            }
        }
        Report report = compileAll(samples, docs.toString() + "/", ALLOWED);
        System.out.println("[doc samples] " + report.summary());
        if (!report.failures().isEmpty()) fail(report.text());
        // the extractor going quiet would make this test pass by doing nothing
        assertTrue(report.compiled() > 300, "only " + report.compiled() + " samples were compiled — "
            + "the extractor stopped finding them");
    }

    // ==================== extraction ====================

    record Sample(Path file, int line, String lang, String code) {}

    /** {@code codeBlock("""…""")}, with an optional language argument. */
    static final Pattern CODE_BLOCK_CALL =
        Pattern.compile("(?s).*\\bcodeBlock\\(\\s*(?:\"([^\"\\n]*)\"\\s*,\\s*)?$");
    /** {@code public static final String NAME = """…""";} — how DocExamples holds its samples. */
    static final Pattern CONST_SAMPLE =
        Pattern.compile("(?s).*\\bString\\s+[A-Z][A-Z0-9_]*\\s*=\\s*$");
    static final Pattern LANG_MARKER =
        Pattern.compile(
            // the marker must sit on the line directly above the declaration, or it would
            // be picked up by the next constant down the file
            "(?s).*//\\s*lang:\\s*(\\w+)[^\\n]*\\n[^\\n]*String\\s+[A-Z][A-Z0-9_]*\\s*=\\s*$");
    /** {@code before("v3.0.1", …)} — content kept for readers of an older release. */
    static final Pattern BEFORE_CALL = Pattern.compile("(?<![.\\w])before\\s*\\(\\s*\"");
    /** A bare {@code ...} or {@code …}: the sample is a fragment on purpose. */
    static final Pattern ELISION = Pattern.compile("(?<![\\w>\\]])\\.\\.\\.|\\u2026");

    static List<Sample> samplesIn(Path file) throws IOException {
        String src = Files.readString(file);
        Masked m = mask(src);
        List<int[]> gated = new ArrayList<>();
        Matcher bm = BEFORE_CALL.matcher(m.masked());
        while (bm.find()) {
            int open = m.masked().indexOf('(', bm.start());
            int close = matchParen(m.masked(), open);
            if (close > 0) gated.add(new int[]{open, close});
        }
        List<Sample> out = new ArrayList<>();
        for (int[] b : m.textBlocks()) {
            int open = b[0];
            int from = Math.max(0, open - 400);
            String prefix = m.masked().substring(from, open);
            Matcher call = CODE_BLOCK_CALL.matcher(prefix);
            Matcher constant = CONST_SAMPLE.matcher(prefix);
            if (!call.matches() && !constant.matches()) continue;
            String lang = "java";
            if (call.matches() && call.group(1) != null) {
                int at = from + call.start(1);
                lang = src.substring(at, at + call.group(1).length());
            } else if (constant.matches()) {
                Matcher marker = LANG_MARKER.matcher(src.substring(from, open));
                if (marker.find()) lang = marker.group(1);
            }
            if (gated.stream().anyMatch(r -> open > r[0] && open < r[1])) lang = "before";
            out.add(new Sample(file, lineOf(src, open), lang,
                textBlockValue(src.substring(b[1], b[2]))));
        }
        return out;
    }

    // ==================== compiling ====================

    record Failure(String where, String error, String firstLine) {}

    record Report(int found, int compiled, Map<String, Integer> skipped, List<Failure> failures) {
        String summary() {
            int skip = skipped.values().stream().mapToInt(Integer::intValue).sum();
            return "found=" + found + " compiled=" + compiled + " skipped=" + skip
                + " " + skipped + " failed=" + failures.size();
        }

        String text() {
            StringBuilder sb = new StringBuilder(failures.size() + " doc sample(s) do not compile.\n");
            sb.append(summary()).append("\n\n");
            for (Failure f : failures) {
                sb.append(f.where()).append('\n')
                  .append("    ").append(f.error()).append('\n')
                  .append("    sample starts: ").append(f.firstLine()).append("\n\n");
            }
            return sb.toString();
        }
    }

    static Report compileAll(List<Sample> samples, String stripPrefix, Map<String, String> allowed) {
        String classpath = classpath();
        Map<String, Integer> skipped = new TreeMap<>();
        List<Failure> failures = new ArrayList<>();
        Set<String> stale = new TreeSet<>(allowed.keySet());
        int compiled = 0, id = 0;
        for (Sample s : samples) {
            String where = s.file().toString().replace(stripPrefix, "") + ":" + s.line();
            boolean java = s.lang().equals("java") || s.lang().equals("style");
            if (java && ELISION.matcher(mask(s.code()).masked()).find()) {
                skipped.merge("elided", 1, Integer::sum);
                continue;
            }
            if (!java) {
                skipped.merge(s.lang(), 1, Integer::sum);
                continue;
            }
            List<String> wrappers = s.lang().equals("style")
                ? styleWrappers(s.code(), id++) : wrappers(s.code(), id++);
            String error = compile("Sample" + (id - 1), wrappers, classpath);
            String firstLine = s.code().lines().filter(l -> !l.isBlank()).findFirst().orElse("");
            if (allowed.containsKey(where)) {
                // an entry that has started compiling is a to-do that is done
                stale.remove(where);
                if (error == null) failures.add(new Failure(where,
                    "this sample compiles now — delete its ALLOWED entry (" + allowed.get(where) + ")",
                    firstLine));
                else skipped.merge("allow-listed", 1, Integer::sum);
                continue;
            }
            if (error == null) compiled++;
            else failures.add(new Failure(where, error, firstLine));
        }
        for (String key : stale) {
            failures.add(new Failure(key, "ALLOWED names no sample at this line — a line number "
                + "moved, or the sample is gone; fix or delete the entry", allowed.get(key)));
        }
        return new Report(samples.size(), compiled, skipped, failures);
    }

    // ==================== the wrappers ====================

    static final Pattern IMPORT_LINE = Pattern.compile("^\\s*import\\s+[^;]+;\\s*(//.*)?$");
    static final Pattern PACKAGE_LINE = Pattern.compile("^\\s*package\\s+[^;]+;\\s*(//.*)?$");

    static final String ANN = "(?:@[\\w.]+(?:\\([^)]*\\))?\\s*)*";
    static final String MODS = "(?:public|private|protected|static|final|abstract|sealed"
        + "|non-sealed|synchronized|default|transient|volatile)\\s+";
    static final String NOT_KEYWORD = "(?!(?:return|if|for|while|switch|do|try|catch|else|case"
        + "|new|throw|assert|break|continue|yield|super|this)\\b)";

    /** A type declaration: belongs in the class body. */
    static final Pattern MEMBERISH = Pattern.compile(
        "(?s)^" + ANN + "(?:" + MODS + ")*(?:class|interface|record|enum|@interface)\\s+\\w.*");
    /** A method declaration, with or without modifiers: belongs in the class body. */
    static final Pattern METHODISH = Pattern.compile(
        "(?s)^" + ANN + "(?:" + MODS + ")*" + NOT_KEYWORD
        + "[\\w.<>\\[\\],?]+(?:\\s*<[^>]*>)?\\s+\\w+\\s*\\([^;{]*\\)\\s*(?:throws [\\w.,\\s]+)?\\{.*");
    /** A field declaration — {@code static}/{@code public} and friends are illegal on a local. */
    static final Pattern FIELDISH = Pattern.compile(
        "(?s)^" + ANN + "(?:public|private|protected|static)\\s+.*;\\s*");

    static List<String> wrappers(String code, int id) {
        List<String> imports = new ArrayList<>();
        StringBuilder body = new StringBuilder();
        for (String l : code.split("\n", -1)) {
            if (IMPORT_LINE.matcher(l).matches()) imports.add(l.strip().replaceAll("//.*$", "").strip());
            else if (PACKAGE_LINE.matcher(l).matches()) { /* a sample's package is not ours */ }
            else body.append(l).append('\n');
        }
        String head = PRELUDE + String.join("\n", imports) + "\n";
        String name = "Sample" + id;
        List<String> units = units(body.toString());

        List<String> sources = new ArrayList<>();
        sources.add(split(head, name, units, false, ""));
        sources.add(split(head, name, units, true, ""));
        // an @Override fragment of a page: abstract, so render() need not be present
        sources.add(split(head, name, units, false, " implements Template"));
        // whole block as class members
        sources.add(head + "public class " + name + " {\n" + FIXTURE + body + "\n}\n");
        // whole block as a method body, verbatim
        sources.add(head + "public class " + name + " {\n" + FIXTURE
            + "@SuppressWarnings(\"all\") void __run() throws Exception {\n" + body + "}\n}\n");
        // whole block as one argument list
        sources.add(head + "public class " + name + " {\n" + FIXTURE
            + "@SuppressWarnings(\"all\") void __run() throws Exception {\nObject[] __a = {\n"
            + body + "};\n}\n}\n");
        return List.copyOf(new LinkedHashSet<>(sources));
    }

    /** Declarations to the class body, statements to a method body, anything else an array element. */
    static String split(String head, String name, List<String> units, boolean dropOverride, String impl) {
        StringBuilder members = new StringBuilder(), stmts = new StringBuilder();
        int e = 0;
        for (String u : units) {
            if (isMember(u)) {
                members.append(dropOverride ? u.replaceAll("(?m)^(\\s*)@Override\\s*$", "$1") : u).append('\n');
            } else if (isStatement(u)) {
                stmts.append(u).append('\n');
            } else {
                stmts.append("Object[] __a").append(e++).append(" = {\n").append(u).append("};\n");
            }
        }
        return head + (impl.isEmpty() ? "public class " : "public abstract class ") + name + impl
            + " {\n" + FIXTURE + members
            + "@SuppressWarnings(\"all\") void __run() throws Exception {\n" + stmts + "}\n}\n";
    }

    /** A "style" block: every chain gets the receiver the sample elides. */
    static List<String> styleWrappers(String code, int id) {
        StringBuilder b = new StringBuilder();
        int e = 0;
        for (String u : units(code)) {
            if (uncommented(u).stripLeading().startsWith(".")) {
                b.append("Object __s").append(e++).append(" = style()\n")
                 .append(u.stripTrailing()).append("\n;\n");
            } else if (isStatement(u)) {
                b.append(u).append('\n');
            } else {
                b.append("Object[] __a").append(e++).append(" = {\n").append(u).append("};\n");
            }
        }
        return List.of(PRELUDE + "public class Sample" + id + " {\n" + FIXTURE
            + "@SuppressWarnings(\"all\") void __run() throws Exception {\n" + b + "}\n}\n");
    }

    // ==================== splitting a block into units ====================

    /** Runs on to the next line, so the unit is not finished. */
    static final Pattern RUNS_ON = Pattern.compile("(?:[+\\-*/%&|^<>=?:.,]|->)$");
    /** Opens with a token that continues the unit above. */
    static final Pattern CONTINUES = Pattern.compile(
        "[.+\\-*/%&|^<>=?:,)\\]}]|->|else\\b|catch\\b|finally\\b|while\\s*\\(");

    /**
     * The largest self-standing pieces of a block: a unit ends at a line where every
     * bracket has closed, the text does not run on, and the next line does not continue
     * it. Comments ride along with the unit they introduce, and so do annotations.
     */
    static List<String> units(String code) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        boolean inTextBlock = false, pending = false;
        for (String l : code.split("\n", -1)) {
            String m = maskLine(l);
            if (pending) {
                if (!inTextBlock && CONTINUES.matcher(m.stripLeading()).lookingAt()) pending = false;
                else { flush(out, cur); pending = false; depth = 0; }
            }
            if (!inTextBlock && l.isBlank() && depth <= 0) { flush(out, cur); continue; }
            cur.append(l).append('\n');
            if (m.contains("\"\"\"")) inTextBlock = !inTextBlock;
            if (inTextBlock) continue;
            for (char c : m.toCharArray()) {
                if (c == '(' || c == '[' || c == '{') depth++;
                else if (c == ')' || c == ']' || c == '}') depth--;
            }
            if (depth > 0 || isOnlyComments(cur.toString()) || isOnlyAnnotations(cur.toString())) continue;
            String t = m.stripTrailing();
            if (t.isEmpty() || RUNS_ON.matcher(t).find()) continue;
            pending = true;
        }
        flush(out, cur);
        return out;
    }

    static void flush(List<String> out, StringBuilder cur) {
        if (!cur.toString().isBlank()) out.add(cur.toString());
        cur.setLength(0);
    }

    /** Strips literal content and line comments so brackets can be counted. */
    static String maskLine(String l) {
        String s = l.replaceAll("\"([^\"\\\\]|\\\\.)*\"", "\"\"").replaceAll("'([^'\\\\]|\\\\.)*'", "''");
        int c = s.indexOf("//");
        return c >= 0 ? s.substring(0, c) : s;
    }

    static boolean isOnlyComments(String s) {
        return s.lines().map(String::strip)
            .allMatch(l -> l.isEmpty() || l.startsWith("//") || l.startsWith("*") || l.startsWith("/*"));
    }

    /** A run of annotations introduces the member below it; it is never a unit of its own. */
    static boolean isOnlyAnnotations(String s) {
        boolean any = false;
        for (String l : s.lines().map(String::strip).toList()) {
            if (l.isEmpty() || l.startsWith("//")) continue;
            if (!l.startsWith("@")) return false;
            any = true;
        }
        return any;
    }

    /** Classification looks past the comments that introduce a unit. */
    static String uncommented(String unit) {
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        for (String l : unit.split("\n", -1)) {
            String t = l.strip();
            if (!started && (t.isEmpty() || t.startsWith("//"))) continue;
            started = true;
            sb.append(l).append('\n');
        }
        return sb.toString();
    }

    static boolean isMember(String unit) {
        String u = uncommented(unit);
        return MEMBERISH.matcher(u).matches() || METHODISH.matcher(u).matches()
            || FIELDISH.matcher(u).matches();
    }

    /** true when a unit's last meaningful line ends a statement or declaration. */
    static boolean isStatement(String unit) {
        String[] lines = unit.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String l = maskLine(lines[i]).strip();
            if (l.isEmpty()) continue;
            return l.endsWith(";") || l.endsWith("}") || l.endsWith("*/");
        }
        return true;
    }

    // ==================== javac ====================

    static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

    /** A parse failure says more about the wrapping than about the sample. */
    static final Pattern SYNTAX = Pattern.compile(
        "expected|illegal start|illegal character|not a statement|unclosed|reached end of file"
        + "|class, interface|modifier |premature|not allowed here|invalid method declaration"
        + "|return type required|orphaned|misplaced|<identifier>");

    /** Compiles each wrapping in turn; null when one succeeds, else the clearest diagnostic. */
    static String compile(String name, List<String> sources, String classpath) {
        String best = null;
        int bestScore = Integer.MAX_VALUE;
        for (String source : sources) {
            DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<>();
            StandardJavaFileManager std = COMPILER.getStandardFileManager(diags, null, null);
            JavaFileManager fm = new ForwardingJavaFileManager<>(std) {
                @Override public JavaFileObject getJavaFileForOutput(
                        Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
                    return new Discarded(className);
                }
            };
            boolean ok = COMPILER.getTask(new StringWriter(), fm, diags,
                List.of("-classpath", classpath, "-proc:none", "-nowarn", "-Xlint:none"),
                null, List.of(new InMemory(name, source))).call();
            try { fm.close(); } catch (IOException ignored) { }
            if (ok) return null;
            List<Diagnostic<? extends JavaFileObject>> errors = diags.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR).toList();
            String message = errors.isEmpty() ? "compilation failed with no error diagnostic"
                : "line " + errors.get(0).getLineNumber() + ": "
                  + errors.get(0).getMessage(Locale.ENGLISH).replace('\n', ' ');
            int score = (errors.isEmpty() || SYNTAX.matcher(message).find() ? 10_000 : 0) + errors.size();
            if (score < bestScore) { bestScore = score; best = message; }
        }
        return best;
    }

    static final class InMemory extends SimpleJavaFileObject {
        private final String code;
        InMemory(String name, String code) {
            super(URI.create("string:///" + name + ".java"), Kind.SOURCE);
            this.code = code;
        }
        @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return code; }
    }

    static final class Discarded extends SimpleJavaFileObject {
        Discarded(String name) { super(URI.create("mem:///" + name + ".class"), Kind.CLASS); }
        @Override public OutputStream openOutputStream() { return OutputStream.nullOutputStream(); }
    }

    /**
     * The test's own classpath. Surefire hands the JVM a manifest-only jar, so the entries
     * are behind its {@code Class-Path} attribute rather than on {@code java.class.path}.
     */
    static String classpath() {
        List<String> entries = new ArrayList<>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            entries.add(entry);
            if (!entry.endsWith(".jar")) continue;
            try (JarFile jar = new JarFile(entry)) {
                Manifest manifest = jar.getManifest();
                if (manifest == null) continue;
                String cp = manifest.getMainAttributes().getValue("Class-Path");
                if (cp == null) continue;
                Path base = Path.of(entry).toAbsolutePath().getParent();
                for (String ref : cp.split("\\s+")) {
                    if (ref.isBlank()) continue;
                    try {
                        entries.add(Path.of(URI.create(base.toUri() + "/").resolve(ref)).toString());
                    } catch (RuntimeException ignored) {
                        entries.add(base.resolve(ref).toString());
                    }
                }
            } catch (IOException ignored) { }
        }
        entries.add(Path.of("target/classes").toAbsolutePath().toString());
        entries.add(Path.of("target/test-classes").toAbsolutePath().toString());
        return String.join(File.pathSeparator, entries);
    }

    // ==================== reading Java text blocks out of Java source ====================

    record Masked(String masked, List<int[]> textBlocks) {}

    /**
     * Blanks out comment and literal <em>content</em> so the surrounding source can be
     * searched with plain regexes, and records every text block as
     * {@code {openingDelimiter, contentStart, contentEnd}}.
     */
    static Masked mask(String s) {
        char[] out = s.toCharArray();
        List<int[]> blocks = new ArrayList<>();
        int n = s.length();
        int i = 0;
        while (i < n) {
            char c = s.charAt(i);
            if (c == '/' && i + 1 < n && s.charAt(i + 1) == '/') {
                int j = s.indexOf('\n', i);
                if (j < 0) j = n;
                for (int k = i; k < j; k++) out[k] = ' ';
                i = j;
            } else if (c == '/' && i + 1 < n && s.charAt(i + 1) == '*') {
                int j = s.indexOf("*/", i + 2);
                j = (j < 0) ? n : j + 2;
                for (int k = i; k < j; k++) if (out[k] != '\n') out[k] = ' ';
                i = j;
            } else if (c == '\'') {
                int j = i + 1;
                while (j < n) {
                    char d = s.charAt(j);
                    if (d == '\\') { j += 2; continue; }
                    if (d == '\'' || d == '\n') { j++; break; }
                    j++;
                }
                for (int k = i + 1; k < Math.min(j, n) - 1; k++) out[k] = ' ';
                i = j;
            } else if (c == '"') {
                if (s.startsWith("\"\"\"", i)) {
                    int nl = s.indexOf('\n', i + 3);
                    int contentStart = (nl < 0) ? n : nl + 1;
                    int j = contentStart;
                    while (j < n) {
                        if (s.charAt(j) == '\\') { j += 2; continue; }
                        if (s.startsWith("\"\"\"", j)) break;
                        j++;
                    }
                    int contentEnd = Math.min(j, n);
                    blocks.add(new int[]{i, contentStart, contentEnd});
                    for (int k = i; k < Math.min(contentEnd + 3, n); k++) if (out[k] != '\n') out[k] = ' ';
                    i = contentEnd + 3;
                } else {
                    int j = i + 1;
                    while (j < n) {
                        char d = s.charAt(j);
                        if (d == '\\') { j += 2; continue; }
                        if (d == '"') { j++; break; }
                        if (d == '\n') break;
                        j++;
                    }
                    for (int k = i + 1; k < Math.min(j, n) - 1; k++) out[k] = ' ';
                    i = j;
                }
            } else {
                i++;
            }
        }
        return new Masked(new String(out), blocks);
    }

    /** JLS 3.10.6: incidental indentation off, then escapes. */
    static String textBlockValue(String raw) {
        List<String> lines = new ArrayList<>(Arrays.asList(raw.split("\n", -1)));
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            if (l.isBlank() && i != lines.size() - 1) continue;
            int indent = 0;
            while (indent < l.length() && Character.isWhitespace(l.charAt(indent))) indent++;
            min = Math.min(min, indent);
        }
        if (min == Integer.MAX_VALUE) min = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            sb.append((l.length() >= min ? l.substring(min) : l.strip()).replaceAll("\\s+$", ""));
            if (i < lines.size() - 1) sb.append('\n');
        }
        return unescape(sb.toString());
    }

    static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i == s.length() - 1) { sb.append(c); continue; }
            char d = s.charAt(++i);
            switch (d) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 's' -> sb.append(' ');
                case '0' -> sb.append('\0');
                case '"' -> sb.append('"');
                case '\'' -> sb.append('\'');
                case '\\' -> sb.append('\\');
                case '\n' -> { }                                  // line continuation
                case 'u' -> {
                    if (i + 4 < s.length()) {
                        sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                        i += 4;
                    } else sb.append("\\u");
                }
                default -> sb.append('\\').append(d);
            }
        }
        return sb.toString();
    }

    static int matchParen(String s, int open) {
        int depth = 0;
        for (int i = open; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') depth++;
            else if (c == ')' && --depth == 0) return i;
        }
        return -1;
    }

    static int lineOf(String s, int index) {
        int line = 1;
        for (int i = 0; i < index && i < s.length(); i++) if (s.charAt(i) == '\n') line++;
        return line;
    }
}
