package com.osmig.Jweb.framework.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.*;
import java.util.stream.Collectors;

/** SendGrid email backend. 100 emails/day free. */
public class SendGridBackend implements Mailer.MailerBackend {
    private static final Logger log = LoggerFactory.getLogger(SendGridBackend.class);
    private static final String API_URL = "https://api.sendgrid.com/v3/mail/send";
    private final String apiKey;
    private final String fromAddress;

    public SendGridBackend(String apiKey, String fromAddress) {
        this.apiKey = apiKey;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(Email email) throws Mailer.MailException {
        try {
            String from = email.getFrom() != null ? email.getFrom() : fromAddress;
            String json = buildJson(from, email);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new RateLimitException("SendGrid rate limit exceeded");
            }
            if (response.statusCode() >= 400) {
                throw new Mailer.MailException("SendGrid error: " + response.body());
            }
            log.info("Email sent via SendGrid to: {}", email.getTo());
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new Mailer.MailException("SendGrid failed: " + e.getMessage(), e);
        }
    }

    private String buildJson(String from, Email email) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"personalizations\":[{\"to\":[");
        sb.append(email.getTo().stream()
            .map(t -> "{\"email\":\"" + escape(t) + "\"}")
            .collect(Collectors.joining(",")));
        sb.append("]}],");
        sb.append("\"from\":{\"email\":\"").append(escape(from)).append("\"},");
        sb.append("\"subject\":\"").append(escape(email.getSubject())).append("\",");
        sb.append("\"content\":[");
        if (email.getTextBody() != null) {
            sb.append("{\"type\":\"text/plain\",\"value\":\"").append(escape(email.getTextBody())).append("\"}");
            if (email.getHtmlBody() != null) sb.append(",");
        }
        if (email.getHtmlBody() != null) {
            sb.append("{\"type\":\"text/html\",\"value\":\"").append(escape(email.getHtmlBody())).append("\"}");
        }
        sb.append("]}");
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
