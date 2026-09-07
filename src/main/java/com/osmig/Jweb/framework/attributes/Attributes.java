package com.osmig.Jweb.framework.attributes;

import java.util.Map;

/**
 * @deprecated Moved to {@link jweb.Attributes} — same class, shorter import.
 *             This name is a compatibility alias only: {@code attrs()} returns
 *             {@code jweb.Attributes}, so declare variables as {@code jweb.Attributes}.
 */
@Deprecated
public class Attributes extends jweb.Attributes {

    public Attributes() {}

    public Attributes(Map<String, String> initial) {
        super(initial);
    }
}
