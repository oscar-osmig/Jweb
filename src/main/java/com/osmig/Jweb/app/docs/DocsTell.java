package com.osmig.Jweb.app.docs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Backs {@code GET /docs/tell} — the whole JWeb documentation set as one
 * plain-text markdown document, meant to be pulled in by an AI assistant as
 * grounding before it writes JWeb code.
 *
 * <p>Why a single endpoint rather than the docs site: the site is Java-built
 * {@code Element} trees rendered to HTML, which an assistant has to scrape and
 * guess at. This serves the markdown sources directly, in a deliberate order —
 * orientation first (the DSL rules and the 2.x breaking changes, so a model
 * does not emit 1.x syntax), then the long-form guides, then the per-topic
 * reference.
 *
 * <p>The document is assembled once and cached. Everything it serves is a
 * classpath resource, so it is identical in a dev run and from the jar — see
 * the {@code copy-readme} execution in pom.xml, which is what puts the guides
 * and the migration doc on the classpath in the first place.
 *
 * <p>Three files in {@code readme/} are deliberately NOT served:
 * {@code research.md} is a comparison of other frontend frameworks and would
 * mislead a model into citing React advice as JWeb's; {@code publish.md} is
 * about publishing a library to Maven; {@code design-tooling.md} is about an
 * external design skill. None of them helps write a JWeb app.
 */
public final class DocsTell {

    private DocsTell() {}

    /** One served document. {@code id} is the value accepted by {@code ?topic=}. */
    public record Topic(String id, String title, String resource) {}

    /**
     * Serving order is the reading order for someone starting from nothing:
     * what it is, what changed in 2.x, then the guides, then reference.
     */
    private static final List<Topic> TOPICS = List.of(
        new Topic("overview", "Overview and quick start", "readme/README.md"),
        new Topic("migrating-to-2", "Migrating to 2.x — what changed, and what breaks silently", "readme/dsl-simplification.md"),
        new Topic("why-jweb", "Why JWeb, and the honest trade-offs", "readme/guides/why-jweb.md"),
        new Topic("architecture", "Architecture: rendering pipeline, request flow, routing, middleware", "readme/guides/architecture.md"),
        new Topic("html-dsl", "HTML DSL: elements, attributes, forms, conditionals, builders", "readme/guides/html-dsl.md"),
        new Topic("css-dsl", "CSS DSL: inline styles, rules, units, colors, at-rules, themes", "readme/guides/css-dsl.md"),
        new Topic("javascript-dsl", "JavaScript DSL: actions, handlers, fetch, events, browser APIs", "readme/guides/javascript-dsl.md"),
        new Topic("state-and-realtime", "State, hydration, WebSocket, SSE, transitions, async rendering", "readme/guides/state-and-realtime.md"),
        new Topic("backend", "Backend: REST, OpenAPI, MongoDB, security, validation, uploads, jobs", "readme/guides/backend.md"),
        new Topic("configuration", "Configuration, environment, dev tools, CLI, project structure", "readme/guides/configuration.md"),
        new Topic("known-issues", "Known issues and sharp edges — the API pitfalls", "readme/guides/known-issues.md"),
        new Topic("ref-templates", "Reference: templates", "framework-src/docs/templates.md"),
        new Topic("ref-elements", "Reference: elements", "framework-src/docs/elements.md"),
        new Topic("ref-css", "Reference: css", "framework-src/docs/css.md"),
        new Topic("ref-javascript", "Reference: javascript", "framework-src/docs/javascript.md"),
        new Topic("ref-routing", "Reference: routing", "framework-src/docs/routing.md"),
        new Topic("ref-state", "Reference: state", "framework-src/docs/state.md"),
        new Topic("ref-http", "Reference: http", "framework-src/docs/http.md"),
        new Topic("ref-middleware", "Reference: middleware", "framework-src/docs/middleware.md"),
        new Topic("ref-validation", "Reference: validation", "framework-src/docs/validation.md"),
        new Topic("ref-security", "Reference: security", "framework-src/docs/security.md"),
        new Topic("ref-file-upload", "Reference: file upload", "framework-src/docs/file-upload.md"),
        new Topic("ref-sse", "Reference: server-sent events", "framework-src/docs/sse.md"),
        new Topic("ref-jobs", "Reference: background jobs", "framework-src/docs/jobs.md"),
        new Topic("ref-i18n", "Reference: i18n", "framework-src/docs/i18n.md"),
        new Topic("ref-testing", "Reference: testing", "framework-src/docs/testing.md")
    );

