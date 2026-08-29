package jweb;

import com.osmig.Jweb.framework.util.YamlPropertySourceFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Main annotation for JWeb applications.
 *
 * Use this instead of {@code @SpringBootApplication} to get automatic
 * framework configuration (the framework beans arrive via auto-configuration)
 * and jweb.yaml loading.
 *
 * <pre>
 * {@literal @}JWebApplication
 * public class App {
 *     public static void main(String[] args) {
 *         SpringApplication.run(App.class, args);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootApplication
@PropertySource(value = "classpath:jweb.yaml", factory = YamlPropertySourceFactory.class)
public @interface JWebApplication {
}
