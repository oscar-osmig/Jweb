package com.osmig.Jweb.app.docs;

import jweb.Cls;
import jweb.Element;
import jweb.StateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jweb.El.*;

/**
 * Full-text search over the in-app documentation.
 *
 * <p>The docs are Java, so the index is built from the rendered sections
 * themselves: every section is rendered once, its HTML split at the headings,
 * and the text under each heading (prose and code alike) becomes an entry that
 * links to the section — and to the heading's anchor when it has one. Built
 * lazily on the first query, from the latest documentation version, in a
 * render context of its own so nothing the sections register leaks into the
 * request that triggered the build.</p>
 *
 * <p>{@link #results(String, String)} renders the hits as a fragment for the
 * sidebar box; {@link #search(String, int)} is the ranked list behind it.</p>
 */
public final class DocsSearch {

    private DocsSearch() {}

    /** The list of hits — {@code .docs-search-results-list}. */
    public static final Cls RESULTS_LIST = cls("docs-search-results-list");
    /** One hit, a link — {@code .docs-search-hit}. */
    public static final Cls HIT = cls("docs-search-hit");
    /** The hit's section name — {@code .docs-search-hit-section}. */
    public static final Cls HIT_SECTION = cls("docs-search-hit-section");
    /** The hit's heading — {@code .docs-search-hit-heading}. */
    public static final Cls HIT_HEADING = cls("docs-search-hit-heading");
    /** The hit's excerpt with the terms marked — {@code .docs-search-hit-excerpt}. */
    public static final Cls HIT_EXCERPT = cls("docs-search-hit-excerpt");
    /** The no-match line — {@code .docs-search-empty}. */
    public static final Cls EMPTY = cls("docs-search-empty");

    static final int LIMIT = 8;
    private static final int EXCERPT_BEFORE = 60;
    private static final int EXCERPT_AFTER = 110;

    /** Elements whose content is chrome, not documentation. */
    private static final Pattern CHROME = Pattern.compile(
        "<(script|style|svg|button|summary)\\b[^>]*>.*?</\\1>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern HEADING = Pattern.compile("<h([1-4])\\b([^>]*)>(.*?)</h\\1>", Pattern.DOTALL);
    private static final Pattern ID_ATTR = Pattern.compile("\\bid=\"([^\"]*)\"");
    private static final Pattern BLOCK_END = Pattern.compile("</(p|li|pre|div|tr|td|th|dd|dt|section|article)>|<br\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG = Pattern.compile("<[^>]+>");

    /** One indexed block: a section's text under one heading. */
    public record Entry(String section, String sectionTitle, String heading, String anchor, String text) {}

    /** A ranked match with the excerpt around the first term. */
    public record Hit(Entry entry, int score, String excerpt) {}

    private static volatile List<Entry> index;

    // ==================== querying ====================

    /** The top {@code limit} entries matching every term of {@code query}, best first. */
    public static List<Hit> search(String query, int limit) {
        List<String> terms = terms(query);
        if (terms.isEmpty()) return List.of();
        List<Hit> hits = new ArrayList<>();
        for (Entry entry : index()) {
            String section = entry.sectionTitle().toLowerCase(Locale.ROOT);
            String heading = entry.heading().toLowerCase(Locale.ROOT);
            String text = entry.text().toLowerCase(Locale.ROOT);
            int score = 0;
            boolean all = true;
            for (String term : terms) {
                int s = 0;
                if (section.contains(term)) s += 10;
                if (heading.contains(term)) s += 6;
                s += Math.min(5, count(text, term));
                if (s == 0) { all = false; break; }
                score += s;
            }
            if (!all) continue;
            hits.add(new Hit(entry, score, excerpt(entry.text(), terms)));
        }
        hits.sort((a, b) -> Integer.compare(b.score(), a.score()));
        return hits.size() > limit ? hits.subList(0, limit) : hits;
    }

    /** The search box's fragment: the hits, a no-match line, or nothing for a blank query. */
    public static Element results(String query, String version) {
        if (query == null || query.isBlank()) return fragment();
        List<Hit> hits = search(query, LIMIT);
        if (hits.isEmpty()) {
            return p(EMPTY, "No matches for \u201c" + query.trim() + "\u201d");
        }
        List<String> terms = terms(query);
        String v = DocVersions.normalize(version);
        return div(RESULTS_LIST, each(hits, hit -> {
            Entry e = hit.entry();
            String href = DocVersions.href(e.section(), v) + (e.anchor() == null ? "" : "#" + e.anchor());
            return a(HIT, href(href),
                span(HIT_SECTION, e.sectionTitle()),
                strong(HIT_HEADING, e.heading()),
                p(HIT_EXCERPT, marked(hit.excerpt(), terms)));
        }));
    }

    // ==================== the index ====================

    /** Every indexed entry, built on first use from the latest docs version. */
    public static List<Entry> index() {
        List<Entry> built = index;
        if (built == null) {
            synchronized (DocsSearch.class) {
                built = index;
                if (built == null) {
                    built = build();
                    index = built;
                }
            }
        }
        return built;
    }

    /** Forgets the index so the next query rebuilds it (tests, hot reload). */
    public static void reset() {
        index = null;
    }

    private static List<Entry> build() {
        // Rendering every section registers handlers, styles and action
        // definitions; a throwaway context keeps them out of the request's
        // own (which would otherwise ship them all with the first search).
        StateManager.StateContext caller = StateManager.getContext();
        StateManager.StateContext scratch = StateManager.createContext();
        try {
            String version = DocVersions.latest();
            List<Entry> entries = new ArrayList<>();
            for (Map.Entry<String, String> section : DocSidebar.SECTIONS.entrySet()) {
                String id = section.getKey();
                if (!DocVersions.sectionAvailable(id, version)) continue;
                Element rendered = DocContent.get(id, version);
                if (rendered == null) continue;
                split(id, section.getValue(), rendered.toHtml(), entries);
            }
            return Collections.unmodifiableList(entries);
        } finally {
            scratch.clearContext();
            if (caller != null) StateManager.setContext(caller);
            else StateManager.clearContext();
        }
    }

    /** Splits one section's HTML at its headings; the text under each heading is one entry. */
    static void split(String section, String sectionTitle, String html, List<Entry> out) {
        String body = CHROME.matcher(html).replaceAll(" ");
        Matcher m = HEADING.matcher(body);
        String heading = sectionTitle;
        String anchor = null;
        int from = 0;
        while (m.find()) {
            add(out, section, sectionTitle, heading, anchor, body.substring(from, m.start()));
            heading = plainText(m.group(3));
            Matcher id = ID_ATTR.matcher(m.group(2));
            anchor = id.find() ? id.group(1) : null;
            from = m.end();
        }
        add(out, section, sectionTitle, heading, anchor, body.substring(from));
    }

    private static void add(List<Entry> out, String section, String sectionTitle, String heading, String anchor, String html) {
        String text = plainText(html);
        if (text.isEmpty() && heading.equals(sectionTitle)) return;
        out.add(new Entry(section, sectionTitle, heading.isEmpty() ? sectionTitle : heading, anchor, text));
    }

    /** The text a reader sees: tags gone, block ends as spaces, entities decoded, whitespace folded. */
    static String plainText(String html) {
        String withBreaks = BLOCK_END.matcher(html).replaceAll(" ");
        String noTags = TAG.matcher(withBreaks).replaceAll("");
        return noTags.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
            .replace("&#x27;", "'").replace("&#39;", "'").replace("&nbsp;", " ").replace("&amp;", "&")
            .replaceAll("\\s+", " ").trim();
    }

    // ==================== text helpers ====================

    /** Lower-cased words of the query, punctuation dropped: {@code "Three.patch"} is {@code three}, {@code patch}. */
    /** How much of a query is searched: the rest is ignored, so a pasted page cannot cost a scan per word. */
    static final int MAX_QUERY_CHARS = 200;
    static final int MAX_TERMS = 8;

    static List<String> terms(String query) {
        if (query == null) return List.of();
        if (query.length() > MAX_QUERY_CHARS) query = query.substring(0, MAX_QUERY_CHARS);
        List<String> terms = new ArrayList<>();
        for (String raw : query.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}_]+")) {
            if (!raw.isEmpty() && !terms.contains(raw)) terms.add(raw);
            if (terms.size() == MAX_TERMS) break;
        }
        return terms;
    }

