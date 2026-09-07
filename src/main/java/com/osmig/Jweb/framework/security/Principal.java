package com.osmig.Jweb.framework.security;

/**
 * @deprecated Moved to {@link jweb.Principal} — same class, shorter import.
 *             {@code Auth.user()} and {@code Principal.of(...)} return
 *             {@code jweb.Principal}; only the static factories still resolve
 *             through this name.
 */
@Deprecated
public class Principal extends jweb.Principal {

    private Principal(Builder builder) {
        super(builder);
    }
}
