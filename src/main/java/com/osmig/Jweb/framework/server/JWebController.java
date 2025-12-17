package com.osmig.Jweb.framework.server;

import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.routing.Router;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Spring MVC Controller that handles all JWeb routes.
 */
@Controller
public class JWebController {

    private final Router router;

    public JWebController(Router router) {
        this.router = router;
    }

    @RequestMapping("/**")
    public ResponseEntity<String> handleRequest(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        String method = servletRequest.getMethod();
        String path = servletRequest.getRequestURI();

        Optional<Router.RouteMatch> match = router.match(method, path);

        if (match.isEmpty()) {
            return handleNotFound(path);
        }

        try {
            Request request = new Request(servletRequest);
            Object result = match.get().handle(request);
            return processResult(result);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    private ResponseEntity<String> processResult(Object result) {
        if (result == null) {
            return ResponseEntity.ok().body("");
        }

        if (result instanceof Element element) {
            String html = element.toHtml();
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
        }

        if (result instanceof String str) {
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(str);
        }

        if (result instanceof ResponseEntity<?> responseEntity) {
            @SuppressWarnings("unchecked")
            ResponseEntity<String> typed = (ResponseEntity<String>) responseEntity;
            return typed;
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(result.toString());
    }

    private ResponseEntity<String> handleNotFound(String path) {
        Element notFoundPage = html(
            head(title("404 - Not Found")),
            body(
                div(new Attributes().style(
                    "font-family: system-ui, sans-serif; max-width: 600px; margin: 100px auto; text-align: center;"
                ),
                    h1("404"),
                    h2("Page Not Found"),
                    p("The page " + path + " could not be found."),
                    a("/", "← Go Home")
                )
            )
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.TEXT_HTML)
            .body(notFoundPage.toHtml());
    }

    private ResponseEntity<String> handleError(Exception e) {
        Element errorPage = html(
            head(title("500 - Server Error")),
            body(
                div(new Attributes().style(
                    "font-family: system-ui, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px;"
                ),
                    h1(new Attributes().style("color: #dc3545;"), "500 - Server Error"),
                    p("An error occurred while processing your request."),
                    pre(new Attributes().style(
                        "background: #f8f9fa; padding: 15px; border-radius: 5px; overflow-x: auto;"
                    ),
                        code(e.getClass().getName() + ": " + e.getMessage())
                    ),
                    a("/", "← Go Home")
                )
            )
        );

        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.TEXT_HTML)
            .body(errorPage.toHtml());
    }
}
