package com.osmig.Jweb.framework.navigation;

/**
 * The pre-3.0 spelling of the navigation link builder.
 *
 * @deprecated Replaced by {@link jweb.Link} — {@code Link.to(...)} (inherited
 *             here) hands out that type, so declare {@code jweb.Link}. This
 *             subclass only keeps old subclasses compiling.
 */
@Deprecated
public class Link extends jweb.Link {

    protected Link(String href) {
        super(href);
    }
}
