package com.osmig.Jweb.framework.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.*;
import java.util.stream.Collectors;

/** Resend.com email backend. 100 emails/day free. */
public class ResendBackend implements Mailer.MailerBackend {
    private static final Logger log = LoggerFactory.getLogger(ResendBackend.class);
    private static final String API_URL = "https://api.resend.com/emails";
    private final String apiKey;
    private final String fromAddress;

    public ResendBackend(String apiKey, String fromAddress) {
        this.apiKey = apiKey;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(Email email) throws Mailer.MailException {
        try {
            String from = email.getFrom() != null ? email.getFrom() : fromAddress;
            String to = email.getTo().stream().collect(Collectors.joining(","));
            String json = buildJson(from, to, email.getSubject(), email.getTextBody(), email.getHtmlBody());

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new RateLimitException("Resend rate limit exceeded");
            }
            if (response.statusCode() >= 400) {
                throw new Mailer.MailException("Resend error: " + response.body());
            }
            log.info("Email sent via Resend to: {}", to);
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new Mailer.MailException("Resend failed: " + e.getMessage(), e);
        }
    }

    private String buildJson(String from, String to, String subject, String text, String html) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"from\":\"").append(escape(from)).append("\",");
        sb.append("\"to\":\"").append(escape(to)).append("\",");
        sb.append("\"subject\":\"").append(escape(subject)).append("\"");
        if (text != null) sb.append(",\"text\":\"").append(escape(text)).append("\"");
        if (html != null) sb.append(",\"html\":\"").append(escape(html)).append("\"");
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
