package com.osmig.Jweb.framework;

import com.osmig.Jweb.framework.util.YamlPropertySourceFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.*;

/**
 * Main annotation for JWeb applications.
 *
 * Use this instead of @SpringBootApplication to get automatic
 * framework configuration including component scanning and
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
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootApplication
@ComponentScan(basePackages = "com.osmig.Jweb")
@PropertySource(value = "classpath:jweb.yaml", factory = YamlPropertySourceFactory.class)
public @interface JWebApplication {
}
