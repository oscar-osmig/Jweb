package com.osmig.Jweb.app;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.JWebRoutes;
import org.springframework.stereotype.Component;

/**
 * Application routes.
 */
@Component
public class Routes implements JWebRoutes {

    @Override
    public void configure(JWeb app) {
        app.get("/", ctx -> new Layout("JWeb - Light Java Web Framework",
            new HomePage().render()
        ).render());
    }
}
