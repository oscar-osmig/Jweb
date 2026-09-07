package com.osmig.Jweb.framework.styles;

/**
 * @deprecated Moved to {@link jweb.css.Keyframes} — same class, shorter import.
 *             {@code keyframes(name)} and the presets return {@code jweb.css.Keyframes};
 *             only the static factories still resolve through this name.
 */
@Deprecated
public class Keyframes extends jweb.css.Keyframes {

    protected Keyframes(String name) {
        super(name);
    }
}
