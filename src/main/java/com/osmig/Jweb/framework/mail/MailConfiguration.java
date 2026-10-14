package com.osmig.Jweb.framework.mail;

import com.osmig.Jweb.framework.util.Log;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Picks the {@link jweb.Mail} sender at startup: a {@code Mail.Sender} bean
 * when the app defines one, SMTP when {@code jweb.mail.smtp.host} is set,
 * the outbox otherwise — which logs each message instead of sending it,
 * the right thing in development.
 */
@Configuration
public class MailConfiguration {

    @Value("${jweb.mail.from:}")
    private String from;

    @Value("${jweb.mail.smtp.host:}")
    private String host;

    @Value("${jweb.mail.smtp.port:587}")
    private int port;

    @Value("${jweb.mail.smtp.username:}")
    private String username;

    @Value("${jweb.mail.smtp.password:}")
    private String password;

    @Value("${jweb.mail.smtp.tls:starttls}")
    private String tls;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 115)
    public ApplicationRunner jwebMailSender(ObjectProvider<jweb.Mail.Sender> senders) {
        return args -> {
            jweb.Mail.defaultFrom(from);
            jweb.Mail.Sender custom = senders.getIfUnique();
            if (custom != null) {
                jweb.Mail.sender(custom);
                Log.info("jweb.Mail sender: {}", custom);
            } else if (host != null && !host.isBlank()) {
                jweb.Mail.Tls mode = switch (tls.trim().toLowerCase()) {
                    case "implicit", "ssl", "tls" -> jweb.Mail.Tls.IMPLICIT;
                    case "none", "plain" -> jweb.Mail.Tls.NONE;
                    default -> jweb.Mail.Tls.STARTTLS;
                };
                jweb.Mail.Smtp smtp = new jweb.Mail.Smtp(host.trim(), port, username, password, mode);
                jweb.Mail.sender(smtp);
                Log.info("jweb.Mail sender: {}", smtp);
            } else {
                Log.info("jweb.Mail sender: outbox (no jweb.mail.smtp.host — messages are logged, not sent)");
            }
        };
    }
}
