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

spring:
  application:
    name: MyApp

jweb:
  # API base path
  api:
    base: /api/v1

  # MongoDB (optional)
  data:
    enabled: true
    mongo:
      uri: ${MONGO_URI:mongodb://localhost:27017}
      database: ${MONGO_DB:myapp}"""),

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
