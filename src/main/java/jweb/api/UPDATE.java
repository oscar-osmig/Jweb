package jweb.api;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PUT endpoint mapping.
 * Short alias for {@code com.osmig.Jweb.framework.api.UPDATE}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.PUT)
public @interface UPDATE {
    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};
}
