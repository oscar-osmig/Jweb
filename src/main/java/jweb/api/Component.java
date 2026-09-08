package jweb.api;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class the container creates once and injects wherever a
 * constructor asks for it — your {@code Routes}, stores and services:
 *
 * <pre>{@code
 * @Component
 * public class MessageStore { ... }
 *
 * @Component
 * public class Routes implements JWebRoutes {
 *     public Routes(MessageStore store) { ... }      // injected
 * }
 * }</pre>
 *
 * <p>Short spelling of Spring's {@code @Component}, so app code needs no
 * {@code org.springframework} import; {@code @REST} classes are components
 * already.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@org.springframework.stereotype.Component
public @interface Component {
    /** The bean name; defaults to the decapitalised class name. */
    @AliasFor(annotation = org.springframework.stereotype.Component.class, attribute = "value")
    String value() default "";
}
