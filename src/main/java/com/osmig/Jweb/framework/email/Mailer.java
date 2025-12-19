package com.osmig.Jweb.framework.email;

import com.osmig.Jweb.framework.async.Jobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Email sending service.
 *
 * <h2>Configuration</h2>
 * Configure in application.properties:
 * <pre>
 * spring.mail.host=smtp.gmail.com
 * spring.mail.port=587
 * spring.mail.username=your-email@gmail.com
 * spring.mail.password=your-app-password
 * spring.mail.properties.mail.smtp.auth=true
 * spring.mail.properties.mail.smtp.starttls.enable=true
 *
 * # JWeb mail settings
 * jweb.mail.from=noreply@yourapp.com
 * jweb.mail.enabled=true
 * </pre>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Send synchronously
 * Mailer.send(Email.create()
 *     .to("user@example.com")
 *     .subject("Hello")
 *     .text("Welcome!")
 *     .build());
 *
 * // Send asynchronously
 * Mailer.sendAsync(email).thenRun(() -> {
 *     System.out.println("Email sent!");
 * });
 * }</pre>
 *
 * <h2>Dependency Required</h2>
 * Add to pom.xml:
 * <pre>{@code
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-mail</artifactId>
 * </dependency>
 * }</pre>
 */
public final class Mailer {

    private static final Logger log = LoggerFactory.getLogger(Mailer.class);

    private static MailerBackend backend = new LoggingBackend();
    private static String defaultFrom;
    private static boolean enabled = true;

    private Mailer() {}

    /**
     * Configures the mailer with a Spring JavaMailSender.
     * Called automatically by JWebMailAutoConfiguration if spring-boot-starter-mail is on classpath.
     *
     * @param mailSender the Spring mail sender
     */
    public static void configure(org.springframework.mail.javamail.JavaMailSender mailSender) {
        backend = new SpringMailBackend(mailSender);
        log.info("Mailer configured with Spring JavaMailSender");
    }

    /**
     * Sets the default from address.
     *
     * @param from the default sender address
     */
    public static void setDefaultFrom(String from) {
        defaultFrom = from;
    }

    /**
     * Enables or disables email sending.
     *
     * @param enabled true to enable
     */
    public static void setEnabled(boolean enabled) {
        Mailer.enabled = enabled;
    }

    /**
     * Sends an email synchronously.
     *
     * @param email the email to send
     * @throws MailException if sending fails
     */
    public static void send(Email email) {
        if (!enabled) {
            log.info("Email sending disabled. Would send to: {}", email.getTo());
            return;
        }

        Email toSend = applyDefaults(email);
        backend.send(toSend);
    }

    /**
     * Sends an email asynchronously.
     *
     * @param email the email to send
     * @return a future that completes when sent
     */
    public static CompletableFuture<Void> sendAsync(Email email) {
        return Jobs.submit(() -> {
            send(email);
        });
    }

    /**
     * Sends an email asynchronously (fire and forget).
     *
     * @param email the email to send
     */
    public static void sendInBackground(Email email) {
        Jobs.run(() -> {
            try {
                send(email);
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", email.getTo(), e.getMessage());
            }
        });
    }

    private static Email applyDefaults(Email email) {
        if (email.getFrom() == null && defaultFrom != null) {
            return Email.create()
                .from(defaultFrom)
                .to(email.getTo().toArray(new String[0]))
                .cc(email.getCc().toArray(new String[0]))
                .bcc(email.getBcc().toArray(new String[0]))
                .subject(email.getSubject())
                .text(email.getTextBody())
                .html(email.getHtmlBody())
                .build();
        }
        return email;
    }

    /**
     * Backend interface for sending emails.
     */
    public interface MailerBackend {
        void send(Email email) throws MailException;
    }

    /**
     * Logging backend (used when no mail sender configured).
     */
    private static class LoggingBackend implements MailerBackend {
        @Override
        public void send(Email email) {
            log.info("Email would be sent (no mail sender configured):");
            log.info("  To: {}", email.getTo());
            log.info("  Subject: {}", email.getSubject());
            log.info("  Body: {}", email.getTextBody() != null ?
                email.getTextBody().substring(0, Math.min(100, email.getTextBody().length())) + "..." :
                "(HTML only)");
        }
    }

    /**
     * Spring Mail backend.
     */
    private static class SpringMailBackend implements MailerBackend {
        private final org.springframework.mail.javamail.JavaMailSender mailSender;

        SpringMailBackend(org.springframework.mail.javamail.JavaMailSender mailSender) {
            this.mailSender = mailSender;
        }

        @Override
        public void send(Email email) throws MailException {
            try {
                jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
                org.springframework.mail.javamail.MimeMessageHelper helper =
                    new org.springframework.mail.javamail.MimeMessageHelper(message,
                        email.hasAttachments(), "UTF-8");

                helper.setTo(email.getTo().toArray(new String[0]));
                if (!email.getCc().isEmpty()) {
                    helper.setCc(email.getCc().toArray(new String[0]));
                }
                if (!email.getBcc().isEmpty()) {
                    helper.setBcc(email.getBcc().toArray(new String[0]));
                }
                if (email.getFrom() != null) {
                    helper.setFrom(email.getFrom());
                }
                if (email.getReplyTo() != null) {
                    helper.setReplyTo(email.getReplyTo());
                }
                helper.setSubject(email.getSubject());

                if (email.hasHtml()) {
                    helper.setText(
                        email.getTextBody() != null ? email.getTextBody() : "",
                        email.getHtmlBody()
                    );
                } else if (email.getTextBody() != null) {
                    helper.setText(email.getTextBody());
                }

                for (Email.Attachment att : email.getAttachments()) {
                    helper.addAttachment(att.filename(),
                        new org.springframework.core.io.ByteArrayResource(att.content()),
                        att.contentType());
                }

                mailSender.send(message);
                log.info("Email sent to: {}", email.getTo());

            } catch (Exception e) {
                throw new MailException("Failed to send email: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Exception thrown when email sending fails.
     */
    public static class MailException extends RuntimeException {
        public MailException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
