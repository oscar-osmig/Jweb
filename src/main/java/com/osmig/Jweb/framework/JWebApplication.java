package com.osmig.Jweb.framework;

import com.osmig.Jweb.framework.util.YamlPropertySourceFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.*;

/**
 * Main annotation for JWeb applications.
 *
 * Use this instead of @SpringBootApplication to get automatic
 * framework configuration (the framework beans arrive via auto-configuration) and
 * jweb.yaml loading.
 *
 * <pre>
 * {@literal @}JWebApplication
 * public class App {
 *     public static void main(String[] args) {
 *         SpringApplication.run(App.class, args);
 *     }
 * }
 * </pre>
 *
 * @deprecated Replaced by {@code jweb.JWebApplication} — shorter import, same behavior. Existing code keeps working.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootApplication
@PropertySource(value = "classpath:jweb.yaml", factory = YamlPropertySourceFactory.class)
@Deprecated
public @interface JWebApplication {
}
