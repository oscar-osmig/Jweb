package com.osmig.Jweb.framework.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/** Auto-configures Mailer with fallback email providers from application.yaml. */
@Configuration
public class MailerConfig {
    private static final Logger log = LoggerFactory.getLogger(MailerConfig.class);

    @Value("${jweb.mail.from:noreply@jweb.dev}")
    private String fromAddress;

    @Value("${jweb.mail.resend.api-key:}")
    private String resendKey;

    @Value("${jweb.mail.sendgrid.api-key:}")
    private String sendGridKey;

    @Value("${jweb.mail.brevo.api-key:}")
    private String brevoKey;

    @Value("${jweb.mail.enabled:true}")
    private boolean enabled;

    @PostConstruct
    public void configure() {
        Mailer.setEnabled(enabled);
        Mailer.setDefaultFrom(fromAddress);

        FallbackBackend.Builder builder = FallbackBackend.create();
        int count = 0;

        if (hasValue(resendKey)) {
            builder.resend(resendKey, fromAddress);
            count++;
            log.info("Resend email provider configured");
        }
        if (hasValue(sendGridKey)) {
            builder.sendGrid(sendGridKey, fromAddress);
            count++;
            log.info("SendGrid email provider configured");
        }
        if (hasValue(brevoKey)) {
            builder.brevo(brevoKey, fromAddress);
            count++;
            log.info("Brevo email provider configured");
        }

        if (count > 0) {
            Mailer.configure(builder.build());
            log.info("Mailer configured with {} provider(s) and fallback support", count);
        } else {
            log.info("No email providers configured - emails will be logged only");
        }
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
