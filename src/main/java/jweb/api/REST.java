package jweb.api;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shorthand for @RestController + @RequestMapping. Paths must start with /api/v*.
 * Short alias for {@code com.osmig.Jweb.framework.api.REST}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
@RequestMapping
public @interface REST {
    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};
}
