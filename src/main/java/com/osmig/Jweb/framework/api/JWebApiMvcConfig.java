package com.osmig.Jweb.framework.api;

import com.osmig.Jweb.framework.JWeb;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Wires the {@code @REST} conveniences into Spring MVC: the
 * {@code jweb.api} parameter annotations and {@code jweb.Request} injection
 * ({@link JWebArgumentResolver}), and the app's guards in front of every
 * Spring-dispatched handler ({@link GuardInterceptor}).
 */
@Configuration
public class JWebApiMvcConfig implements WebMvcConfigurer {

    private final JWeb app;

    public JWebApiMvcConfig(JWeb app) {
        this.app = app;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new JWebArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new GuardInterceptor(app));
    }
}