    private static int count(String text, String term) {
        int n = 0, i = 0;
        while ((i = text.indexOf(term, i)) >= 0) {
            n++;
            i += term.length();
        }
        return n;
    }

    /** A window of the text around the first term, trimmed to word boundaries. */
    static String excerpt(String text, List<String> terms) {
        String lower = text.toLowerCase(Locale.ROOT);
        int at = -1;
        for (String term : terms) {
            int i = lower.indexOf(term);
            if (i >= 0 && (at < 0 || i < at)) at = i;
        }
        if (at < 0) at = 0;
        int start = Math.max(0, at - EXCERPT_BEFORE);
        int end = Math.min(text.length(), at + EXCERPT_AFTER);
        if (start > 0) {
            int space = text.indexOf(' ', start);
            if (space >= 0 && space < at) start = space + 1;
        }
        if (end < text.length()) {
            int space = text.lastIndexOf(' ', end);
            if (space > at) end = space;
        }
        String window = text.substring(start, end).trim();
        return (start > 0 ? "\u2026" : "") + window + (end < text.length() ? "\u2026" : "");
    }

    /** The excerpt as text nodes with every term occurrence wrapped in {@code <mark>}. */
    static Element marked(String excerpt, List<String> terms) {
        if (terms.isEmpty()) return fragment(text(excerpt));
        StringBuilder alternation = new StringBuilder();
        for (String term : terms) {
            if (alternation.length() > 0) alternation.append('|');
            alternation.append(Pattern.quote(term));
        }
        Matcher m = Pattern.compile(alternation.toString(), Pattern.CASE_INSENSITIVE).matcher(excerpt);
        List<Element> parts = new ArrayList<>();
        int last = 0;
        while (m.find()) {
            if (m.start() > last) parts.add(text(excerpt.substring(last, m.start())));
            parts.add(mark(excerpt.substring(m.start(), m.end())));
            last = m.end();
        }
        if (last < excerpt.length()) parts.add(text(excerpt.substring(last)));
        return fragment(parts.toArray(new Element[0]));
    }
}
