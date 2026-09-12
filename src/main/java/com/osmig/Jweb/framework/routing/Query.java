package com.osmig.Jweb.framework.routing;

import jweb.QueryParam;

/**
 * The pre-3.0 spelling of a typed query parameter.
 *
 * @param <T> the parameter's Java type
 * @deprecated Replaced by {@link jweb.QueryParam} — same API under a name that
 *             does not collide with the {@code @Query} annotation in
 *             {@code jweb.api}. Declarations of this type keep compiling: every
 *             factory here returns this subtype.
 */
@Deprecated
public final class Query<T> extends QueryParam<T> {

    private Query(String name, Class<T> type, T defaultValue, boolean required) {
        super(name, type, defaultValue, required);
    }

    /** See {@link QueryParam#of(String, Class)}. */
    public static <T> Query<T> of(String name, Class<T> type) {
        return new Query<>(name, type, null, false);
    }

    @Override
    public Query<T> orElse(T defaultValue) {
        return new Query<>(name(), type(), defaultValue, false);
    }

    @Override
    public Query<T> required() {
        return new Query<>(name(), type(), null, true);
    }
}
