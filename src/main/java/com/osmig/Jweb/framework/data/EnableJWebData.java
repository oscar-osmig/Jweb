package com.osmig.Jweb.framework.data;

import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.annotation.*;

/**
 * Enables JWeb data support with JPA repositories.
 *
 * <p>This annotation is optional - JWeb data auto-configuration
 * works automatically when spring-boot-starter-data-jpa is on
 * the classpath. Use this annotation when you need to:</p>
 * <ul>
 *   <li>Specify custom base packages for entity/repository scanning</li>
 *   <li>Override auto-configuration behavior</li>
 *   <li>Make the data layer configuration explicit</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * @SpringBootApplication
 * @EnableJWebData(
 *     entityPackages = "com.example.model",
 *     repositoryPackages = "com.example.repository"
 * )
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h2>Default Behavior</h2>
 * <p>Without this annotation, JWeb automatically scans for entities
 * and repositories from the main application class package.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JWebDataAutoConfiguration.class)
@EnableTransactionManagement
public @interface EnableJWebData {

    /**
     * Base packages to scan for JPA entities.
     * Defaults to the package of the annotated class.
     *
     * @return entity package names
     */
    String[] entityPackages() default {};

    /**
     * Base packages to scan for JPA repositories.
     * Defaults to the package of the annotated class.
     *
     * @return repository package names
     */
    String[] repositoryPackages() default {};
}
