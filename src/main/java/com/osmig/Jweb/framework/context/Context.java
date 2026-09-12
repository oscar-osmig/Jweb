package com.osmig.Jweb.framework.context;

/**
 * The pre-3.0 spelling of the request-scoped context.
 *
 * @deprecated Replaced by {@link jweb.Context} — shorter import, same statics
 *             (inherited here). {@code Context.key(...)} now returns
 *             {@link jweb.ContextKey}; the old {@code ContextKey} record is gone.
 */
@Deprecated
public class Context extends jweb.Context {

    protected Context() {}
}
