package jweb.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bounds the length of a String record component bound from request
 * parameters (see {@code app.action}); outside the bounds is a 400.
 *
 * <pre>{@code
 * record Note(@Length(min = 1, max = 280) String text) {}
 * }</pre>
 */
@Target({ElementType.RECORD_COMPONENT, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Length {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}
