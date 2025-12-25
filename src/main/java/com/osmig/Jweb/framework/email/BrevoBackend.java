package com.osmig.Jweb.framework.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.*;
import java.util.stream.Collectors;

/** Brevo (Sendinblue) email backend. 300 emails/day free. */
public class BrevoBackend implements Mailer.MailerBackend {
    private static final Logger log = LoggerFactory.getLogger(BrevoBackend.class);
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    private final String apiKey;
    private final String fromAddress;
    private final String fromName;

    public BrevoBackend(String apiKey, String fromAddress) {
        this(apiKey, fromAddress, "JWeb");
    }

    public BrevoBackend(String apiKey, String fromAddress, String fromName) {
        this.apiKey = apiKey;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @Override
    public void send(Email email) throws Mailer.MailException {
        try {
            String from = email.getFrom() != null ? email.getFrom() : fromAddress;
            String json = buildJson(from, email);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new RateLimitException("Brevo rate limit exceeded");
            }
            if (response.statusCode() >= 400) {
                throw new Mailer.MailException("Brevo error: " + response.body());
            }
            log.info("Email sent via Brevo to: {}", email.getTo());
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new Mailer.MailException("Brevo failed: " + e.getMessage(), e);
        }
    }

    private String buildJson(String from, Email email) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"sender\":{\"name\":\"").append(escape(fromName)).append("\",");
        sb.append("\"email\":\"").append(escape(from)).append("\"},");
        sb.append("\"to\":[");
        sb.append(email.getTo().stream()
            .map(t -> "{\"email\":\"" + escape(t) + "\"}")
            .collect(Collectors.joining(",")));
        sb.append("],");
        sb.append("\"subject\":\"").append(escape(email.getSubject())).append("\"");
        if (email.getTextBody() != null) {
            sb.append(",\"textContent\":\"").append(escape(email.getTextBody())).append("\"");
        }
        if (email.getHtmlBody() != null) {
            sb.append(",\"htmlContent\":\"").append(escape(email.getHtmlBody())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    public static class RateLimitException extends Mailer.MailException {
        public RateLimitException(String msg) { super(msg); }
    }
}
