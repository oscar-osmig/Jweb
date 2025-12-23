package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class SetupSection {
    private SetupSection() {}

    public static Element render() {
        return section(
            docTitle("Getting Started"),
            para("JWeb is built on Spring Boot. Get started by cloning the repository."),

            docSubtitle("Coming Soon"),
            docTip("We are working to find the best way to make JWeb available for everyone. " +
                   "For now, we will keep you updated."),
            codeBlock("""
// please reach out to us if you'd like to try JWeb in its early stage"""),

            docSubtitle("Application Configuration"),
            para("Configure JWeb in application.yaml:"),
            codeBlock("""
server:
  port: 8080
  compression:
    enabled: true
    mime-types: text/html,text/css,application/javascript,application/json
    min-response-size: 1024

spring:
  application:
    name: MyApp

jweb:
  # API base path
  api:
    base: /api/v1

  # Development settings
  dev:
    hot-reload: true
    watch-paths: src/main/java,src/main/resources
    debounce-ms: 50

  # Performance settings
  performance:
    minify-css: true
    minify-html: false
    prefetch:
      enabled: true
      cache-ttl: 300000
      hover-delay: 300"""),

            docSubtitle("Required Imports"),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;"""),

            docSubtitle("Configure Routes"),
            codeBlock("""
@Component
public class Routes implements JWebRoutes {
    public void configure(JWeb app) {
        app.pages(
            "/", HomePage.class,
            "/about", AboutPage.class
        );
    }
}"""),

            docSubtitle("Create a Page"),
            codeBlock("""
public class HomePage implements Template {
    public Element render() {
        return main(
            h1("Welcome"),
            p("Your first JWeb page!")
        );
    }
}"""),

            docTip("Run with: mvn spring-boot:run, then visit http://localhost:8080")
        );
    }
}
