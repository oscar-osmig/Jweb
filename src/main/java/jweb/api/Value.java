package jweb.api;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a configuration value — a property from {@code application.yaml}
 * or the environment — into a field or constructor parameter:
 *
 * <pre>{@code
 * @Value("${jweb.admin.token:}") private String adminToken;
 *
 * public Api(@Value("${app.timeout-seconds:120}") long timeout) { ... }
 * }</pre>
 *
 * <p>Short spelling of Spring's {@code @Value}: the same
 * {@code ${property:default}} syntax, no {@code org.springframework} import.</p>
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@org.springframework.beans.factory.annotation.Value("")
public @interface Value {
    /** The property expression, e.g. {@code "${jweb.admin.email:}"}. */
    @AliasFor(annotation = org.springframework.beans.factory.annotation.Value.class, attribute = "value")
    String value();
}
