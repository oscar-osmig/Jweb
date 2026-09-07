package com.osmig.Jweb.framework.seo;

/**
 * @deprecated Moved to {@link jweb.Seo} — same class, shorter import.
 *             {@code Seo.of(title, description)} returns {@code jweb.Seo}.
 */
@Deprecated
public class Seo extends jweb.Seo {

    protected Seo(String title, String description) {
        super(title, description);
    }
}