    private static volatile String cachedFull;
    private static volatile Map<String, String> cachedTopics;
    /** Keyed by topic id, with "" for the whole set. */
    private static final Map<String, String> ETAGS = new ConcurrentHashMap<>();

    public static List<Topic> topics() {
        return TOPICS;
    }

    /**
     * Strong validator for a response, e.g. {@code "3f8a1c02d4e5b6a7"}.
     *
     * <p>Derived from the bytes actually served rather than from the version, so
     * editing a guide without cutting a release still invalidates it. Paired with
     * {@code Cache-Control: no-cache} this gives an immediate switch on deploy —
     * the client must revalidate every time — while a 304 keeps an unchanged
     * fetch from re-sending ~300KB.
     *
     * @param topicId null or blank for the whole set
     * @return null when no topic has that id
     */
    public static String etag(String topicId) {
        String key = (topicId == null || topicId.isBlank()) ? "" : topicId;
        String body = key.isEmpty() ? full() : topic(key);
        if (body == null) return null;
        return ETAGS.computeIfAbsent(key, k -> '"' + sha256Prefix(body) + '"');
    }

    private static String sha256Prefix(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the platform; unreachable in practice.
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /**
     * Download filename for a response, e.g. {@code jweb-2.0.0-docs.md} or
     * {@code jweb-2.0.0-css-dsl.md}. Version-stamped so a folder of these stays
     * sortable and it is obvious which release a saved copy describes.
     *
     * @param topicId null or blank for the whole set
     */
    public static String filename(String topicId) {
        String suffix = (topicId == null || topicId.isBlank()) ? "docs" : topicId;
        return "jweb-" + version() + "-" + suffix + ".md";
    }

    /** The current framework version, e.g. {@code 2.0.0}, or {@code unknown}. */
    public static String version() {
        String v = readProperty("META-INF/build-info.properties", "build.version");
        if (v == null) v = readProperty("META-INF/maven/com.osmig/Jweb/pom.properties", "version");
        return v == null ? "unknown" : v;
    }

    /** The whole documentation set: header, index, then every topic in order. */
    public static String full() {
        String c = cachedFull;
        if (c == null) {
            synchronized (DocsTell.class) {
                if (cachedFull == null) cachedFull = buildFull();
                c = cachedFull;
            }
        }
        return c;
    }

    /**
     * One topic, still carrying the header — a model that fetches only
     * {@code ?topic=css-dsl} must not miss the version or the breaking changes.
     *
     * @return null when no topic has that id
     */
    public static String topic(String id) {
        Map<String, String> byId = cachedTopics;
        if (byId == null) {
            synchronized (DocsTell.class) {
                if (cachedTopics == null) cachedTopics = buildTopics();
                byId = cachedTopics;
            }
        }
        return byId.get(id);
    }

    // ==================== assembly ====================

    private static String buildFull() {
        StringBuilder sb = new StringBuilder(400_000);
        sb.append(header()).append('\n');
        sb.append("## What is in this document\n\n");
        for (Topic t : TOPICS) {
            sb.append("- `").append(t.id()).append("` — ").append(t.title()).append('\n');
        }
        sb.append("\nFetch one on its own with `/docs/tell?topic=<id>`.\n");
        for (Topic t : TOPICS) {
            sb.append(section(t));
        }
        sb.append("\n").append(FOOTER);
        return sb.toString();
    }

    private static Map<String, String> buildTopics() {
        Map<String, String> byId = new LinkedHashMap<>();
        for (Topic t : TOPICS) {
            byId.put(t.id(), header() + section(t) + "\n" + FOOTER);
        }
        return byId;
    }

    /**
     * Each document is fenced by a banner naming its id, so a model splitting
     * this back apart has an unambiguous boundary that markdown headings — which
     * appear inside the documents themselves — cannot give it.
     */
    private static String section(Topic t) {
        return "\n\n<!-- ==================== BEGIN " + t.id() + " ==================== -->\n\n"
             + "# " + t.title() + "\n\n"
             + "> Source: `" + t.resource() + "` · topic id: `" + t.id() + "`\n\n"
             + read(t.resource())
             + "\n\n<!-- ==================== END " + t.id() + " ==================== -->\n";
    }

    private static String header() {
        String v = version();
        return """
            # JWeb %s — complete documentation

            JWeb is a Java web framework for building full-stack web applications entirely
            in Java: no HTML templates, no JSP, no frontend toolchain. Pages are Java code
            that returns typed element trees, rendered server-side by Spring Boot.

            This document is the whole documentation set for version %s, concatenated for
            use as a source. If you are writing JWeb code, read the rules and the breaking
            changes below before anything else — they are where generated code usually goes
            wrong.

            ## Install

            Maven — the JitPack repository plus the dependency:

            ```xml
            <repositories>
              <repository><id>jitpack.io</id><url>https://jitpack.io</url></repository>
            </repositories>

            <dependency>
              <groupId>com.github.oscar-osmig</groupId>
              <artifactId>Jweb</artifactId>
              <version>v%s</version>
            </dependency>
            ```

            Gradle:

            ```groovy
            repositories { maven { url 'https://jitpack.io' } }
            dependencies { implementation 'com.github.oscar-osmig:Jweb:v%s' }
            ```

            Requires Java 21+. Spring Boot's web starter arrives transitively. Annotate the
            application class with `@JWebApplication` (not `@SpringBootApplication`).

            ## The six rules the DSL follows

            Everything in the HTML, CSS and JavaScript DSLs is a consequence of one of these:

            1. **A lone String child is text.** `a("Home")` renders `<a>Home</a>`.
            2. **Every element is `name(Object... attributesAndChildren)`.** Attributes and
               children mix in any order, in one call.
            3. **Every CSS property takes a plain String as well as a typed value.**
               `cursor("copy")` and `cursor(pointer)` both work.
            4. **A Java keyword gets a trailing underscore, and nothing else does.**
               `if_`, `return_`.
            5. **A block takes its body inline.** There is no `endFor()`, `endWhile()`,
               `endTry()`, `endSwitch()`.
            6. **Platform names win.** `writeText`, `getRandomValues`, `getItem`,
               `fillStyle`, `pushState(state, url)`.

            ## Do not emit 1.x syntax

            Version 2.0.0 is source-compatible but NOT binary-compatible with 1.x, and three
            changes do not produce a compile error:

            - `textarea("hello")` renders the text `hello`. It used to set `name="hello"`.
              Use `textarea(name("bio"))`.
            - `JSHistory.pushState(state, url)` takes its arguments in platform order. The
              `(String url, Val state)` and 3-argument forms are deleted.
            - 29 CSS animation presets were deleted. They never animated anything, so
              nothing visible changes.

            Also gone: 139 element overloads, and the attribute surface moved to the
            `HtmlAttributes` interface. Full detail is in the `migrating-to-2` topic below.

            ## Imports

            Elements, styles and JS all come from single static imports:

            ```java
            import static jweb.El.*;   // div, h1, p, text, a, form, ...
            import static jweb.Css.*;  // style(), px(), rem(), hex(), ...
            ```

            `div`, `h1`, `p` and `text` must all resolve to `jweb.El`. An IDE will happily
            offer `javax.management.Query.div` or `com.mongodb.client.model.Indexes.text` —
            both compile and neither is JWeb.

            """.formatted(v, v, v, v);
    }

    private static final String FOOTER =
        "\n<!-- End of JWeb documentation. -->\n";

    // ==================== classpath IO ====================

    private static String read(String resource) {
        try (InputStream in = DocsTell.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                // Loud on purpose: a silently-missing guide would ship a document
                // that looks complete and is not. The pom must copy these.
                throw new IllegalStateException(
                    "Documentation resource missing from the classpath: " + resource
                    + " — check the copy-readme execution in pom.xml");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resource, e);
        }
    }

    private static String readProperty(String resource, String key) {
        try (InputStream in = DocsTell.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;
            Properties p = new Properties();
            p.load(in);
            String v = p.getProperty(key);
            return v == null || v.isBlank() ? null : v;
        } catch (IOException e) {
            return null;
        }
    }
}
