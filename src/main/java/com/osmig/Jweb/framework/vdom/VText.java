package com.osmig.Jweb.framework.vdom;

/**
 * Virtual DOM Text Node - plain text content (HTML-escaped).
 */
public record VText(String content) implements VNode {

    public VText {
        if (content == null) {
            content = "";
        }
    }

    @Override
    public String toHtml() {
        return escapeHtml(content);
    }

    @Override
    public VNode copy() {
        return new VText(content);
    }

    private static String escapeHtml(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#x27;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
