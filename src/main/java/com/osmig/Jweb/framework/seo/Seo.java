package com.osmig.Jweb.framework.seo;

import com.osmig.Jweb.framework.core.Element;

import java.util.ArrayList;
import java.util.List;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * One builder for the whole SEO/social head block — title, description,
 * canonical URL, Open Graph, and Twitter cards, all consistent with each
 * other from a single declaration.
 *
 * <pre>
 * head(
 *     metaCharset(), metaViewport(),
 *     Seo.of("JWeb — Java Web Framework", "Build complete web apps entirely in Java")
 *         .url("https://jweb.dev/")
 *         .image("https://jweb.dev/og.png")
 *         .siteName("JWeb")
 *         .render(),
 *     ...
 * )
 * </pre>
 */
public final class Seo {

    private final String title;
    private final String description;
    private String url;
    private String image;
    private String siteName;
    private String type = "website";

    private Seo(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /** Starts an SEO block with the page title and description. */
    public static Seo of(String title, String description) {
        return new Seo(title, description);
    }

    /** The canonical URL of this page. */
    public Seo url(String url) {
        this.url = url;
        return this;
    }

    /** The social-preview image (absolute URL). */
    public Seo image(String imageUrl) {
        this.image = imageUrl;
        return this;
    }

    /** The site name shown in social embeds. */
    public Seo siteName(String siteName) {
        this.siteName = siteName;
        return this;
    }

    /** Open Graph type — "website" (default), "article", "product", ... */
    public Seo type(String ogType) {
        this.type = ogType;
        return this;
    }

    /** Renders the full head block. */
    public Element render() {
        List<Element> tags = new ArrayList<>();
        tags.add(title(title));
        tags.add(meta("description", description));

        tags.add(og("og:title", title));
        tags.add(og("og:description", description));
        tags.add(og("og:type", type));
        if (url != null) {
            tags.add(link(attrs().set("rel", "canonical").href(url)));
            tags.add(og("og:url", url));
        }
        if (image != null) {
            tags.add(og("og:image", image));
        }
        if (siteName != null) {
            tags.add(og("og:site_name", siteName));
        }

        tags.add(meta("twitter:card", image != null ? "summary_large_image" : "summary"));
        tags.add(meta("twitter:title", title));
        tags.add(meta("twitter:description", description));
        if (image != null) {
            tags.add(meta("twitter:image", image));
        }

        return fragment(tags.toArray());
    }

    private static Element og(String property, String content) {
        return meta(attrs().set("property", property).set("content", content));
    }
}
